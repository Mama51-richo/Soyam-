package com.example.soyamgebeya.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.soyamgebeya.data.local.SoyamDatabase
import com.example.soyamgebeya.data.model.ChatMessage
import com.example.soyamgebeya.data.model.Listing
import com.example.soyamgebeya.data.model.MarketplaceCategory
import com.example.soyamgebeya.data.model.Order
import com.example.soyamgebeya.data.model.WalletTransaction
import com.example.soyamgebeya.data.repository.SoyamRepository
import com.example.soyamgebeya.service.GeminiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppLanguage {
    ENGLISH, AMHARIC, AFAAN_OROMO, SOMALI, TIGRINYA
}

class SoyamViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SoyamDatabase.getDatabase(application)
    private val repository = SoyamRepository(
        database.listingDao(),
        database.orderDao(),
        database.walletTransactionDao(),
        database.chatMessageDao()
    )

    // UI Configuration
    val selectedLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentTab = MutableStateFlow("Home") // Home, Marketplace, Sell, Messages, Profile

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<MarketplaceCategory?>(null)

    // Local state for listings, orders, transactions
    val listings: StateFlow<List<Listing>> = repository.allListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<WalletTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wallet states
    val telebirrBalance = MutableStateFlow(1850.50)
    val cbebirrBalance = MutableStateFlow(4200.75)
    val rewardPoints = MutableStateFlow(240)
    val totalReferrals = MutableStateFlow(5)

    // Chat threads & AI states
    val activeChatThread = MutableStateFlow<String?>(null) // "AI_ASSISTANT", "AI_BUSINESS_ADVISOR" or listingId/sellerPhone
    val chatMessages = activeChatThread.flatMapLatest { threadId ->
        if (threadId == null) flowOf(emptyList())
        else repository.getChatMessages(threadId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI thinking/generating states
    val aiGenerating = MutableStateFlow(false)
    val pricingSuggestion = MutableStateFlow<String?>(null)
    val fraudStatus = MutableStateFlow<String?>(null)

    // Seller Dashboard States
    val sellerStoreName = MutableStateFlow("Abebe & Sons Agrotech")
    val sellerStoreTagline = MutableStateFlow("Quality agricultural machinery and seeds directly from Nazret.")
    val sellerStoreLocation = MutableStateFlow("Adama, Ethiopia")
    val sellerIsVerified = MutableStateFlow(true)

    // Selected listing detail navigation state
    val selectedListingDetail = MutableStateFlow<Listing?>(null)

    // Admin platform flags
    val adminSearchQuery = MutableStateFlow("")
    val reportedListings = MutableStateFlow<Set<String>>(emptySet()) // set of flagged listing ids

    init {
        seedInitialDatabase()
    }

    private fun seedInitialDatabase() {
        viewModelScope.launch {
            repository.allListings.first().let { currentListings ->
                if (currentListings.isEmpty()) {
                    val initialListings = listOf(
                        Listing(
                            id = "L1",
                            category = MarketplaceCategory.PRODUCTS.name,
                            title = "Vintage Mesob Basket & Table",
                            titleAmharic = "የባህል መሶብ ወርቅ ሰፊ",
                            description = "Authentic hand-woven Harari Mesob basket, ideal for traditional family dining (Injera serving) and beautiful cultural interior decor. Made by master weaver in Harar.",
                            descriptionAmharic = "በባለሙያ የተሰፋ የማዕድ ማቅረቢያ መሶብ። ለቤት ማስዋቢያ የሚሆን ግሩም የሃረር ባህላዊ እደ-ጥበብ እቃ።",
                            price = 3800.00,
                            imageUrl = "mesob",
                            location = "Bole, Addis Ababa",
                            locationAmharic = "ቦሌ, አዲስ አበባ",
                            sellerName = "Selamawit Harari Craft",
                            sellerPhone = "+251911223344",
                            sellerRating = 4.8f,
                            isSellerVerified = true,
                            timestamp = System.currentTimeMillis() - 3600000,
                            subcategory = "Fashion & Home Decor",
                            viewCount = 142,
                            recommendedPriceRange = "3,500 - 4,200 ETB"
                        ),
                        Listing(
                            id = "L2",
                            category = MarketplaceCategory.PRODUCTS.name,
                            title = "Organic Sidama Coffee Beans (Wholesale)",
                            titleAmharic = "የሲዳማ ጥሬ ቡና በጅምላ",
                            description = "Premium grade washed Sidama Arabica green coffee beans. High quality, sun-dried, directly sourced from farmer cooperative. Minimum order: 50kg.",
                            descriptionAmharic = "የታጠበ ምርጥ የሲዳማ ጥሬ ቡና በጅምላ ለሽያጭ ቀርቧል። አነስተኛው ትዕዛዝ: 50 ኪ.ግ.",
                            price = 12000.00,
                            imageUrl = "coffee",
                            location = "Mercato, Addis Ababa",
                            locationAmharic = "መርካቶ, አዲስ አበባ",
                            sellerName = "Sidama Agro Union",
                            sellerPhone = "+251912556677",
                            sellerRating = 4.9f,
                            isSellerVerified = true,
                            timestamp = System.currentTimeMillis() - 7200000,
                            subcategory = "Agriculture & Food",
                            viewCount = 310,
                            recommendedPriceRange = "11,500 - 13,000 ETB"
                        ),
                        Listing(
                            id = "L3",
                            category = MarketplaceCategory.VEHICLES.name,
                            title = "Toyota Vitz 2018 (Excellent Condition)",
                            titleAmharic = "ቶዮታ ቪትስ 2018 ሞዴል",
                            description = "Super clean automatic Toyota Vitz, excellent mileage, locally used for only 4 months. Fuel-efficient, perfect for Addis commuting.",
                            descriptionAmharic = "በጣም ንፁህ ቶዮታ ቪትስ፣ አውቶማቲክ፣ በትንሽ ኪሎሜትር የተነዳ። ለአዲስ አበባ ትራፊክ ፍቱን መፍትሄ።",
                            price = 1450000.00,
                            imageUrl = "vitz",
                            location = "Gerji, Addis Ababa",
                            locationAmharic = "ገርጂ, አዲስ አበባ",
                            sellerName = "Kidus Car Marketplace",
                            sellerPhone = "+251911990088",
                            sellerRating = 4.5f,
                            isSellerVerified = false,
                            timestamp = System.currentTimeMillis() - 14400000,
                            subcategory = "Cars",
                            viewCount = 890,
                            recommendedPriceRange = "1,400,000 - 1,500,000 ETB"
                        ),
                        Listing(
                            id = "L4",
                            category = MarketplaceCategory.SERVICES.name,
                            title = "Emergency Plumbing & Maintenance",
                            titleAmharic = "የቧንቧ ጥገና አገልግሎት",
                            description = "Certified professional plumbing services available 24/7. Leak fixes, toilet installations, sewer line cleaning, water pump repair.",
                            descriptionAmharic = "ፈጣን የቧንቧ ጥገና ስራ ማናቸውም ፍሳሾች፣ የውሃ ቧንቧ እና ፓምፖች ጥገና በ24 ሰአት ውስጥ።",
                            price = 850.00,
                            imageUrl = "plumbing",
                            location = "Haya Hulet, Addis Ababa",
                            locationAmharic = "ሀያ ሁለት, አዲስ አበባ",
                            sellerName = "Fasil General Maintenance",
                            sellerPhone = "+251910243657",
                            sellerRating = 4.7f,
                            isSellerVerified = true,
                            timestamp = System.currentTimeMillis() - 18000000,
                            subcategory = "Maintenance & Repairs",
                            viewCount = 74,
                            recommendedPriceRange = "700 - 1,000 ETB"
                        ),
                        Listing(
                            id = "L5",
                            category = MarketplaceCategory.CREATIVE_SERVICES.name,
                            title = "Custom Brand Identity Design & Logo",
                            titleAmharic = "የብራንድ እና ሎጎ ዲዛይን ስራዎች",
                            description = "Get professional branding for your local startup. Package includes custom high-res logo designs, banner graphics, and beautiful stationery formats.",
                            descriptionAmharic = "ለድርጅትዎ ወይም ለአዲስ ስራዎ የሚሆን ውብ ሎጎ እና የማስተዋወቂያ ዲዛይን በተመጣጣኝ ዋጋ።",
                            price = 4500.00,
                            imageUrl = "design",
                            location = "Bole Medhanialem, Addis Ababa",
                            locationAmharic = "ቦሌ መድኃኔዓለም, አዲስ አበባ",
                            sellerName = "Yared Creative Studio",
                            sellerPhone = "+251920448899",
                            sellerRating = 4.9f,
                            isSellerVerified = true,
                            timestamp = System.currentTimeMillis() - 28000000,
                            subcategory = "Creative Services",
                            viewCount = 203,
                            recommendedPriceRange = "4,000 - 5,500 ETB"
                        ),
                        Listing(
                            id = "L6",
                            category = MarketplaceCategory.PROPERTY.name,
                            title = "Spacious 2-Bedroom Bole Apartment",
                            titleAmharic = " ባለ ሁለት ክፍል አፓርትመንት በቦሌ",
                            description = "Modern luxury 2-bedroom apartment for rent inside secure walled compound. Excellent backup generator, central location, scenic views of Bole district.",
                            descriptionAmharic = "ለሁሉም መሰረተ-ልማት ቅርብ የሆነ የቅንጦት ባለ 2 ክፍል ኮንዶሚኒየም ቤት ለኪራይ። ጄነሬተር እና አስተማማኝ ጥበቃ አለው።",
                            price = 42000.00,
                            imageUrl = "apartment",
                            location = "Bole Brass, Addis Ababa",
                            locationAmharic = "ቦሌ ብራስ, አዲስ አበባ",
                            sellerName = "Zenebe Real-estate Brokers",
                            sellerPhone = "+251944556600",
                            sellerRating = 4.3f,
                            isSellerVerified = true,
                            timestamp = System.currentTimeMillis() - 40000000,
                            subcategory = "Apartments & Rentals",
                            viewCount = 524,
                            recommendedPriceRange = "40000 - 45000 ETB/Month"
                        ),
                        Listing(
                            id = "L7",
                            category = MarketplaceCategory.JOBS.name,
                            title = "Senior Flutter/Android Developer (Remote)",
                            titleAmharic = "ከቤት ግብረ-ስራ የሞባይል መተግበሪያ ሰራተኛ",
                            description = "Looking for a seasoned Kotlin/Flutter developer to build dynamic commercial super-apps. Local fintech experience a major plus. High USD-pegged salary.",
                            descriptionAmharic = "ለኮንትራት ወይም ሙሉ ቀን የሞባይል መተግበሪያ አበልፃጊ እጅግ በሚማርክ ደሞዝ እንፈልጋለን።",
                            price = 68000.00,
                            imageUrl = "developer_job",
                            location = "Remote / Addis Ababa",
                            locationAmharic = "ከቤት / አዲስ አበባ",
                            sellerName = "Gebeya Fintech Solutions",
                            sellerPhone = "+251900112233",
                            sellerRating = 4.6f,
                            isSellerVerified = true,
                            timestamp = System.currentTimeMillis() - 50000000,
                            subcategory = "Remote Work",
                            viewCount = 1045,
                            recommendedPriceRange = "60,000 - 75,000 ETB"
                        ),
                        Listing(
                            id = "L8",
                            category = MarketplaceCategory.TOURISM.name,
                            title = "Lalibela Guided Heritage Tour (2 Days)",
                            titleAmharic = "የላሊበላ አብያተ ክርስቲያናት ጉብኝት",
                            description = "Breathtaking cultural experience exploring Lalibela rock-hewn churches. Includes professional historical guide, hotel pickup, and authentic local honey-wine tasting.",
                            descriptionAmharic = "የታሪክ መመሪያ፣ የሆቴል ትራንስፖርት እና የሀገር ባህል ማር ጠጅ ቅምሻን የያዘ ልዩ የላሊበላ ቆይታ።",
                            price = 14000.00,
                            imageUrl = "lalibela",
                            location = "Lalibela, Amhara Regional",
                            locationAmharic = "ላሊበላ, አማራ ክልል",
                            sellerName = "Girma Lalibela Guides",
                            sellerPhone = "+251930777999",
                            sellerRating = 5.0f,
                            isSellerVerified = true,
                            timestamp = System.currentTimeMillis() - 60000000,
                            subcategory = "Tours & Trips",
                            viewCount = 370,
                            recommendedPriceRange = "12,000 - 15,000 ETB"
                        )
                    )
                    repository.insertListings(initialListings)

                    // Seed standard transactions
                    repository.insertTransaction(
                        WalletTransaction(
                            transactionId = "TX1",
                            amount = 1500.00,
                            transactionType = "TOPUP",
                            provider = "TELEBIRR",
                            referenceNumber = "REF-TB-9831742",
                            senderOrReceiver = "CBE Bank Account",
                            timestamp = System.currentTimeMillis() - 86400000
                        )
                    )
                    repository.insertTransaction(
                        WalletTransaction(
                            transactionId = "TX2",
                            amount = 350.00,
                            transactionType = "PAYMENT",
                            provider = "CBEBIRR",
                            referenceNumber = "REF-CBE-1029485",
                            senderOrReceiver = "Mercato Store",
                            timestamp = System.currentTimeMillis() - 43200000
                        )
                    )
                    repository.insertTransaction(
                        WalletTransaction(
                            transactionId = "TX3",
                            amount = 50.00,
                            transactionType = "CASHBACK",
                            provider = "TELEBIRR",
                            referenceNumber = "REF-CB-2940192",
                            senderOrReceiver = "Soyam Reward Program",
                            timestamp = System.currentTimeMillis() - 21600000
                        )
                    )
                }
            }
        }
    }

    // Languages helper dictionary
    fun getTranslation(key: String): String {
        return getTranslation(key, selectedLanguage.value)
    }

    fun getTranslation(key: String, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> englishDict[key] ?: key
            AppLanguage.AMHARIC -> amharicDict[key] ?: englishDict[key] ?: key
            AppLanguage.AFAAN_OROMO -> oromoDict[key] ?: englishDict[key] ?: key
            AppLanguage.SOMALI -> somaliDict[key] ?: englishDict[key] ?: key
            AppLanguage.TIGRINYA -> tigrinyaDict[key] ?: englishDict[key] ?: key
        }
    }

    // Business Logic Actions
    fun updateLanguage(lang: AppLanguage) {
        selectedLanguage.value = lang
    }

    fun selectListing(listing: Listing?) {
        selectedListingDetail.value = listing
    }

    fun toggleReportListing(listingId: String) {
        val currentSet = reportedListings.value
        reportedListings.value = if (currentSet.contains(listingId)) {
            currentSet - listingId
        } else {
            currentSet + listingId
        }
    }

    // Wallet transaction actions
    fun transferMoney(amount: Double, sendTo: String, provider: String) {
        viewModelScope.launch {
            val refNo = "REF-${provider.take(3)}-${(1000000..9999999).random()}"
            if (provider == "TELEBIRR") {
                telebirrBalance.value = (telebirrBalance.value - amount).coerceAtLeast(0.0)
            } else {
                cbebirrBalance.value = (cbebirrBalance.value - amount).coerceAtLeast(0.0)
            }
            rewardPoints.value += (amount * 0.05).toInt() // 5% points reward!

            repository.insertTransaction(
                WalletTransaction(
                    transactionId = "TX-${System.currentTimeMillis()}",
                    amount = amount,
                    transactionType = "SEND",
                    provider = provider,
                    referenceNumber = refNo,
                    senderOrReceiver = sendTo,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun topUpWallet(amount: Double, provider: String) {
        viewModelScope.launch {
            val refNo = "REF-${provider.take(3)}-${(1000000..9999999).random()}"
            if (provider == "TELEBIRR") {
                telebirrBalance.value += amount
            } else {
                cbebirrBalance.value += amount
            }

            repository.insertTransaction(
                WalletTransaction(
                    transactionId = "TX-${System.currentTimeMillis()}",
                    amount = amount,
                    transactionType = "TOPUP",
                    provider = provider,
                    referenceNumber = refNo,
                    senderOrReceiver = "Bank Transfer",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Buy / Checkouts
    fun checkoutListing(listing: Listing, paymentMethod: String) {
        viewModelScope.launch {
            val amount = listing.price
            if (paymentMethod == "TELEBIRR" || paymentMethod == "TELEBIRR_PAY") {
                telebirrBalance.value = (telebirrBalance.value - amount).coerceAtLeast(0.0)
            } else if (paymentMethod == "CBEBIRR") {
                cbebirrBalance.value = (cbebirrBalance.value - amount).coerceAtLeast(0.0)
            }
            // Add Reward Points
            rewardPoints.value += (amount * 0.02).toInt()

            val orderId = "ORD-${(10000..99999).random()}"
            val txId = "TX-PAY-${System.currentTimeMillis()}"

            // Save order inside database
            repository.insertOrder(
                Order(
                    orderId = orderId,
                    listingId = listing.id,
                    listingTitle = listing.title,
                    listingImage = listing.imageUrl,
                    listingCategory = listing.category,
                    sellerName = listing.sellerName,
                    buyerName = "You",
                    price = listing.price,
                    status = "Pending",
                    paymentMethod = paymentMethod,
                    timestamp = System.currentTimeMillis()
                )
            )

            // Save transaction histories inside database
            repository.insertTransaction(
                WalletTransaction(
                    transactionId = txId,
                    amount = amount,
                    transactionType = "PAYMENT",
                    provider = paymentMethod.replace("_PAY", ""),
                    referenceNumber = "REF-PMT-${(1000000..9999999).random()}",
                    senderOrReceiver = listing.sellerName,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    // Sell actions
    fun publishListing(
        title: String,
        category: MarketplaceCategory,
        price: Double,
        description: String,
        location: String,
        subcategory: String
    ) {
        viewModelScope.launch {
            val id = "L-${System.currentTimeMillis()}"
            val newListing = Listing(
                id = id,
                category = category.name,
                title = title,
                titleAmharic = title, // Fallback to same
                description = description,
                descriptionAmharic = description, // Fallback to same
                price = price,
                imageUrl = "custom",
                location = location,
                locationAmharic = location,
                sellerName = sellerStoreName.value,
                sellerPhone = "+251955001122",
                sellerRating = 5.0f,
                isSellerVerified = sellerIsVerified.value,
                timestamp = System.currentTimeMillis(),
                subcategory = subcategory,
                viewCount = 1,
                recommendedPriceRange = "${price * 0.9} - ${price * 1.1} ETB"
            )
            repository.insertListing(newListing)
        }
    }

    fun deleteListing(id: String) {
        viewModelScope.launch {
            repository.deleteListing(id)
        }
    }

    // Chat thread message sending
    fun sendMessageToThread(threadId: String, text: String, senderId: String = "USER") {
        viewModelScope.launch {
            val msgId = "MSG-${System.currentTimeMillis()}"
            val myName = if (senderId == "USER") "You" else "Seller"
            val textAmharic = if (senderId == "USER") "" else text

            val message = ChatMessage(
                messageId = msgId,
                senderId = senderId,
                senderName = myName,
                content = text,
                contentAmharic = textAmharic,
                timestamp = System.currentTimeMillis(),
                threadId = threadId
            )
            repository.insertMessage(message)

            // Auto replies for simulator
            if (threadId == "AI_ASSISTANT" && senderId == "USER") {
                triggerGeminiAIResponse(text, "AI_ASSISTANT")
            } else if (threadId == "AI_BUSINESS_ADVISOR" && senderId == "USER") {
                triggerGeminiAIResponse(text, "AI_BUSINESS_ADVISOR")
            } else if (senderId == "USER" && !threadId.startsWith("AI_")) {
                // Simulate Seller Auto Reply
                kotlinx.coroutines.delay(1200)
                repository.insertMessage(
                    ChatMessage(
                        messageId = "MSG-${System.currentTimeMillis()}",
                        senderId = "SELLER",
                        senderName = "Seller Agent",
                        content = "Selam! Thank you for your inquiry about this listing. I am online and accepting payments via Telebirr or CBEBirr. Shall I prepare the delivery?",
                        contentAmharic = "ሰላም ! ስላነጋገሩኝ አመሰግናለሁ። ክፍያ በቴሌብር ወይም በሲቢኢ ይቻላል። እቃውን ልላክሎት ?",
                        timestamp = System.currentTimeMillis(),
                        threadId = threadId
                    )
                )
            }
        }
    }

    // AI Integrations using Gemini Service
    private fun triggerGeminiAIResponse(userPrompt: String, threadId: String) {
        viewModelScope.launch {
            aiGenerating.value = true
            val systemInstructions = if (threadId == "AI_ASSISTANT") {
                "You are Soyam AI Assistant, the smart companion of Ethiopia's Soyam Gebeya super-app. Keep answers helpful, concise, highly focused on Ethiopian contexts (payments, digital products, Mercato trade strategies, telebirr, CBEbirr), and friendly. Highlight that users can safely purchase anything via Telebirr and CBEBirr secure escrow contracts in-app."
            } else {
                "You are Soyam Business Advisor AI, designed to help Ethiopian merchants scale their business, optimize store listings, write highly search-optimized descriptions, advise on local wholesale margins at Merkato and Adama markets, and price items logically based on regional cities."
            }

            val response = GeminiService.generateResponse(userPrompt, systemInstructions)

            repository.insertMessage(
                ChatMessage(
                    messageId = "MSG-${System.currentTimeMillis()}",
                    senderId = threadId,
                    senderName = if (threadId == "AI_ASSISTANT") "Soyam AI Assistant" else "AI Business Advisor",
                    content = response,
                    contentAmharic = "",
                    timestamp = System.currentTimeMillis(),
                    threadId = threadId
                )
            )
            aiGenerating.value = false
        }
    }

    fun requestPriceCheck(listingTitle: String, category: String, desc: String) {
        viewModelScope.launch {
            aiGenerating.value = true
            val prompt = "Estimate the fair retail price in Ethiopian Birr (ETB) and give 3 short bullet points of local market advisory for this item listing: Title: $listingTitle, Category: $category, Description: $desc. Target regional locations like Addis Ababa, Bahir Dar and Awassa."
            val systemMsg = "You are a professional price advisor bot specializing in the modern Ethiopian marketplace ecosystem. Always format the output neatly with bold headers and values."

            val response = GeminiService.generateResponse(prompt, systemMsg)
            pricingSuggestion.value = response
            aiGenerating.value = false
        }
    }

    fun requestFraudScan(title: String, description: String) {
        viewModelScope.launch {
            aiGenerating.value = true
            val prompt = "Scan the following product title and description for commercial scam elements, suspicious pricing anomalies (too cheap/expensive), black market patterns, or invalid payment methods. Title: $title, Description: $description."
            val systemMsg = "You are Soyam Trust & Security AI. Evaluate the commercial risk of other sellers' listings. Highlight any flags clearly in red or status warnings."

            val response = GeminiService.generateResponse(prompt, systemMsg)
            fraudStatus.value = response
            aiGenerating.value = false
        }
    }

    // Languages Dict Definitions
    private val englishDict = mapOf(
        "app_tagline" to "Everything Ethiopian. One Marketplace.",
        "search_hint" to "Search products, services, jobs, rentals...",
        "trending" to "Trending Now",
        "featured_businesses" to "Featured Merchants",
        "nearby" to "Nearby Opportunities",
        "recent" to "Recently Added",
        "home" to "Home",
        "marketplace" to "Marketplace",
        "sell" to "Sell",
        "messages" to "Messages",
        "profile" to "Profile",
        "buy_now" to "Buy Now",
        "add_listing" to "Add New Listing",
        "verified" to "Verified Merchant",
        "wallet" to "Soyam Wallet",
        "top_up" to "Top Up",
        "transfer" to "Send Money",
        "points" to "Reward Points",
        "ai_assistant" to "AI Assistant",
        "business_advisor" to "AI Partner",
        "languages" to "Supported Languages",
        "feedback_reviews" to "Reviews & Feedback",
        "admin_dashboard" to "Admin Dashboard",
        "seller_tools" to "Seller Tools",
        "order_tracking" to "Order Tracking",
        "completed" to "Completed",
        "pending" to "Pending",
        "in_progress" to "In Progress",
        "disputed" to "Disputed",
        "accepted" to "Accepted",
        "cancelled" to "Cancelled",
        "all" to "All Categories"
    )

    private val amharicDict = mapOf(
        "app_tagline" to "ኢትዮጵያዊ የሆነው ሁሉ በአንድ ገበያ።",
        "search_hint" to "ምርቶች ፣ ሙያዊ ስራዎች ፣ ኪራዮችን እዚህ ይፈልጉ ...",
        "trending" to "በብዛት እየታዩ ያሉ",
        "featured_businesses" to "ታዋቂ ነጋዴዎች",
        "nearby" to "በአቅራቢያ ያሉ እድሎች",
        "recent" to "በቅርቡ የተጨመሩ",
        "home" to "መነሻ",
        "marketplace" to "ካታሎግ",
        "sell" to "ለመሸጥ",
        "messages" to "መልዕክቶች",
        "profile" to "መለያዬ",
        "buy_now" to "ክፈያና ግዢ",
        "add_listing" to "አዲስ ዕቃ መመዝገብ",
        "verified" to "የተረጋገጠ አባል",
        "wallet" to "የሶያም ዲጂታል ቦርሳ",
        "top_up" to "ገንዘብ አስገባ",
        "transfer" to "ገንዘብ ላክ",
        "points" to "የሽልማት ነጥቦች",
        "ai_assistant" to "አርቴፊሻል ረዳት",
        "business_advisor" to "የቢዝነስ አማካሪ",
        "languages" to "ክልላዊ ቋንቋዎች",
        "feedback_reviews" to "ደረጃና አስተያየቶች",
        "admin_dashboard" to "የአስተዳዳሪ ማዕከል",
        "seller_tools" to "የአከፋፋዮች መከታተያ",
        "order_tracking" to "ደብዳቤዎችና ትዕዛዞች",
        "completed" to "የተጠናቀቀ",
        "pending" to "እየተጠባበቀ ያለ",
        "in_progress" to "በሂደት ላይ",
        "disputed" to "አለመግባባት ያለበት",
        "accepted" to "የተቀበሉት",
        "cancelled" to "የተሰረዘ",
        "all" to "ሁሉም ክፍሎች"
    )

    private val oromoDict = mapOf(
        "app_tagline" to "Hunduu Itoophiyaa. Wiirtuu tokko.",
        "search_hint" to "Meeshaalee, hojii, kireeffamu barbaadi...",
        "trending" to "Amma Mul'atu",
        "featured_businesses" to "Daldaltoota Bebekamoo",
        "nearby" to "Carraawwan Naannoo",
        "recent" to "Dhiheenya Kan Dabalame",
        "home" to "Mana",
        "marketplace" to "Gabaa",
        "sell" to "Gurguri",
        "messages" to "Ergaawwan",
        "profile" to "Profaayilii",
        "buy_now" to "Bituuf",
        "add_listing" to "Gabaa Dabaladhu",
        "verified" to "Mirkanaa'e",
        "wallet" to "Soyam Saanduka",
        "top_up" to "Guuti",
        "transfer" to "Kofalchi",
        "points" to "Qabxiilee Badhaasaa",
        "ai_assistant" to "Gargaaraa AI",
        "business_advisor" to "Gorsitoota Daldalaa",
        "languages" to "Afaanota Sadarkaa",
        "feedback_reviews" to "Yaada",
        "admin_dashboard" to "Giddugala Admin",
        "seller_tools" to "Kuusaa Gurgurtootaa",
        "order_tracking" to "Hordoffii",
        "completed" to "Xumurame",
        "pending" to "Eegamaa jira",
        "in_progress" to "Hojjatamaa jira",
        "disputed" to "Wal-diddaa",
        "accepted" to "Fudhatame",
        "cancelled" to "Haqame",
        "all" to "Koree Hunda"
    )

    private val somaliDict = mapOf(
        "app_tagline" to "Wax kasta oo Itoobiyaan ah. Hal Suuqa.",
        "search_hint" to "Raadi alaabo, adeegyo, shaqooyin...",
        "trending" to "Hadda Socda",
        "featured_businesses" to "Ganacsatada caanka ah",
        "nearby" to "Fursado kuu dhow",
        "recent" to "Dhawaan lagu daray",
        "home" to "Hoyga",
        "marketplace" to "Suuqa",
        "sell" to "Ibi",
        "messages" to "Farriimo",
        "profile" to "Profile",
        "buy_now" to "Hadda iibso",
        "add_listing" to "Kudar alaab",
        "verified" to "Ganacsade la hubiyay",
        "wallet" to "Soyam Boorsada",
        "top_up" to "Ku shubo",
        "transfer" to "Xawil",
        "points" to "Dhibco abaalmarin",
        "ai_assistant" to "Caawiyaha AI",
        "business_advisor" to "La-taliyaha Ganacsiga",
        "languages" to "Luuqadaha",
        "feedback_reviews" to "Faallooyinka",
        "admin_dashboard" to "Meesha Maamulka",
        "seller_tools" to "Agabka Iibiyaha",
        "order_tracking" to "La-socodka dalabka",
        "completed" to "Dhamaaday",
        "pending" to "Sugaya",
        "in_progress" to "Hadda socda",
        "disputed" to "Khilaaf jira",
        "accepted" to "La aqbalay",
        "cancelled" to "La baajiyay",
        "all" to "Qaybaha oo dhan"
    )

    private val tigrinyaDict = mapOf(
        "app_tagline" to "ኩሉ ኢትዮጵያዊ። ሓደ ዕዳጋ።",
        "search_hint" to "ምርቶታት፣ ሙያዊ ስራሕቲ፣ ክራይ ክትደሊ...",
        "trending" to "ብብዝሒ ዝተራእዩ",
        "featured_businesses" to "ፍሉጣት ነጋዶ",
        "nearby" to "ኣብ ከባቢኹም ዘለዉ ዕድላት",
        "recent" to "ዝተመረጹ ሓደስቲ",
        "home" to "ቤት",
        "marketplace" to "ዕዳጋ",
        "sell" to "ምሻጥ",
        "messages" to "መልእኽትታት",
        "profile" to "መገለጺ ሒሳብ",
        "buy_now" to "ክፍሊትን ዕድጊትን",
        "add_listing" to "ሓድሽ ዕቃ መዝግብ",
        "verified" to "ዝተረጋገጸ ኣባል",
        "wallet" to "ካብቲ ዲጂታል ቦርሳ",
        "top_up" to "መልእኽቶም አስፍር",
        "transfer" to "ፋይናንስ ልኣኽ",
        "points" to "ናይ ሽልማት ዓንቀጻት",
        "ai_assistant" to "ረዳት ቴክኖሎጂ",
        "business_advisor" to "ኣማኻሪ ንግዲ",
        "languages" to "ዝተፈላለዩ ቋንቋታት",
        "feedback_reviews" to "ርእይቶን ግምገማን",
        "admin_dashboard" to "ማእኸል መመሓደሪ",
        "seller_tools" to "ናይ ነጋዳይ ማሕደር",
        "order_tracking" to "ምክትታል ትእዛዛት",
        "completed" to "ዝተወድአ",
        "pending" to "ዝጽበ ዘሎ",
        "in_progress" to "ኣብ መስርሕ ዘሎ",
        "disputed" to "ኣብ ንሕንሕ ዘሎ",
        "accepted" to "ዝተቐበልዎ",
        "cancelled" to "ዝተሰረዘ",
        "all" to "ኩሎም ክፍልታት"
    )
}
