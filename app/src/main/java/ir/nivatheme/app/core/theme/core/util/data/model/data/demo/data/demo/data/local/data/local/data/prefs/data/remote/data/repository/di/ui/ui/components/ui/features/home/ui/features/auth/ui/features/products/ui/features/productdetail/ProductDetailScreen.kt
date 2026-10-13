// app/src/main/java/ir/nivatheme/app/ui/features/productdetail/ProductDetailScreen.kt
package ir.nivatheme.app.ui.features.productdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.nivatheme.app.core.theme.*
import ir.nivatheme.app.core.util.toman
import ir.nivatheme.app.core.util.toFa
import ir.nivatheme.app.data.demo.DemoData
import ir.nivatheme.app.data.repository.NivaRepository
import ir.nivatheme.app.ui.components.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repo: NivaRepository,
    saved: SavedStateHandle
) : ViewModel() {
    private val id = saved.get<String>("id")?.toLongOrNull() ?: 1L
    val product = repo.product(id)!!

    val fav = repo.favorites.map { l -> l.any { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleFav() = viewModelScope.launch { repo.toggleFavorite(id) }

    fun addToCart() = viewModelScope.launch { repo.addToCart(id) }

    fun buyNow(onGoCart: () -> Unit, onLogin: () -> Unit) = viewModelScope.launch {
        if (repo.session.isLoggedIn.first()) { repo.addToCart(id); onGoCart() } else onLogin()
    }
}

@Composable
fun ProductDetailScreen(
    vm: ProductDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCart: () -> Unit,
    onLogin: () -> Unit
) {
    val p = vm.product
    val fav by vm.fav.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("توضیحات", "نظرات", "ویژگی‌ها", "آموزش نصب")

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(NavyDark, NavySecondary)))) {
                Column(Modifier.statusBarsPadding().padding(bottom = 18.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "بازگشت", tint = Color.White) }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { vm.toggleFav() }) {
                            Icon(
                                if (fav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                "علاقه‌مندی", tint = if (fav) Color(0xFFE91E63) else Color.White
                            )
                        }
                    }
                    // کارت نمایش محصول
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Box {
                            ProductThumb(p.iconKey, Modifier.fillMaxWidth().height(150.dp), iconSize = 56.dp, corner = 18.dp)
                            if (p.oldPrice != null) {
                                Box(
                                    Modifier.align(Alignment.TopStart).padding(8.dp)
                                        .clip(RoundedCornerShape(8.dp)).background(NavyDark)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "٪${(((p.oldPrice - p.price) * 100) / p.oldPrice).toInt().toFa()} تخفیف ویژه",
                                        color = Cyan, style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            Box(
                                Modifier.align(Alignment.BottomEnd).padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp)).background(SuccessGreen)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) { Text("PRO", color = Color.White, style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(p.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(horizontal = 16.dp))
                    Text(
                        p.shortDesc, color = Color(0xFF9FB3C8),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StarRating(p.rating, 16.dp)
                        Text("  ${p.rating.toFa()} (${p.reviews.toFa()} نظر)", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Rounded.Download, null, tint = Cyan, modifier = Modifier.size(16.dp))
                        Text(" ${p.sales.toFa()} فروش", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Rounded.Verified, null, tint = Cyan, modifier = Modifier.size(16.dp))
                        Text(" به‌روزرسانی ${p.updated}", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // شیت سفید
            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column {
                    TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                        tabs.forEachIndexed { i, t ->
                            Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) })
                        }
                    }
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)
                    ) {
                        when (tab) {
                            0 -> {
                                Text(p.description, style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.height(16.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(
                                        Triple(Icons.Rounded.Verified, "سازگار با", "وردپرس ${p.wpVersion}"),
                                        Triple(Icons.Rounded.Settings, "تنظیمات", "انعطاف‌پذیر"),
                                        Triple(Icons.Rounded.Brush, "طراحی", "ریسپانسیو")
                                    ).forEach { (ic, t1, t2) ->
                                        Column(
                                            Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.surface)
                                                .padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(ic, null, tint = Turquoise)
                                            Spacer(Modifier.height(4.dp))
                                            Text(t1, style = MaterialTheme.typography.labelSmall)
                                            Text(t2, style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Card(shape = RoundedCornerShape(16.dp)) {
                                    Column(Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("${p.rating.toFa()}", color = Turquoise, style = MaterialTheme.typography.headlineSmall)
                                            Spacer(Modifier.width(8.dp))
                                            StarRating(p.rating, 15.dp)
                                            Spacer(Modifier.weight(1f))
                                            Text(
                                                "امتیاز کاربران از ${p.reviews.toFa()} نظر",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        listOf(5 to 70, 4 to 18, 3 to 8, 2 to 3, 1 to 1).forEach { (star, pct) ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("${star.toFa()} ستاره", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(52.dp))
                                                Box(
                                                    Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                ) {
                                                    Box(
                                                        Modifier.fillMaxWidth(pct / 100f).fillMaxHeight()
                                                            .clip(RoundedCornerShape(4.dp)).background(Turquoise)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                            1 -> DemoData.reviews.forEach { r ->
                                Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                                    Column(Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(34.dp).clip(CircleShape).background(Turquoise), contentAlignment = Alignment.Center) {
                                                Text(r.name.first().toString(), color = Color.White)
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Text(r.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                            StarRating(r.rating.toDouble(), 12.dp)
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text(r.text, style = MaterialTheme.typography.bodyMedium)
                                        Text(r.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            2 -> p.features.forEach {
                                Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(it, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            else -> listOf(
                                "فایل افزونه را از بخش «دانلودهای من» دریافت کنید",
                                "در پیشخوان وردپرس به افزونه‌ها > افزودن بروید",
                                "فایل ZIP را آپلود و نصب کنید",
                                "افزونه را فعال کرده و لایسنس را وارد کنید",
                                "از تنظیمات افزونه، پیکربندی مورد نظر را انجام دهید"
                            ).forEachIndexed { i, s ->
                                Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(26.dp).clip(CircleShape).background(Turquoise), contentAlignment = Alignment.Center) {
                                        Text("${(i + 1).toFa()}", color = Color.White, style = MaterialTheme.typography.labelMedium)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(s, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                        Spacer(Modifier.height(90.dp))
                    }
                }
            }
        }

        // نوار خرید چسبان (Sticky)
        Surface(
            shadowElevation = 14.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    if (p.oldPrice != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                p.oldPrice!!.toman(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(Modifier.clip(RoundedCornerShape(6.dp)).background(WarningOrange.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("تخفیف ویژه", color = WarningOrange, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Text(p.price.toman(), color = Turquoise, style = MaterialTheme.typography.titleLarge)
                    Text("قیمت نهایی", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { vm.addToCart(); scope.launch { snack.showSnackbar("به سبد خرید اضافه شد") } },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Icon(Icons.Rounded.ShoppingCart, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("افزودن به سبد")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { vm.buyNow(onGoCart = onCart, onLogin = onLogin) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Icon(Icons.Rounded.Bolt, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("خرید فوری")
                }
            }
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
    }
}
