package com.example.soyamgebeya.data.local

import androidx.room.*
import com.example.soyamgebeya.data.model.Listing
import com.example.soyamgebeya.data.model.Order
import com.example.soyamgebeya.data.model.WalletTransaction
import com.example.soyamgebeya.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings ORDER BY timestamp DESC")
    fun getAllListings(): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE category = :category ORDER BY timestamp DESC")
    fun getListingsByCategory(category: String): Flow<List<Listing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<Listing>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: Listing)

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteListingById(id: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: WalletTransaction)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesByThread(threadId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: ChatMessage)
}

@Database(
    entities = [Listing::class, Order::class, WalletTransaction::class, ChatMessage::class],
    version = 1,
    exportSchema = false
)
abstract class SoyamDatabase : RoomDatabase() {
    abstract fun listingDao(): ListingDao
    abstract fun orderDao(): OrderDao
    abstract fun walletTransactionDao(): WalletTransactionDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: SoyamDatabase? = null

        fun getDatabase(context: android.content.Context): SoyamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SoyamDatabase::class.java,
                    "soyam_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
