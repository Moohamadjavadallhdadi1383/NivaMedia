// app/src/main/java/ir/nivatheme/app/data/remote/WooApi.kt
package ir.nivatheme.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/* ─────────────────────────────────────────────────────────────
   ⚠️ فاز ۳ — اتصال واقعی به WooCommerce (فعلاً غیرفعال)
   در حالت DEMO_MODE استفاده نمی‌شود.

   نکات امنیتی مهم:
   • Consumer Key/Secret ووکامرس هرگز نباید داخل APK ذخیره شوند.
   • عملیات حساس (OTP، سفارش، پرداخت، لینک دانلود) باید از یک
     بک‌اند/افزونه اختصاصی نیوا روی سرور عبور کنند و توکن JWT
     به اپ برگردد.
   • Endpointهای پیشنهادی افزونه نیوا:
       POST /wp-json/niva/v1/auth/request-otp
       POST /wp-json/niva/v1/auth/verify-otp
       GET  /wp-json/niva/v1/me/downloads
       POST /wp-json/niva/v1/orders
       GET  /wp-json/niva/v1/orders/{id}/status
   ───────────────────────────────────────────────────────────── */

data class WcImageDto(val src: String = "")
data class WcProductDto(
    val id: Long = 0,
    val name: String = "",
    val price: String = "0",
    val regular_price: String? = null,
    val average_rating: String = "0",
    val images: List<WcImageDto>? = null
)

interface WooApi {
    @GET("wp-json/wc/v3/products")
    suspend fun products(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): List<WcProductDto>
}
