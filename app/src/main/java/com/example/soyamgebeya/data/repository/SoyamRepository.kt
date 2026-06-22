package com.example.soyamgebeya.data.repository

import com.example.soyamgebeya.data.local.ListingDao
import com.example.soyamgebeya.data.local.OrderDao
import com.example.soyamgebeya.data.local.WalletTransactionDao
import com.example.soyamgebeya.data.local.ChatMessageDao
import com.example.soyamgebeya.data.model.Listing
import com.example.soyamgebeya.data.model.Order
import com.example.soyamgebeya.data.model.WalletTransaction
import com.example.soyamgebeya.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

class SoyamRepository(
    private val listingDao: ListingDao,
    private val orderDao: OrderDao,
    private val walletTransactionDao: WalletTransactionDao,
    private val chatMessageDao: ChatMessageDao
) {
    val allListings: Flow<List<Listing>> = listingDao.getAllListings()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val allTransactions: Flow<List<WalletTransaction>> = walletTransactionDao.getAllTransactions()

    fun getListingsByCategory(category: String): Flow<List<Listing>> =
        listingDao.getListingsByCategory(category)

    fun getChatMessages(threadId: String): Flow<List<ChatMessage>> =
        chatMessageDao.getMessagesByThread(threadId)

    suspend fun insertListing(listing: Listing) = listingDao.insertListing(listing)
    suspend fun insertListings(listings: List<Listing>) = listingDao.insertListings(listings)
    suspend fun deleteListing(id: String) = listingDao.deleteListingById(id)

    suspend fun insertOrder(order: Order) = orderDao.insertOrder(order)
    suspend fun updateOrderStatus(orderId: String, status: String) =
        orderDao.updateOrderStatus(orderId, status)

    suspend fun insertTransaction(tx: WalletTransaction) =
        walletTransactionDao.insertTransaction(tx)

    suspend fun insertMessage(msg: ChatMessage) = chatMessageDao.insertMessage(msg)
}
