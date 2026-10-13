// app/src/main/java/ir/nivatheme/app/ui/features/auth/AuthScreen.kt
package ir.nivatheme.app.ui.features.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.nivatheme.app.core.theme.*
import ir.nivatheme.app.core.util.isValidIranMobile
import ir.nivatheme.app.core.util.toFa
import ir.nivatheme.app.data.prefs.SessionStore
import ir.nivatheme.app.ui.components.GradientButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val session: SessionStore) : ViewModel() {
    var isRegister by mutableStateOf(false)
    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var code by mutableStateOf("")
    var codeSent by mutableStateOf(false)
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var secondsLeft by mutableIntStateOf(0)

    fun sendCode() {
        error = null
        if (!phone.isValidIranMobile()) { error = "شماره موبایل معتبر نیست (مثال: 09123456789)"; return }
        loading = true
        viewModelScope.launch {
            delay(700) // در Production: فراخوانی POST /niva/v1/auth/request-otp
            loading = false
            codeSent = true
            secondsLeft = 120
            startTimer()
        }
    }

    private fun startTimer() = viewModelScope.launch {
        while (secondsLeft > 0) { delay(1000); secondsLeft-- }
    }

    fun login(onDone: () -> Unit) {
        error = null
        if (code.length != 6) { error = "کد تأیید باید ۶ رقم باشد"; return }
        loading = true
        viewModelScope.launch {
            delay(700) // در Production: POST /niva/v1/auth/verify-otp → دریافت توکن JWT از سرور
            session.login(phone, name)
            loading = false
            onDone()
        }
    }
}

@Composable
fun AuthScreen(vm: AuthViewModel = hiltViewModel(), onDone: () -> Unit) {
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(NavyDark, NavySecondary)))) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .statusBarsPadding().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Turquoise, Cyan))),
                contentAlignment = Alignment.Center
            ) { Text("N", color = Color.White, style = MaterialTheme.typography.headlineSmall) }
            Spacer(Modifier.height(14.dp))
            Text("خوش آمدید", color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Text(
                "برای ورود یا ثبت‌نام، شماره موبایل خود را وارد کنید",
                color = Color(0xFF9FB3C8), style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        FilterChip(
                            selected = !vm.isRegister, onClick = { vm.isRegister = false },
                            label = { Text("ورود") }, modifier = Modifier.padding(end = 8.dp)
                        )
                        FilterChip(selected = vm.isRegister, onClick = { vm.isRegister = true }, label = { Text("ثبت‌نام") })
                    }
                    Spacer(Modifier.height(16.dp))

                    if (vm.isRegister) {
                        OutlinedTextField(
                            value = vm.name, onValueChange = { vm.name = it },
                            label = { Text("نام و نام خانوادگی") },
                            leadingIcon = { Icon(Icons.Rounded.Person, null) },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = vm.phone, onValueChange = { vm.phone = it.filter { c -> c.isDigit() }.take(11) },
                        label = { Text("شماره موبایل") },
                        placeholder = { Text("09XXXXXXXXX") },
                        leadingIcon = { Icon(Icons.Rounded.Smartphone, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(12.dp))

                    AnimatedVisibility(visible = vm.codeSent) {
                        Column {
                            OutlinedTextField(
                                value = vm.code, onValueChange = { vm.code = it.filter { c -> c.isDigit() }.take(6) },
                                label = { Text("کد تأیید (OTP)") },
                                placeholder = { Text("------") },
                                leadingIcon = { Icon(Icons.Rounded.Lock, null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )
                            Row(
                                Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (vm.secondsLeft > 0) "ارسال مجدد تا ${vm.secondsLeft.toFa()} ثانیه"
                                    else "می‌توانید کد را دوباره بفرستید",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    vm.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(16.dp))

                    if (!vm.codeSent) {
                        GradientButton(
                            text = if (vm.loading) "در حال ارسال..." else "ارسال کد تأیید",
                            onClick = { vm.sendCode() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !vm.loading
                        )
                    } else {
                        GradientButton(
                            text = if (vm.loading) "در حال ورود..." else "ورود",
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            onClick = { vm.login(onDone) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !vm.loading
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { vm.sendCode() },
                            enabled = vm.secondsLeft == 0 && !vm.loading,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("ارسال مجدد کد") }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Lock, null, tint = Turquoise, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "ارتباط امن و رمزنگاری شده • اطلاعات شما نزد ما محفوظ است",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    // ⚠️ برچسب شفاف حالت دمو — در Production حذف می‌شود
                    Text(
                        "⚠ حالت دمو: پیامکی ارسال نمی‌شود؛ هر کد ۶ رقمی را وارد کنید.",
                        style = MaterialTheme.typography.labelMedium,
                        color = WarningOrange
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
