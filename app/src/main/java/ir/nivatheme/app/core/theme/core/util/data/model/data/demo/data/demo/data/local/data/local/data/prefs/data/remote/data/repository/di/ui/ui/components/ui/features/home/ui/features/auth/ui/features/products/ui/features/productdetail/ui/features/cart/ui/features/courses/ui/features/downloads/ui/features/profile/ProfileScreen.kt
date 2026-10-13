// app/src/main/java/ir/nivatheme/app/ui/features/profile/ProfileScreen.kt
package ir.nivatheme.app.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.nivatheme.app.core.theme.*
import ir.nivatheme.app.core.util.toman
import ir.nivatheme.app.core.util.toFa
import ir.nivatheme.app.data.prefs.SessionStore
import ir.nivatheme.app.ui.Dest
import ir.nivatheme.app.ui.components.NivaHeader
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val session: SessionStore) : ViewModel() {
    val isLoggedIn = session.isLoggedIn.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val userName = session.userName.stateIn(viewModelScope, SharingStarted.Eagerly, "کاربر نیوا")
    val wallet = session.wallet.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)
    fun logout() = viewModelScope.launch { session.logout() }
}

@Composable
fun ProfileScreen(vm: ProfileViewModel = hiltViewModel(), go: (String) -> Unit, onDownloads: () -> Unit, onCourses: () -> Unit) {
    val isLoggedIn by vm.isLoggedIn.collectAsStateWithLifecycle()
    val name by vm.userName.collectAsStateWithLifecycle()
    val wallet by vm.wallet.collectAsStateWithLifecycle()
    var showLogout by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            NivaHeader(
                title = if (isLoggedIn) name else "کاربر مهمان",
                subtitle = if (isLoggedIn) "خوش‌آمدید! دوباره شما را می‌بینیم." else "برای استفاده از امکانات، وارد شوید",
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Rounded.Notifications, null, tint = Color.White) }
                }
            )
            if (!isLoggedIn) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().padding(20.dp)
                ) {
                    Column(
                        Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.AccountCircle, null, modifier = Modifier.size(64.dp), tint = Turquoise)
                        Spacer(Modifier.height(10.dp))
                        Text("وارد حساب کاربری شوید", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "برای خرید، دانلودها و دوره‌های شما",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(onClick = { go(Dest.Auth.route) }, shape = RoundedCornerShape(14.dp)) {
                            Text("ورود / ثبت‌نام")
                        }
                    }
                }
            } else {
                // کارت عضویت ویژه
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(NavyCard, NavySecondary)))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.WorkspacePremium, null, tint = StarGold, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("عضو ویژه", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text("تا ۱۴۰۴/۰۶/۳۱", color = Color(0xFF9FB3C8), style = MaterialTheme.typography.labelMedium)
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Rounded.Star, null, tint = StarGold, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.height(14.dp))
                MenuItem(Icons.Rounded.ShoppingBag, "سفارش‌های من", "مشاهده تاریخچه سفارشات و وضعیت آن‌ها") { go(Dest.Orders.route) }
                MenuItem(Icons.Rounded.Download, "دانلودهای من", "دوره‌ها و فایل‌های خریداری شده") { onDownloads() }
                MenuItem(Icons.Rounded.MenuBook, "دوره‌های من", "دوره‌های فعال و در حال یادگیری شما") { onCourses() }
                MenuItem(Icons.Rounded.Favorite, "علاقه‌مندی‌ها", "دوره‌ها و محصولات مورد علاقه شما") { go(Dest.Favorites.route) }
                MenuItem(
                    Icons.Rounded.Wallet, "کیف پول", "موجودی، تراکنش‌ها و شارژ حساب",
                    trailing = { Text(wallet.toman(), color = Turquoise, style = MaterialTheme.typography.labelLarge) }
                ) { go(Dest.Wallet.route) }
                MenuItem(Icons.Rounded.SupportAgent, "پشتیبانی", "ارتباط با تیم پشتیبانی و دریافت کمک") { go(Dest.Support.route) }
                MenuItem(Icons.Rounded.Settings, "تنظیمات", "مدیریت حساب کاربری و تنظیمات اپلیکیشن") { go(Dest.Settings.route) }
                MenuItem(Icons.Rounded.Shield, "امنیت حساب", "اطلاعات شما محفوظ و امن است") { go(Dest.Security.route) }
                MenuItem(Icons.Rounded.Logout, "خروج از حساب", "پایان نشست فعلی", tint = MaterialTheme.colorScheme.error) { showLogout = true }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("خروج از حساب") },
            text = { Text("آیا مطمئنید که می‌خواهید خارج شوید؟") },
            confirmButton = { TextButton(onClick = { showLogout = false; vm.logout() }) { Text("خروج", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showLogout = false }) { Text("انصراف") } }
        )
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector, title: String, subtitle: String,
    tint: Color = Turquoise,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (trailing != null) { Spacer(Modifier.width(8.dp)); trailing() }
            }
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.outline)
    }
}
