package com.example.soyamgebeya.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

enum class MarketplaceCategory(val displayName: String, val amharicName: String, val iconName: String) {
    PRODUCTS("Products", "ምርቶች", "shopping_bag"),
    SERVICES("Services", "አገልግሎቶች", "build"),
    MANUFACTURING("Manufacturing", "ማኑፋክቸሪንግ", "factory"),
    PROFESSIONAL_SERVICES("Professional", "ሙያዊ አገልግሎት", "gavel"),
    CREATIVE_SERVICES("Creative", "ፈጠራ ስራዎች", "palette"),
    KNOWLEDGE_MARKETPLACE("Knowledge", "ዕውቀትና ስልጠና", "school"),
    BUSINESS_OPPORTUNITIES("Business Opps", "የቢዝነስ አማራጭ", "trending_up"),
    JOBS("Jobs", "ስራዎች", "work"),
    PROPERTY("Property", "ንብረትና መሬት", "home"),
    VEHICLES("Vehicles", "ተሽከርካሪዎች", "directions_car"),
    TOURISM("Tourism", "ቱሪዝም", "flight"),
    EDUCATION("Education", "ትምህርት", "auto_stories"),
    GOVERNMENT_SERVICES("Gov Services", "የመንግስት አገልግሎት", "account_balance")
}

@Entity(tableName = "listings")
@JsonClass(generateAdapter = true)
data class Listing(
    @PrimaryKey val id: String,
    val category: String, // String representation of MarketplaceCategory
    val title: String,
    val titleAmharic: String,
    val description: String,
    val descriptionAmharic: String,
    val price: Double,
    val imageUrl: String, // either a placeholder identifier or URL
    val location: String, // e.g., "Addis Ababa", "Bole", "Adama"
    val locationAmharic: String,
    val sellerName: String,
    val sellerPhone: String,
    val sellerRating: Float,
    val isSellerVerified: Boolean,
    val timestamp: Long,
    val subcategory: String = "",
    val viewCount: Int = 0,
    val recommendedPriceRange: String = ""
)

@Entity(tableName = "orders")
@JsonClass(generateAdapter = true)
data class Order(
    @PrimaryKey val orderId: String,
    val listingId: String,
    val listingTitle: String,
    val listingImage: String,
    val listingCategory: String,
    val sellerName: String,
    val buyerName: String,
    val price: Double,
    val status: String, // Pending, Accepted, In Progress, Completed, Cancelled, Disputed, Refunded
    val paymentMethod: String, // Telebirr, CBEBirr, Chapa, Cash
    val timestamp: Long
)

@Entity(tableName = "transactions")
@JsonClass(generateAdapter = true)
data class WalletTransaction(
    @PrimaryKey val transactionId: String,
    val amount: Double,
    val transactionType: String, // SEND, RECEIVE, PAYMENT, CASHBACK, REFERRAL, TOPUP
    val provider: String, // TELEBIRR, CBEBIRR, CHAPA, CASH
    val referenceNumber: String,
    val senderOrReceiver: String,
    val timestamp: Long
)

@Entity(tableName = "messages")
@JsonClass(generateAdapter = true)
data class ChatMessage(
    @PrimaryKey val messageId: String,
    val senderId: String, // "USER" or "SELLER" or "AI_ASSISTANT" or "AI_BUSINESS_ADVISOR"
    val senderName: String,
    val content: String,
    val contentAmharic: String = "",
    val timestamp: Long,
    val threadId: String // "AI_ASSISTANT", "AI_BUSINESS_ADVISOR", or listingId/sellerName
)
