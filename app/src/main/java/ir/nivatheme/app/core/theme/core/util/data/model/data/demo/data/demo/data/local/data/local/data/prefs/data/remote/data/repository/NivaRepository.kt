// app/src/main/java/ir/nivatheme/app/data/repository/NivaRepository.kt
package ir.nivatheme.app.data.repository

import ir.nivatheme.app.data.demo.DemoData
import ir.nivatheme.app.data.local.*
import ir.nivatheme.app.data.model.CartItem
import ir.nivatheme.app.data.model.Product
import ir.nivatheme.app.data.model.ProductType
import ir.nivatheme.app.data.prefs.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NivaRepository @Inject constructor(
    private val db: NivaDatabase,
    val session: SessionStore
) {
    private val cartDao = db.cartDao()
    private val favoriteDao = db.favoriteDao()
    private val downloadDao = db.downloadDao()
    private val orderDao = db.orderDao()

    fun products(): List<Product> = DemoData.products
    fun product(id: Long): Product? = DemoData.products.firstOrNull { it.id == id }
    fun courses() = DemoData.courses
    fun course(id: Long) = DemoData.courses.firstOrNull { it.id == id }

    val cart: Flow<List<CartItem>> = cartDao.observe().map { list ->
        list.mapNotNull { e -> product(e.productId)?.let { CartItem(it, e.qty) } }
    }

    suspend fun addToCart(productId: Long) {
        val cur = cartDao.get(productId)
        cartDao.upsert(CartEntity(productId, (cur?.qty ?: 0) + 1))
    }

    suspend fun setQty(productId: Long, qty: Int) {
        if (qty <= 0) cartDao.delete(productId) else cartDao.upsert(CartEntity(productId, qty))
    }

    suspend fun removeFromCart(productId: Long) = cartDao.delete(productId)

    val favorites = favoriteDao.observe().map { l -> l.mapNotNull { product(it.productId) } }

    suspend fun toggleFavorite(id: Long) {
        if (favoriteDao.get(id) == null) favoriteDao.insert(FavoriteEntity(id))
        else favoriteDao.delete(id)
    }

    val downloads = downloadDao.observe()

    suspend fun ensureSeed() {
        if (downloadDao.count() == 0) DemoData.seedDownloads.forEach { downloadDao.upsert(it) }
    }

    val orders = orderDao.observe()

    /* سفارش آزمایشی: سفارش ثبت، لایسنس دانلود فعال و سبد خالی می‌شود.
       در Production این عملیات باید در سرور و پس از تأیید پرداخت انجام شود. */
    suspend fun placeOrder(items: List<CartItem>, discount: Long) {
        val total = items.sumOf { it.lineTotal } - discount
        orderDao.insert(
            OrderEntity(
                date = "۱۴۰۴/۰۳/۰۱",
                total = total,
                itemsJson = items.joinToString("، ") { it.product.title },
                status = "تکمیل شده"
            )
        )
        items.forEach { c ->
            if (c.product.type != ProductType.COURSE) {
                downloadDao.upsert(
                    DownloadEntity(c.product.id, c.product.title, c.product.version, "۱۴۰۴/۱۲/۲۹", true, c.product.iconKey)
                )
            }
        }
        cartDao.clear()
    }

    suspend fun enroll(courseId: Long) = session.enroll(courseId)
}
