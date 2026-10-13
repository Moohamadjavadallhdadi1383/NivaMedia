
// app/src/main/java/ir/nivatheme/app/ui/features/downloads/DownloadsScreen.kt
package ir.nivatheme.app.ui.features.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.nivatheme.app.core.theme.*
import ir.nivatheme.app.core.util.toFa
import ir.nivatheme.app.data.local.DownloadEntity
import ir.nivatheme.app.data.repository.NivaRepository
import ir.nivatheme.app.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(repo: NivaRepository) : ViewModel() {
    init { viewModelScope.launch { repo.ensureSeed() } }
    val downloads = repo.downloads.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun DownloadsScreen(vm: DownloadsViewModel = hiltViewModel(), onLogin: () -> Unit) {
    val items by vm.downloads.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    var resetKey by remember { mutableIntStateOf(0) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            NivaHeader(title = "دانلودهای من", subtitle = "محصولات خریداری‌شده شما")
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Turquoise.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = SuccessGreen)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "شما ${items.size.toFa()} محصول خریداری‌شده دارید که می‌توانید دانلود کنید.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                if (items.isEmpty()) {
                    item { EmptyState(Icons.Rounded.Download, "فایلی برای دانلود نیست", "پس از خرید، محصولات اینجا فعال می‌شوند") }
                }
                items(items, key = { it.id }) { d ->
                    DownloadCard(d, resetKey, onLogin = onLogin, onSnack = { msg -> scope.launch { snack.showSnackbar(msg) } })
                    Spacer(Modifier.height(10.dp))
                }
                item {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("دانلود مجدد", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "در صورت نیاز به دریافت دوباره فایل‌های خریداری‌شده، از این بخش استفاده کنید.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Rounded.Refresh, null, tint = Turquoise, modifier = Modifier.size(28.dp))
                        }
                        Button(
                            onClick = { resetKey++; scope.launch { snack.showSnackbar("همه لینک‌ها بازنشانی شد (دمو)") } },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(bottom = 14.dp)
                        ) { Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("دانلود مجدد") }
                    }
                }
            }
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun DownloadCard(d: DownloadEntity, resetKey: Int, onLogin: () -> Unit, onSnack: (String) -> Unit) {
    var progress by remember(d.id, resetKey) { mutableStateOf(-1) } // -1 = آماده
    val scope = rememberCoroutineScope()
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                ProductThumb(d.iconKey, Modifier.size(56.dp), iconSize = 26.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(d.title, style = MaterialTheme.typography.titleMedium)
                    Box(
                        Modifier.clip(RoundedCornerShape(6.dp))
                            .background(if (d.active) SuccessGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (d.active) "فعال" else "منقضی",
                            color = if (d.active) SuccessGreen else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CalendarMonth, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(" انقضا: ${d.expiry ?: "نامحدود"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(14.dp))
                Icon(Icons.Rounded.History, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(" نسخه: ${d.version}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    scope.launch {
                        for (p in 1..100 step 4) { progress = p; delay(50) }
                        onSnack("دانلود «${d.title}» کامل شد (دمو)")
                        progress = -1
                    }
                },
                enabled = progress < 0,
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                when {
                    progress < 0 -> {
                        Icon(Icons.Rounded.Download, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp)); Text("دانلود فایل")
                    }
                    progress <= 100 -> {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = androidx.compose.ui.graphics.Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp)); Text("در حال دانلود ${progress.toFa()}٪")
                    }
                }
            }
        }
    }
}
