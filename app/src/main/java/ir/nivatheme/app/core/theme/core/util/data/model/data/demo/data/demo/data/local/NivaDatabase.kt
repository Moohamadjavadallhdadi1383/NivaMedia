// app/src/main/java/ir/nivatheme/app/data/local/NivaDatabase.kt
package ir.nivatheme.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cart")
data class CartEntity(@PrimaryKey val productId: Long, val qty: Int)

@Entity(tableName = "favorites")
data class FavoriteEntity(@PrimaryKey val productId: Long)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val version: String,
    val expiry: String?,
    val active: Boolean,
    val iconKey: String
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val total: Long,
    val itemsJson: String,
    val status: String
)

@Dao
interface CartDao {
    @Query("SELECT * FROM cart") fun observe(): Flow<List<CartEntity>>
    @Query("SELECT * FROM cart WHERE productId = :id") suspend fun get(id: Long): CartEntity?
    @Upsert suspend fun upsert(entity: CartEntity)
    @Query("DELETE FROM cart WHERE productId = :id") suspend fun delete(id: Long)
    @Query("DELETE FROM cart") suspend fun clear()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites") fun observe(): Flow<List<FavoriteEntity>>
    @Query("SELECT * FROM favorites WHERE productId = :id") suspend fun get(id: Long): FavoriteEntity?
    @Insert suspend fun insert(entity: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE productId = :id") suspend fun delete(id: Long)
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads") fun observe(): Flow<List<DownloadEntity>>
    @Query("SELECT COUNT(*) FROM downloads") suspend fun count(): Int
    @Upsert suspend fun upsert(entity: DownloadEntity)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY id DESC") fun observe(): Flow<List<OrderEntity>>
    @Insert suspend fun insert(entity: OrderEntity)
}

@Database(
    entities = [CartEntity::class, FavoriteEntity::class, DownloadEntity::class, OrderEntity::class],
    version = 1, exportSchema = false
)
abstract class NivaDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun downloadDao(): DownloadDao
    abstract fun orderDao(): OrderDao
}
