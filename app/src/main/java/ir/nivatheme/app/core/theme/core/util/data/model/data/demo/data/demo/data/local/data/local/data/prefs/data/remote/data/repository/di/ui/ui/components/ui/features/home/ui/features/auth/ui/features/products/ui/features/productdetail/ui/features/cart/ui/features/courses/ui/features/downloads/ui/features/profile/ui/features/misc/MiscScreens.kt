// app/src/main/java/ir/nivatheme/app/ui/features/misc/MiscScreens.kt
package ir.nivatheme.app.ui.features.misc

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import ir.nivatheme.app.data.local.OrderEntity
import ir.nivatheme.app.data.model.Product
import ir.nivatheme.app.data.repository.NivaRepository
import ir.nivatheme.app.ui.MainViewModel
import ir.nivatheme.app.ui.components.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MiscViewModel @Inject constructor(private val repo: NivaRepository) : ViewModel() {
    val favorites = repo.favorites.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<Product>())
    val orders = repo.orders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<OrderEntity>())
    val wallet = repo.session.wallet.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)
    fun unFav(id: Long) = viewModelScope.launch { repo.toggleFavorite(id) }
    fun clearLocal() = viewModelScope.launch { repo.session.clear() }
}

@Composable
fun FavoritesScreen(vm: MiscViewModel = hiltViewModel(), onBack: () -> Unit, onProduct: (Long) -> Unit) {
    val favs by vm.favorites.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        NivaHeader(title = "علاقه‌مندی‌ها", subtitle = "محصولات مورد علاقه شما", onBack = onBack)
        if (favs.isEmpty()) EmptyState(Icons.Rounded.FavoriteBorder, "لیست خالی است", "با لمس آیکون قلب، محصولات را ذخیره کنید")
        else LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(favs) { p ->
                ProductCard(
                    p, fav = true,
                    onFav = { vm.unFav(p.id) },
                    onAdd = { },
                    onClick = { onProduct(p.id) }
                )
            }
        }
    }
}

@Composable
fun OrdersScreen(vm: MiscViewModel = hiltViewModel(), onBack: () -> Unit) {
    val orders by vm.orders.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        NivaHeader(title = "سفارش‌های من", subtitle = "تاریخچه خریدهای شما", onBack = onBack)
        if (orders.isEmpty()) EmptyState(Icons.Rounded.ShoppingBag, "سفارشی ثبت نشده", "اولین خرید خود را از فروشگاه انجام دهید")
        else Column(Modifier.padding(16.dp)) {
            orders.forEach { o ->
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("سفارش #${(1000 + o.id).toFa()}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(SuccessGreen.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                Text(o.status, color = SuccessGreen, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text("تاریخ: ${o.date}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text(o.itemsJson, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        Text("مبلغ: ${o.total.toman()}", color = Turquoise, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun WalletScreen(vm: MiscViewModel = hiltViewModel(), onBack: () -> Unit) {
    val wallet by vm.wallet.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    Column(Modifier.fillMaxSize()) {
        NivaHeader(title = "کیف پول", subtitle = "موجودی، تراکنش‌ها و شارژ حساب", onBack = onBack)
        Column(Modifier.padding(20.dp)) {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(NavyCard, NavySecondary)))
                    .padding(20.dp)
            ) {
                Column {
                    Text("موجودی فعلی", color = Color(0xFF9FB3C8), style = MaterialTheme.typography.bodyMedium)
                    Text(wallet.toman(), color = Color.White, style = MaterialTheme.typography.headlineSmall)
                }
            }
            Spacer(Modifier.height(16.dp))
            GradientButton(
                text = "افزایش موجودی",
                icon = Icons.Rounded.AddCard,
                onClick = { scope.launch { snack.showSnackbar("⚠ حالت دمو: درگاه پرداخت در نسخه اصلی فعال می‌شود") } },
                modifier = Modifier.fillMaxWidth()
            )
        }
        SnackbarHost(snack)
    }
}

@Composable
fun SupportScreen(onBack: () -> Unit) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        NivaHeader(title = "پشتیبانی", subtitle = "ارتباط با تیم پشتیبانی نیوا", onBack = onBack)
        Column(Modifier.padding(20.dp)) {
            listOf(
                Triple(Icons.Rounded.Email, "ایمیل", "support@niva-theme.ir"),
                Triple(Icons.Rounded.Language, "وب‌سایت", "niva-theme.ir"),
                Triple(Icons.Rounded.Schedule, "پاسخ‌گویی", "شنبه تا پنجشنبه، ۹ تا ۱۸")
            ).forEach { (ic, t, v) ->
                Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(ic, null, tint = Turquoise)
                    Spacer(Modifier.width(10.dp))
                    Text("$t: ", style = MaterialTheme.typography.titleMedium)
                    Text(v, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("موضوع") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("متن پیام") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(14.dp))
            Spacer(Modifier.height(12.dp))
            GradientButton(
                text = "ارسال تیکت", icon = Icons.Rounded.Send,
                onClick = {
                    if (subject.isNotBlank() && message.isNotBlank()) {
                        scope.launch { snack.showSnackbar("تیکت شما ثبت شد (دمو)"); subject = ""; message = "" }
                    } else scope.launch { snack.showSnackbar("موضوع و متن پیام را کامل کنید") }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        SnackbarHost(snack)
    }
}

@Composable
fun SecurityScreen(onBack: () -> Unit) {
    var twoFa by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        NivaHeader(title = "امنیت حساب", subtitle = "محافظت از اطلاعات و نشست‌ها", onBack = onBack)
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("ورود دومرحله‌ای", style = MaterialTheme.typography.titleMedium)
                    Text("تأیید ورود با کد پیامکی", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = twoFa, onCheckedChange = { twoFa = it })
            }
            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Lock, null, tint = Turquoise)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("رمز عبور و توکن‌ها", style = MaterialTheme.typography.titleMedium)
                    Text("توکن‌ها به‌صورت رمزنگاری‌شده در دستگاه ذخیره می‌شوند", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(vm: MiscViewModel = hiltViewModel(), main: MainViewModel = hiltViewModel(), onBack: () -> Unit) {
    val mode by main.themeMode.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        NivaHeader(title = "تنظیمات", subtitle = "شخصی‌سازی اپلیکیشن", onBack = onBack)
        Column(Modifier.padding(20.dp)) {
            Text("حالت نمایش", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            listOf("system" to "هماهنگ با سیستم", "light" to "روشن", "dark" to "تیره").forEach { (key, label) ->
                Row(Modifier.fillMaxWidth().clickable { main.setTheme(key) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = mode == key, onClick = { main.setTheme(key) })
                    Text(label, style = MaterialTheme.typography.bodyLarge)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            TextButton(onClick = {
                vm.clearLocal()
                scope.launch { snack.showSnackbar("داده‌های محلی پاک شد؛ لطفاً برنامه را مجدد باز کنید") }
            }) {
                Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(6.dp))
                Text("پاک کردن داده‌های محلی", color = MaterialTheme.colorScheme.error)
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text("نسخه اپلیکیشن: ۱.۰.۰ (دمو)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SnackbarHost(snack)
    }
}
