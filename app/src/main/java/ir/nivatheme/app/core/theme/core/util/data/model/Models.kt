
// app/src/main/java/ir/nivatheme/app/data/model/Models.kt
package ir.nivatheme.app.data.model

enum class ProductType(val label: String) {
    PLUGIN("افزونه وردپرس"), HTML("قالب HTML"), COURSE("دوره آموزشی")
}

data class Product(
    val id: Long,
    val title: String,
    val type: ProductType,
    val price: Long,
    val oldPrice: Long? = null,
    val rating: Double,
    val reviews: Int,
    val sales: Int,
    val version: String,
    val updated: String,
    val wpVersion: String,
    val shortDesc: String,
    val description: String,
    val features: List<String> = emptyList(),
    val iconKey: String,
    val isNew: Boolean = false,
    val isBestSeller: Boolean = false,
    val isFeatured: Boolean = false
)

data class Lesson(val title: String, val duration: String, val free: Boolean = false)

data class Course(
    val id: Long,
    val title: String,
    val instructor: String,
    val hours: Int,
    val price: Long?,          // null = رایگان
    val iconKey: String,
    val description: String,
    val lessons: List<Lesson>,
    val progressPercent: Int = 0,
    val isNew: Boolean = false,
    val featured: Boolean = false
)

data class CartItem(val product: Product, val qty: Int) {
    val lineTotal: Long get() = product.price * qty
}

data class Review(val name: String, val rating: Int, val text: String, val date: String)
