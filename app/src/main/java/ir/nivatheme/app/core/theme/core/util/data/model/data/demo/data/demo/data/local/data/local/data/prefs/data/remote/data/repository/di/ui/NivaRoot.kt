// app/src/main/java/ir/nivatheme/app/ui/NivaRoot.kt
package ir.nivatheme.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.nivatheme.app.core.theme.NivaTheme
import ir.nivatheme.app.data.prefs.SessionStore
import ir.nivatheme.app.ui.features.auth.AuthScreen
import ir.nivatheme.app.ui.features.cart.CartScreen
import ir.nivatheme.app.ui.features.courses.CourseDetailScreen
import ir.nivatheme.app.ui.features.courses.CoursesScreen
import ir.nivatheme.app.ui.features.downloads.DownloadsScreen
import ir.nivatheme.app.ui.features.home.HomeScreen
import ir.nivatheme.app.ui.features.misc.*
import ir.nivatheme.app.ui.features.productdetail.ProductDetailScreen
import ir.nivatheme.app.ui.features.products.ProductsScreen
import ir.nivatheme.app.ui.features.profile.ProfileScreen
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val session: SessionStore) : ViewModel() {
    val themeMode = session.themeMode.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, "system")
    fun setTheme(mode: String) = viewModelScope.launch { session.setTheme(mode) }
}

sealed class Dest(val route: String) {
    data object Home : Dest("home")
    data object Store : Dest("store")
    data object Courses : Dest("courses")
    data object Downloads : Dest("downloads")
    data object Profile : Dest("profile")
    data object Auth : Dest("auth")
    data object Cart : Dest("cart")
    data object Favorites : Dest("favorites")
    data object Orders : Dest("orders")
    data object Wallet : Dest("wallet")
    data object Support : Dest("support")
    data object Settings : Dest("settings")
    data object Security : Dest("security")
    data object ProductDetail : Dest("product/{id}") { fun of(id: Long) = "product/$id" }
    data object CourseDetail : Dest("course/{id}") { fun of(id: Long) = "course/$id" }
}

@Composable
fun NivaRoot(main: MainViewModel = hiltViewModel()) {
    val mode by main.themeMode.collectAsStateWithLifecycle()
    val dark = when (mode) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    NivaTheme(darkTheme = dark) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            NivaAppScaffold()
        }
    }
}

private data class BottomItem(val route: String, val icon: ImageVector, val label: String)

@Composable
fun NivaAppScaffold() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val bottomItems = listOf(
        BottomItem(Dest.Home.route, Icons.Rounded.Home, "خانه"),
        BottomItem(Dest.Store.route, Icons.Rounded.Storefront, "فروشگاه"),
        BottomItem(Dest.Courses.route, Icons.Rounded.MenuBook, "دوره‌ها"),
        BottomItem(Dest.Downloads.route, Icons.Rounded.Download, "دانلودها"),
        BottomItem(Dest.Profile.route, Icons.Rounded.Person, "پروفایل")
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomItems.map { it.route }) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                nav.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(Dest.Home.route) { saveState = true; inclusive = false }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = Dest.Home.route, modifier = Modifier.padding(padding)) {
            composable(Dest.Home.route) {
                HomeScreen(
                    onProduct = { nav.navigate(Dest.ProductDetail.of(it)) },
                    onSeeAll = { nav.navigate(Dest.Store.route) },
                    onCart = { nav.navigate(Dest.Cart.route) },
                    onCourses = { nav.navigate(Dest.Courses.route) }
                )
            }
            composable(Dest.Store.route) {
                ProductsScreen(
                    onProduct = { nav.navigate(Dest.ProductDetail.of(it)) },
                    onCart = { nav.navigate(Dest.Cart.route) }
                )
            }
            composable(Dest.Courses.route) {
                CoursesScreen(onCourse = { nav.navigate(Dest.CourseDetail.of(it)) })
            }
            composable(Dest.Downloads.route) {
                DownloadsScreen(onLogin = { nav.navigate(Dest.Auth.route) })
            }
            composable(Dest.Profile.route) {
                ProfileScreen(
                    go = { route -> nav.navigate(route) },
                    onDownloads = { nav.navigate(Dest.Downloads.route) },
                    onCourses = { nav.navigate(Dest.Courses.route) }
                )
            }
            composable(Dest.Auth.route) { AuthScreen(onDone = { nav.popBackStack() }) }
            composable(Dest.Cart.route) {
                CartScreen(
                    onBack = { nav.popBackStack() },
                    onLogin = { nav.navigate(Dest.Auth.route) },
                    onOrders = { nav.navigate(Dest.Orders.route) { popUpTo(Dest.Home.route) } },
                    onDownloads = { nav.navigate(Dest.Downloads.route) { popUpTo(Dest.Home.route) } }
                )
            }
            composable("product/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 1L
                ProductDetailScreen(
                    productId = id,
                    onBack = { nav.popBackStack() },
                    onCart = { nav.navigate(Dest.Cart.route) },
                    onLogin = { nav.navigate(Dest.Auth.route) }
                )
            }
            composable("course/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 101L
                CourseDetailScreen(courseId = id, onBack = { nav.popBackStack() })
            }
            composable(Dest.Favorites.route) {
                FavoritesScreen(onBack = { nav.popBackStack() }, onProduct = { nav.navigate(Dest.ProductDetail.of(it)) })
            }
            composable(Dest.Orders.route) { OrdersScreen(onBack = { nav.popBackStack() }) }
            composable(Dest.Wallet.route) { WalletScreen(onBack = { nav.popBackStack() }) }
            composable(Dest.Support.route) { SupportScreen(onBack = { nav.popBackStack() }) }
            composable(Dest.Settings.route) { SettingsScreen(onBack = { nav.popBackStack() }) }
            composable(Dest.Security.route) { SecurityScreen(onBack = { nav.popBackStack() }) }
        }
    }
}
