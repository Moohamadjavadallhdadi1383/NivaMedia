// app/src/main/java/ir/nivatheme/app/ui/components/NivaComponents.kt
package ir.nivatheme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import ir.nivatheme.app.core.theme.*
import ir.nivatheme.app.core.util.toman
import ir.nivatheme.app.data.model.Product
import ir.nivatheme.app.data.model.ProductType

@Composable
fun NivaHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(NavyDark, NavySecondary)))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "بازگشت", tint = Color.White)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall)
                if (subtitle != null)
                    Text(subtitle, color = Color(0xFF9FB3C8), style = MaterialTheme.typography.bodyMedium)
            }
            actions()
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Apps, null, tint = Turquoise, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (onSeeAll != null) {
            Row(
                Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onSeeAll).padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("مشاهده همه", color = Turquoise, style = MaterialTheme.typography.labelLarge)
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = Turquoise, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun StarRating(rating: Double, size: Dp = 13.dp) {
    Row {
        (1..5).forEach { i ->
            Icon(
                Icons.Rounded.Star, null,
                tint = if (rating >= i - 0.3) StarGold else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(size)
            )
        }
    }
}

@Composable
fun TypeChip(type: ProductType) {
    val bg = when (type) {
        ProductType.PLUGIN -> Turquoise
        ProductType.HTML -> NavySecondary
        ProductType.COURSE -> WarningOrange
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(type.label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ProductThumb(iconKey: String, modifier: Modifier = Modifier, iconSize: Dp = 34.dp, corner: Dp = 14.dp) {
    val (c1, c2, icon) = when (iconKey) {
        "seo" -> Triple(Color(0xFF123B63), Color(0xFF1E6FA8), Icons.Rounded.TrendingUp)
        "html", "htmlcourse" -> Triple(Color(0xFF0E3A5C), Color(0xFF0A2540), Icons.Rounded.Code)
        "form" -> Triple(Color(0xFF3B2A6B), Color(0xFF6C4DE0), Icons.Rounded.EditNote)
        "woo" -> Triple(Color(0xFF123B63), Color(0xFF2C6E9E), Icons.Rounded.ShoppingCart)
        "security" -> Triple(Color(0xFF0F3D63), Color(0xFF1D7FD1), Icons.Rounded.Security)
        "mail" -> Triple(Color(0xFF5B2D8E), Color(0xFF9C4DE0), Icons.Rounded.Email)
        "speed" -> Triple(Color(0xFF0E5C3A), Color(0xFF22A55F), Icons.Rounded.Rocket)
        "contact" -> Triple(Color(0xFFB3541E), Color(0xFFE07A2F), Icons.Rounded.AlternateEmail)
        "wp" -> Triple(Color(0xFF0A2C4E), Color(0xFF17638F), Icons.Rounded.PhoneAndroid)
        "figma" -> Triple(Color(0xFF3A1D5C), Color(0xFF8A4DE0), Icons.Rounded.Brush)
        "js" -> Triple(Color(0xFF2C2C54), Color(0xFF5B4BC4), Icons.Rounded.Code)
        "pdf" -> Triple(Color(0xFF7A1F1F), Color(0xFFC62828), Icons.Rounded.PictureAsPdf)
        else -> Triple(NavySecondary, NavyDark, Icons.Rounded.ShoppingBag)
    }
    Box(
        modifier.background(Brush.linearGradient(listOf(c1, c2)), RoundedCornerShape(corner)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun AddToCartButton(onClick: () -> Unit, size: Dp = 38.dp) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(Turquoise)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Rounded.ShoppingCart, "افزودن به سبد", tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun ProductCard(
    p: Product,
    fav: Boolean,
    onFav: () -> Unit,
    onAdd: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column {
            Box {
                ProductThumb(p.iconKey, Modifier.fillMaxWidth().height(105.dp).padding(8.dp), iconSize = 38.dp)
                if (p.oldPrice != null) {
                    Box(
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarningOrange)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "٪${(((p.oldPrice - p.price) * 100) / p.oldPrice).toInt().toFa()} تخفیف",
                            color = Color.White, style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
                        .clickable(onClick = onFav),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (fav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        "علاقه‌مندی",
                        tint = if (fav) Color(0xFFE91E63) else TextGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(
                    p.title, style = MaterialTheme.typography.titleMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TypeChip(p.type)
                    Spacer(Modifier.width(6.dp))
                    StarRating(p.rating)
                    Text(" ${p.rating.toFa()}", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        if (p.oldPrice != null)
                            Text(
                                p.oldPrice!!.toman(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        Text(p.price.toman(), color = Turquoise, style = MaterialTheme.typography.labelLarge)
                    }
                    AddToCartButton(onAdd)
                }
            }
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            subtitle, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    brush: Brush = Brush.horizontalGradient(listOf(Turquoise, Cyan))
) {
    Box(
        modifier
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) brush else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)) }
            Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
    }
}
