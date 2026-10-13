// app/src/main/java/ir/nivatheme/app/ui/features/courses/CoursesScreen.kt
package ir.nivatheme.app.ui.features.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.nivatheme.app.core.theme.*
import ir.nivatheme.app.core.util.toman
import ir.nivatheme.app.core.util.toFa
import ir.nivatheme.app.data.repository.NivaRepository
import ir.nivatheme.app.ui.components.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoursesViewModel @Inject constructor(private val repo: NivaRepository) : ViewModel() {
    val courses = repo.courses()
    val featured = courses.filter { it.featured || it.isNew }
    val enrolled = repo.session.enrolled
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet<Long>())
    fun enroll(id: Long) = viewModelScope.launch { repo.enroll(id) }
}

@Composable
fun CoursesScreen(vm: CoursesViewModel = hiltViewModel(), onCourse: (Long) -> Unit) {
    val enrolled by vm.enrolled.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            NivaHeader(
                title = "بخش آموزش",
                subtitle = "دوره‌های تخصصی طراحی و توسعه",
                actions = {
                    IconButton(onClick = { scope.launch { snack.showSnackbar("اعلان جدیدی وجود ندارد") } }) {
                        Icon(Icons.Rounded.Notifications, null, tint = Color.White)
                    }
                }
            )
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                item { SectionHeader("دوره‌های ویژه") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(vm.featured) { c ->
                            Card(
                                onClick = { onCourse(c.id) },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = NavySecondary),
                                modifier = Modifier.width(250.dp)
                            ) {
                                Column {
                                    Box {
                                        ProductThumb(c.iconKey, Modifier.fillMaxWidth().height(120.dp), iconSize = 40.dp, corner = 18.dp)
                                        Box(
                                            Modifier.align(Alignment.TopEnd).padding(8.dp)
                                                .clip(RoundedCornerShape(8.dp)).background(Turquoise)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) { Text("جدید", color = Color.White, style = MaterialTheme.typography.labelSmall) }
                                        Box(
                                            Modifier.align(Alignment.BottomStart).padding(10.dp)
                                                .size(38.dp).clip(CircleShape).background(Turquoise),
                                            contentAlignment = Alignment.Center
                                        ) { Icon(Icons.Rounded.PlayArrow, null, tint = Color.White) }
                                    }
                                    Column(Modifier.padding(12.dp)) {
                                        Text(c.title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                                        Text("مدرس: ${c.instructor}", color = Color(0xFF9FB3C8), style = MaterialTheme.typography.labelMedium)
                                        Spacer(Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Schedule, null, tint = Cyan, modifier = Modifier.size(14.dp))
                                            Text(" ${c.hours.toFa()} ساعت", color = Color.White, style = MaterialTheme.typography.labelMedium)
                                            Spacer(Modifier.weight(1f))
                                            Text(
                                                if (c.price == null) "رایگان" else c.price!!.toman(),
                                                color = if (c.price == null) SuccessGreen else Cyan,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { SectionHeader("همه دوره‌ها") }
                items(vm.courses) { c ->
                    val isEnrolled = enrolled.contains(c.id)
                    Card(
                        onClick = { onCourse(c.id) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(c.title, style = MaterialTheme.typography.titleMedium)
                                Text("مدرس: ${c.instructor}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (isEnrolled && c.progressPercent > 0) {
                                    Spacer(Modifier.height(6.dp))
                                    Text("٪${c.progressPercent.toFa()} تکمیل شده", style = MaterialTheme.typography.labelSmall, color = Turquoise)
                                    LinearProgressIndicator(
                                        progress = { c.progressPercent / 100f },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(" ${c.hours.toFa()} ساعت", style = MaterialTheme.typography.labelMedium)
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        if (c.price == null) "رایگان" else c.price!!.toman(),
                                        color = if (c.price == null) SuccessGreen else Turquoise,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            ProductThumb(c.iconKey, Modifier.size(70.dp), iconSize = 30.dp)
                        }
                    }
                }
            }
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun CourseDetailScreen(vm: CoursesViewModel = hiltViewModel(), courseId: Long, onBack: () -> Unit) {
    val c = vm.courses.first { it.id == courseId }
    val enrolled by vm.enrolled.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    var showEnroll by remember { mutableStateOf(false) }
    val isEnrolled = enrolled.contains(c.id)

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(NavyDark, NavySecondary)))) {
                Column(Modifier.statusBarsPadding().padding(bottom = 16.dp)) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "بازگشت", tint = Color.White) }
                    ProductThumb(c.iconKey, Modifier.fillMaxWidth().height(140.dp).padding(horizontal = 16.dp), iconSize = 48.dp, corner = 18.dp)
                    Text(c.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
                    Text("مدرس: ${c.instructor}", color = Color(0xFF9FB3C8), modifier = Modifier.padding(horizontal = 16.dp))
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Schedule, null, tint = Cyan, modifier = Modifier.size(16.dp))
                        Text(" ${c.hours.toFa()} ساعت", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.width(16.dp))
                        Icon(Icons.Rounded.PlayCircle, null, tint = Cyan, modifier = Modifier.size(16.dp))
                        Text(" ${c.lessons.size.toFa()} درس", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.weight(1f))
                        Text(
                            if (c.price == null) "رایگان" else c.price!!.toman(),
                            color = if (c.price == null) SuccessGreen else Cyan,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    item {
                        Text(c.description, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(14.dp))
                        Text("سرفصل‌های دوره", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                    }
                    items(c.lessons) { l ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.PlayArrow, null, tint = Turquoise, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(l.title, style = MaterialTheme.typography.bodyLarge)
                                Text(l.duration, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (l.free) {
                                Box(Modifier.clip(RoundedCornerShape(6.dp)).background(SuccessGreen.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                    Text("پیش‌نمایش", color = SuccessGreen, style = MaterialTheme.typography.labelSmall)
                                }
                            } else if (!isEnrolled) {
                                Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
        // نوار پایین چسبان
        Surface(shadowElevation = 14.dp, color = MaterialTheme.colorScheme.surface, modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (c.price == null) "دسترسی رایگان" else c.price!!.toman(),
                        color = Turquoise, style = MaterialTheme.typography.titleLarge
                    )
                }
                Button(
                    onClick = {
                        if (isEnrolled) scope.launch { snack.showSnackbar("در حال بارگذاری ویدئو... (در نسخه دمو ویدئو متصل نیست)") }
                        else showEnroll = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Icon(if (isEnrolled) Icons.Rounded.PlayArrow else Icons.Rounded.School, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isEnrolled) "ادامه آموزش" else if (c.price == null) "ثبت‌نام رایگان" else "ثبت‌نام در دوره")
                }
            }
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
    }

    if (showEnroll) {
        AlertDialog(
            onDismissRequest = { showEnroll = false },
            title = { Text("ثبت‌نام در دوره") },
            text = { Text("⚠ حالت دمو: پرداخت واقعی انجام نمی‌شود و دوره به «دوره‌های من» شما اضافه می‌گردد. در نسخه اصلی، پرداخت از طریق درگاه سایت انجام می‌شود.") },
            confirmButton = {
                TextButton(onClick = {
                    showEnroll = false
                    scope.launch { vm.enroll(c.id); snack.showSnackbar("ثبت‌نام انجام شد") }
                }) { Text("تأیید") }
            },
            dismissButton = { TextButton(onClick = { showEnroll = false }) { Text("انصراف") } }
        )
    }
}
