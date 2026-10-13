// app/src/main/java/ir/nivatheme/app/ui/features/products/ProductsScreen.kt
package ir.nivatheme.app.ui.features.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.nivatheme.app.core.theme.Turquoise
import ir.nivatheme.app.core.util.toFa
import ir.nivatheme.app.data.model.Product
import ir.nivatheme.app.data.repository.NivaRepository
import ir.nivatheme.app.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(private val repo: NivaRepository) : ViewModel() {
    enum class Sort(val label: String) {
        NEW("جدیدترین"), CHEAP("ارزان‌ترین"), EXPENSIVE("گران‌ترین"),
        RATING("محبوب‌ترین"), SALES("پرفروش‌ترین")
    }

    val query = MutableStateFlow("")
    val sort = MutableStateFlow(Sort.NEW)
    val cat = MutableStateFlow("all")
    val refreshing = MutableStateFlow(false)

    val cartCount = repo.cart.map { l -> l.sumOf { it.qty } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val favIds = repo.favorites.map { l -> l.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val items: StateFlow<List<Product>> = combine(query, sort, cat) { q, s, c ->
        repo.products()
            .filter { p -> (c == "all" || p.type.name == c) && (q.isBlank() || p.title.contains(q) || p.shortDesc.contains(q)) }
            .sorted { a, b ->
                when (s) {
                    Sort.NEW -> b.id.compareTo(a.id)
                    Sort.CHEAP -> a.price.compareTo(b.price)
                    Sort.EXPENSIVE -> b.price.compareTo(a.price)
                    Sort.RATING -> b.rating.compareTo(a.rating)
                    Sort.SALES -> b.sales.compareTo(a.sales)
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repo.products())

    fun refresh() = viewModelScope.launch { refreshing.value = true; delay(700); refreshing.value = false }
    fun toggleFav(id: Long) = viewModelScope.launch { repo.toggleFavorite(id) }
    fun addCart(id: Long) = viewModelScope.launch { repo.addToCart(id) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(vm: ProductsViewModel = hiltViewModel(), onProduct: (Long) -> Unit, onCart: () -> Unit) {
    val items by vm.items.collectAsStateWithLifecycle()
    val cartCount by vm.cartCount.collectAsStateWithLifecycle()
    val favIds by vm.favIds.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val sort by vm.sort.collectAsStateWithLifecycle()
    val cat by vm.cat.collectAsStateWithLifecycle()
    val refreshing by vm.refreshing.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            NivaHeader(
                title = "فروشگاه",
                subtitle = "افزونه‌ها و قالب‌های حرفه‌ای",
                actions = {
                    IconButton(onClick = onCart) {
                        BadgedBox(badge = { if (cartCount > 0) Badge { Text(cartCount.toFa()) } }) {
                            Icon(Icons.Rounded.ShoppingCart, "سبد خرید", tint = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                    IconButton(onClick = { scope.launch { snack.showSnackbar("اعلان جدیدی وجود ندارد") } }) {
                        Icon(Icons.Rounded.Notifications, "اعلان‌ها", tint = androidx.compose.ui.graphics.Color.White)
                    }
                }
            )
            OutlinedTextField(
                value = query, onValueChange = { vm.query.value = it },
                placeholder = { Text("جستجو در افزونه‌ها و قالب‌ها...") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                singleLine = true, shape = RoundedCornerShape(14.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ProductsViewModel.Sort.values().toList()) { s ->
                    FilterChip(
                        selected = sort == s,
                        onClick = { vm.sort.value = s },
                        label = { Text(s.label) }
                    )
                }
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("all" to "همه", "PLUGIN" to "افزونه وردپرس", "HTML" to "قالب HTML")) { (key, label) ->
                    FilterChip(selected = cat == key, onClick = { vm.cat.value = key }, label = { Text(label) })
                }
            }
            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = { vm.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (items.isEmpty()) {
                    EmptyState(Icons.Rounded.SearchOff, "محصولی پیدا نشد", "عبارت دیگری را جستجو کنید یا فیلترها را تغییر دهید")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(items) { p ->
                            ProductCard(
                                p, favIds.contains(p.id),
                                onFav = { vm.toggleFav(p.id) },
                                onAdd = { vm.addCart(p.id); scope.launch { snack.showSnackbar("به سبد خرید اضافه شد") } },
                                onClick = { onProduct(p.id) }
                            )
                        }
                    }
                }
            }
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter))
    }
}
