
// app/src/main/java/ir/nivatheme/app/ui/features/home/HomeScreen.kt
package ir.nivatheme.app.ui.features.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.nivatheme.app.core.theme.*
import ir.nivatheme.app.core.util.toFa
import ir.nivatheme.app.data.repository.NivaRepository
import ir.nivatheme.app.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(repo: NivaRepository) : ViewModel() {
    private val all = repo.products()
    val best = all.filter { it.isFeatured || it.isBestSeller }.take(4)
    val newest = all.sortedByDescending { it.id }
    val popular = all.sortedByDescending { it.rating }
    val cartCount = repo.cart.map { l -> l.sumOf { it.qty } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val favIds = repo.favorites.map { l -> l.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    fun toggleFav(id: Long) = viewModelScope.launch { repo.toggleFavorite(id) }
    fun addCart(id: Long) = viewModelScope.launch { repo.addToCart(id) }
}

@Composable
fun HomeScreen(
    vm: HomeViewModel = hiltViewModel(),
    onProduct: (Long) -> Unit,
    onSeeAll: () -> Unit,
    onCart: () -> Unit,
    onCourses: () -> Unit
) {
    val cartCount by vm.cartCount.collectAsStateWithLifecycle()
    val favIds by vm.favIds.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // ── هدر سرمه‌ای + بنر ──
            Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(NavyDark, NavySecondary)))) {
                Column(Modifier.statusBarsPadding().padding(bottom = 16.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Rounded.ShoppingBag, null, tint = Cyan, modifier = Modifier.size(26.dp)) }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("فروشگاه دیجیتال نیوا", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text("افزونه و قالب حرفه‌ای", color = Color(0xFF9FB3C8), style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = onSeeAll) { Icon(Icons.Rounded.Search, "جستجو", tint = Color.White) }
                        IconButton(onClick = onCart) {
                            BadgedBox(badge = {
                                if (cartCount > 0) Badge { Text(cartCount.toFa()) }
                            }) { Icon(Icons.Rounded.ShoppingCart, "سبد خرید", tint = Color.White) }
                        }
                    }
                    BannerSlider(onCta = onSeeAll)
                }
            }
            // ── شیت سفید محتوا ──
            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn {
                    item { CategoriesRow(onSeeAll, onCourses) }
                    item { SectionHeader("محبوب‌ترین پیشنهادی", onSeeAll) }
                    items(vm.best.chunked(2)) { row ->
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            row.forEach { p ->
                                ProductCard(
                                    p, favIds.contains(p.id),
                                    onFav = { vm.toggleFav(p.id) },
                                    onAdd = { vm.addCart(p.id); scope.launch { snack.showSnackbar("به سبد خرید اضافه شد") } },
                                    onClick = { onProduct(p.id) },
                                    modifier = Modifier.weight(1f)
                                )
                                if (row.indexOf(p) == 0) Spacer(Modifier.width(10.dp))
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    item { SectionHeader("جدیدترین محصولات", onSeeAll) }
                    item {
                        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(vm.newest) { p ->
                                ProductCard(
                                    p, favIds.contains(p.id),
                                    onFav = { vm.toggleFav(p.id) },
                                    onAdd = { vm.addCart(p.id); scope.launch { snack.showSnackbar("به سبد خرید اضافه شد") } },
                                    onClick = { onProduct(p.id) },
                                    modifier = Modifier.width(200.dp)
                                )
                            }
                        }
                    }
                    item { SectionHeader("پرفروش‌ترین محصولات", onSeeAll) }
                    item {
                        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(vm.popular) { p ->
                                ProductCard(
                                    p, favIds.contains(p.id),
                                    onFav = { vm.toggleFav(p.id) },
                                    onAdd = { vm.addCart(p.id); scope.launch { snack.showSnackbar("به سبد خرید اضافه شد") } },
                                    onClick = { onProduct(p.id) },
                                    modifier = Modifier.width(200.dp)
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(20.dp)) }
                }
            }
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun BannerSlider(onCta: () -> Unit) {
    val state = rememberPagerState { 3 }
    LaunchedEffect(Unit) {
        while (true) { delay(4000); state.animateScrollToPage((state.currentPage + 1) % 3) }
    }
    Column {
        HorizontalPager(
            state = state,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) { page -> BannerCard(page, onCta) }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            repeat(3) { i ->
                val size by animateDpAsState(if (i == state.currentPage) 18.dp else 6.dp, label = "dot")
                Box(
                    Modifier.padding(3.dp).size(size, 6.dp)
                        .clip(CircleShape)
                        .background(if (i == state.currentPage) Turquoise else Color(0x55FFFFFF))
                )
            }
        }
    }
}

@Composable
private fun BannerCard(page: Int, onCta: () -> Unit) {
    val titles = listOf(
        "بهترین افزونه‌ها و قالب‌های وردپرس" to "کیفیت حرفه‌ای، نصب آسان، پشتیبانی سریع",
        "تخفیف ویژه محصولات منتخب" to "تا ٪۲۵ تخفیف روی افزونه‌های حرفه‌ای",
        "دوره‌های آموزشی تخصصی" to "وردپرس، طراحی وب و برنامه‌نویسی را حرفه‌ای یاد بگیر"
    )
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(NavyCard, NavySecondary)))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(titles[page].first, color = Color.White, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(titles[page].second, color = Color(0xFF9FB3C8), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier.clip(RoundedCornerShape(10.dp)).background(Turquoise)
                    .clickable(onClick = onCta).padding(horizontal = 14.dp, vertical = 8.dp)
            ) { Text("مشاهده محصولات", color = Color.White, style = MaterialTheme.typography.labelLarge) }
        }
        Box(
            Modifier.size(74.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Turquoise, Cyan))),
            contentAlignment = Alignment.Center
        ) { Text("W", color = Color.White, style = MaterialTheme.typography.headlineSmall) }
    }
}

@Composable
private fun CategoriesRow(onStore: () -> Unit, onCourses: () -> Unit) {
    data class Cat(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val teal: Boolean, val go: () -> Unit)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf(
            Cat("افزونه وردپرس", Icons.Rounded.Widget, true, onStore),
            Cat("قالب HTML", Icons.Rounded.Code, false, onStore),
            Cat("آموزش", Icons.Rounded.School, false, onCourses),
            Cat("محبوب‌ترین‌ها", Icons.Rounded.Star, false, onStore)
        ).forEach { c ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = c.go)) {
                Box(
                    Modifier.size(58.dp).clip(RoundedCornerShape(16.dp))
                        .background(if (c.teal) Turquoise else MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(c.icon, c.label, tint = if (c.teal) Color.White else Turquoise, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text(c.label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
