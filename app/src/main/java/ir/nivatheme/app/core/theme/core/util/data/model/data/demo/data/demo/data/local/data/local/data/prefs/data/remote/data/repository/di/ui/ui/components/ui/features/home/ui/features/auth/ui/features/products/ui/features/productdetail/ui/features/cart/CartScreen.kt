
// app/src/main/java/ir/nivatheme/app/ui/features/cart/CartScreen.kt
package ir.nivatheme.app.ui.features.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.nivatheme.app.core.theme.*
import ir.nivatheme.app.core.util.toman
import ir.nivatheme.app.core.util.toFa
import ir.nivatheme.app.data.model.CartItem
import ir.nivatheme.app.data.repository.NivaRepository
import ir.nivatheme.app.ui.components.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(private val repo: NivaRepository) : ViewModel() {
    val items = repo.cart.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val subtotal = items.map { l -> l.sumOf { it.lineTotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    private val pct = MutableStateFlow(0)
    val discount = combine(subtotal, pct) { s, p -> s * p / 100 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val total = combine(subtotal, discount) { s, d -> s - d }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val appliedCode = MutableStateFlow<String?>(null)
    val loading = MutableStateFlow(false)

    sealed class Ev { data object NeedLogin : Ev(); data object Done : Ev() }
    private val _ev = MutableSharedFlow<Ev>(); val ev: SharedFlow<Ev> = _ev

    fun setQty(id: Long, q: Int) = viewModelScope.launch { repo.setQty(id, q) }
    fun remove(id: Long) = viewModelScope.launch { repo.removeFromCart(id) }

    fun applyCode(code: String): Boolean =
        if (code.trim().uppercase() == "NIVA10") { pct.value = 10; appliedCode.value = "NIVA10"; true } else false

    fun checkout() = viewModelScope.launch {
        if (!repo.session.isLoggedIn.first()) { _ev.emit(Ev.NeedLogin); return@launch }
        if (items.value.isEmpty()) return@launch
        loading.value = true
        kotlinx.coroutines.delay(1200) // در Production: ایجاد سفارش واقعی + دریافت آدرس درگاه از سرور
        repo.placeOrder(items.value, discount.value)
        loading.value = false
        _ev.emit(Ev.Done)
    }
}

@Composable
fun CartScreen(
    vm: CartViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onOrders: () -> Unit,
    onDownloads: () -> Unit
) {
    val items by vm.items.collectAsStateWithLifecycle()
    val subtotal by vm.subtotal.collectAsStateWithLifecycle()
    val discount by vm.discount.collectAsStateWithLifecycle()
    val total by vm.total.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val applied by vm.appliedCode.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    var code by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.ev.collect {
            when (it) {
                CartViewModel.Ev.NeedLogin -> { snack.showSnackbar("برای پرداخت ابتدا وارد حساب شوید"); onLogin() }
                CartViewModel.Ev.Done -> showSuccess = true
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            NivaHeader(
                title = "سبد خرید",
                subtitle = "${items.sumOf { it.qty }.toFa()} محصول دیجیتال",
                onBack = onBack
            )
            if (items.isEmpty()) {
                EmptyState(Icons.Rounded.ShoppingCart, "سبد خرید خالی است", "از فروشگاه محصولات مورد نظر خود را اضافه کنید")
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(items, key = { it.product.id }) { c ->
                        CartItemCard(
                            item = c,
                            onPlus = { vm.setQty(c.product.id, c.qty + 1) },
                            onMinus = { vm.setQty(c.product.id, c.qty - 1) },
                            onDelete = { vm.remove(c.product.id) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    item {
                        Card(shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.PieChart, null, tint = Turquoise)
                                    Spacer(Modifier.width(8.dp))
                                    Text("کد تخفیف", style = MaterialTheme.typography.titleMedium)
                                }
                                Spacer(Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = code, onValueChange = { code = it; codeError = false },
                                        placeholder = { Text("مثلاً NIVA10") },
                                        modifier = Modifier.weight(1f), singleLine = true,
                                        shape = RoundedCornerShape(12.dp), isError = codeError
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Button(
                                        onClick = { if (!vm.applyCode(code)) codeError = true },
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("اعمال") }
                                }
                                if (codeError) Text("کد تخفیف معتبر نیست", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                                if (applied != null) Text("کد «$applied» اعمال شد (٪۱۰)", color = SuccessGreen, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Card(shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Receipt, null, tint = Turquoise)
                                    Spacer(Modifier.width(8.dp))
                                    Text("خلاصه سفارش", style = MaterialTheme.typography.titleMedium)
                                }
                                Spacer(Modifier.height(10.dp))
                                SummaryRow("جمع", subtotal.toman())
                                SummaryRow("تخفیف", "− ${discount.toman()}", color = WarningOrange)
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                        .background(Turquoise.copy(alpha = 0.08f)).padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("قابل پرداخت", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        Text(total.toman(), color = Turquoise, style = MaterialTheme.typography.titleLarge)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        GradientButton(
                            text = if (loading) "در حال پردازش..." else "پرداخت و دریافت لینک دانلود",
                            icon = Icons.AutoMirrored.Rounded.ArrowForward,
                            onClick = { vm.checkout() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Shield, null, tint = Turquoise, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "لینک دانلود پس از پرداخت به‌صورت خودکار فعال می‌شود",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter))
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false },
            icon = { Icon(Icons.Rounded.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(48.dp)) },
            title = { Text("پرداخت موفق", textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("سفارش شما ثبت شد و محصولات به «دانلودهای من» اضافه شدند.")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "⚠ حالت دمو: پرداخت واقعی از درگاه سایت در نسخه Production انجام می‌شود.",
                        style = MaterialTheme.typography.labelMedium, color = WarningOrange
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showSuccess = false; onDownloads() }) { Text("دانلودهای من") } },
            dismissButton = { TextButton(onClick = { showSuccess = false; onOrders() }) { Text("سفارش‌ها") } }
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String, color: Color? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color ?: MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun CartItemCard(item: CartItem, onPlus: () -> Unit, onMinus: () -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(item.product.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    TypeChip(item.product.type)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.product.shortDesc,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ProductThumb(item.product.iconKey, Modifier.size(64.dp), iconSize = 26.dp)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(Modifier.clickable(onClick = onMinus).padding(horizontal = 12.dp, vertical = 6.dp)) { Text("−") }
                    Text(item.qty.toFa(), style = MaterialTheme.typography.titleMedium)
                    Box(Modifier.clickable(onClick = onPlus).padding(horizontal = 12.dp, vertical = 6.dp)) { Text("+") }
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Rounded.Sell, null, tint = Turquoise, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(item.lineTotal.toman(), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Rounded.Delete, "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
