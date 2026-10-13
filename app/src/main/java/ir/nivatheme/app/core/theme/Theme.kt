// app/src/main/java/ir/nivatheme/app/core/theme/Theme.kt
package ir.nivatheme.app.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val NavyDark = Color(0xFF061B35)
val NavySecondary = Color(0xFF0B2849)
val NavyCard = Color(0xFF10345C)
val Turquoise = Color(0xFF00A6AD)
val Cyan = Color(0xFF21C9CE)
val NivaBg = Color(0xFFF7F9FC)
val TextDark = Color(0xFF101B30)
val BorderColor = Color(0xFFE5EAF0)
val SuccessGreen = Color(0xFF249B50)
val WarningOrange = Color(0xFFFF9800)
val StarGold = Color(0xFFFFB300)
val TextGray = Color(0xFF6B7A90)

private val LightColors = lightColorScheme(
    primary = Turquoise, onPrimary = Color.White,
    secondary = NavySecondary, onSecondary = Color.White,
    background = NivaBg, onBackground = TextDark,
    surface = Color.White, onSurface = TextDark,
    surfaceVariant = Color(0xFFEDF2F7), onSurfaceVariant = TextGray,
    outline = BorderColor, error = Color(0xFFD32F2F)
)

private val DarkColors = darkColorScheme(
    primary = Cyan, onPrimary = Color(0xFF062B2D),
    secondary = Cyan, background = NavyDark, onBackground = Color(0xFFE8EEF6),
    surface = NavySecondary, onSurface = Color(0xFFE8EEF6),
    surfaceVariant = NavyCard, onSurfaceVariant = Color(0xFF9FB3C8),
    outline = Color(0xFF1E4468), error = Color(0xFFFF7B7B)
)

/* فونت: به‌صورت پیش‌فرض فونت سیستم استفاده می‌شود.
   برای وزیرمتن: سه فایل TTF را در app/src/main/res/font بریزید و طبق راهنمای انتهای سند،
   این Typography را به FontFamily(Font(R.font.vazirmatn_regular), ...) تغییر دهید. */
val NivaTypography = Typography(
    headlineSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontSize = 15.sp),
    bodyMedium = TextStyle(fontSize = 13.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun NivaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = NivaTypography,
        content = content
    )
}
