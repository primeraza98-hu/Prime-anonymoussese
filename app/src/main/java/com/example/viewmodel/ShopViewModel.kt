package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.ProductRepository
import com.example.model.CardTransaction
import com.example.model.CartItem
import com.example.model.DigitalProduct
import com.example.model.OrderStatus
import com.example.model.PaymentMethodType
import com.example.model.PaymentOrder
import com.example.model.PurchasedCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class ShopUiState(
    val products: List<DigitalProduct> = ProductRepository.products,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val cartItems: List<CartItem> = emptyList(),
    val purchasedCards: List<PurchasedCard> = emptyList(), // Empty until owner approves or initial seed
    val orders: List<PaymentOrder> = getInitialOrders(),
    val selectedProductForDetail: DigitalProduct? = null,
    val activeTab: Int = 0, // 0 = Store, 1 = Cart, 2 = Vault / My Cards, 3 = Owner Portal, 4 = Guide
    val promoCode: String = "",
    val promoDiscountUsd: Double = 0.0,
    val promoMessage: String? = null,
    val isCheckoutInProgress: Boolean = false,
    val lastSubmittedOrder: PaymentOrder? = null,
    val showOrderPendingDialog: Boolean = false,
    val userEmail: String = "client@digitalcard.me"
)

private fun getInitialOrders(): List<PaymentOrder> {
    return listOf(
        PaymentOrder(
            orderId = "PMH-8821",
            customerEmail = "client.raza@gmail.com",
            items = listOf(CartItem(ProductRepository.products[0], 1)), // All Shopping $20 -> $200
            totalPayable = 20.0,
            totalBalance = 200.0,
            paymentMethod = PaymentMethodType.EASYPAISA,
            transactionId = "TXN98472910381",
            receiptUri = null,
            receiptName = "easypaisa_proof_naila_hussain.jpg",
            timestamp = System.currentTimeMillis() - 1800000L,
            status = OrderStatus.PENDING_APPROVAL
        )
    )
}

class ShopViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openProductDetail(product: DigitalProduct) {
        _uiState.update { it.copy(selectedProductForDetail = product) }
    }

    fun closeProductDetail() {
        _uiState.update { it.copy(selectedProductForDetail = null) }
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
    }

    fun addToCart(product: DigitalProduct, quantity: Int = 1) {
        _uiState.update { state ->
            val existingIndex = state.cartItems.indexOfFirst { it.product.id == product.id }
            val newItems = state.cartItems.toMutableList()
            if (existingIndex >= 0) {
                val current = newItems[existingIndex]
                newItems[existingIndex] = current.copy(quantity = current.quantity + quantity)
            } else {
                newItems.add(CartItem(product = product, quantity = quantity))
            }
            state.copy(cartItems = newItems)
        }
    }

    fun updateCartQuantity(productId: String, delta: Int) {
        _uiState.update { state ->
            val newItems = state.cartItems.mapNotNull { item ->
                if (item.product.id == productId) {
                    val newQty = item.quantity + delta
                    if (newQty > 0) item.copy(quantity = newQty) else null
                } else item
            }
            state.copy(cartItems = newItems)
        }
    }

    fun removeFromCart(productId: String) {
        _uiState.update { state ->
            state.copy(cartItems = state.cartItems.filterNot { it.product.id == productId })
        }
    }

    fun applyPromoCode(code: String) {
        val clean = code.trim().uppercase()
        when (clean) {
            "SAVE5" -> {
                _uiState.update { it.copy(promoCode = clean, promoDiscountUsd = 5.0, promoMessage = "🎉 $5.00 discount applied!") }
            }
            "DIGIPAY" -> {
                _uiState.update { it.copy(promoCode = clean, promoDiscountUsd = 3.0, promoMessage = "⚡ $3.00 instant VIP promo applied!") }
            }
            else -> {
                _uiState.update { it.copy(promoMessage = "Invalid code. Try 'SAVE5' or 'DIGIPAY'") }
            }
        }
    }

    // Step 1: Customer submits payment (TRX ID and/or uploaded receipt).
    // Status is set to PENDING_APPROVAL. Product is NOT received until owner accepts!
    fun submitCheckoutOrder(
        email: String,
        paymentMethod: PaymentMethodType,
        trxId: String,
        receiptUri: String?,
        receiptName: String?
    ) {
        val state = _uiState.value
        if (state.cartItems.isEmpty()) return

        val rawSubtotal = state.cartItems.sumOf { it.product.priceUsd * it.quantity }
        val finalPayable = maxOf(0.0, rawSubtotal - state.promoDiscountUsd)
        val totalBalance = state.cartItems.sumOf { it.product.balanceUsd * it.quantity }

        val order = PaymentOrder(
            orderId = "PMH-${(1000..9999).random()}",
            customerEmail = email,
            items = state.cartItems.toList(),
            totalPayable = finalPayable,
            totalBalance = totalBalance,
            paymentMethod = paymentMethod,
            transactionId = if (trxId.isBlank()) "TRX-${(100000..999999).random()}" else trxId,
            receiptUri = receiptUri,
            receiptName = receiptName,
            timestamp = System.currentTimeMillis(),
            status = OrderStatus.PENDING_APPROVAL
        )

        _uiState.update { current ->
            current.copy(
                orders = listOf(order) + current.orders,
                cartItems = emptyList(),
                lastSubmittedOrder = order,
                showOrderPendingDialog = true,
                userEmail = email,
                activeTab = 2 // Switch to Vault to see the pending approval status
            )
        }
    }

    // Instant Buy also submits a pending request to the app owner
    fun submitInstantBuyOrder(
        product: DigitalProduct,
        quantity: Int,
        email: String,
        paymentMethod: PaymentMethodType,
        trxId: String,
        receiptUri: String?,
        receiptName: String?
    ) {
        val finalPayable = product.priceUsd * quantity
        val totalBalance = product.balanceUsd * quantity

        val order = PaymentOrder(
            orderId = "PMH-${(1000..9999).random()}",
            customerEmail = email,
            items = listOf(CartItem(product = product, quantity = quantity)),
            totalPayable = finalPayable,
            totalBalance = totalBalance,
            paymentMethod = paymentMethod,
            transactionId = if (trxId.isBlank()) "TRX-${(100000..999999).random()}" else trxId,
            receiptUri = receiptUri,
            receiptName = receiptName,
            timestamp = System.currentTimeMillis(),
            status = OrderStatus.PENDING_APPROVAL
        )

        _uiState.update { current ->
            current.copy(
                orders = listOf(order) + current.orders,
                lastSubmittedOrder = order,
                showOrderPendingDialog = true,
                selectedProductForDetail = null,
                userEmail = email,
                activeTab = 2 // Switch to Vault to see pending approval status
            )
        }
    }

    fun dismissOrderPendingDialog() {
        _uiState.update { it.copy(showOrderPendingDialog = false) }
    }

    // Step 2: APP OWNER ACCEPTS REQUEST
    // Only after the owner accepts does the customer receive the product!
    fun approveOrder(orderId: String) {
        val state = _uiState.value
        val order = state.orders.firstOrNull { it.orderId == orderId } ?: return

        // Generate the digital cards for customer's vault
        val newlyDeliveredCards = mutableListOf<PurchasedCard>()
        order.items.forEach { item ->
            for (i in 1..item.quantity) {
                val randomDigits = (1000..9999).random()
                val randomCvv = (100..999).random().toString()
                val card = PurchasedCard(
                    id = "card_${UUID.randomUUID().toString().take(8)}",
                    productId = item.product.id,
                    title = item.product.name,
                    balanceRemaining = item.product.balanceUsd,
                    originalBalance = item.product.balanceUsd,
                    pricePaid = item.product.priceUsd,
                    cardNumber = item.product.defaultCardNumber.replace(Regex("\\d{4}$"), "$randomDigits"),
                    expiry = item.product.expiry,
                    cvv = randomCvv,
                    cardholderName = order.customerEmail.substringBefore("@").uppercase(),
                    isFrozen = false,
                    purchaseTimestamp = System.currentTimeMillis(),
                    category = item.product.category,
                    imageRes = item.product.imageRes,
                    transactions = listOf(
                        CardTransaction(
                            id = "tx_${UUID.randomUUID().toString().take(6)}",
                            merchant = "Pre-loaded Starting Balance (Approved by Owner)",
                            amount = item.product.balanceUsd,
                            timestamp = System.currentTimeMillis(),
                            isDebit = false
                        )
                    )
                )
                newlyDeliveredCards.add(card)
            }
        }

        val updatedOrders = state.orders.map {
            if (it.orderId == orderId) it.copy(status = OrderStatus.APPROVED) else it
        }

        _uiState.update { current ->
            current.copy(
                orders = updatedOrders,
                purchasedCards = newlyDeliveredCards + current.purchasedCards
            )
        }
    }

    // App owner can decline the request
    fun rejectOrder(orderId: String, reason: String = "Payment verification failed") {
        _uiState.update { state ->
            val updated = state.orders.map {
                if (it.orderId == orderId) it.copy(status = OrderStatus.REJECTED, adminNote = reason) else it
            }
            state.copy(orders = updated)
        }
    }

    fun toggleFreezeCard(cardId: String) {
        _uiState.update { state ->
            val updated = state.purchasedCards.map { card ->
                if (card.id == cardId) card.copy(isFrozen = !card.isFrozen) else card
            }
            state.copy(purchasedCards = updated)
        }
    }

    fun simulateSpend(cardId: String, merchant: String, amount: Double) {
        _uiState.update { state ->
            val updated = state.purchasedCards.map { card ->
                if (card.id == cardId && !card.isFrozen && card.balanceRemaining >= amount) {
                    val newBalance = card.balanceRemaining - amount
                    val newTx = CardTransaction(
                        id = "tx_${UUID.randomUUID().toString().take(6)}",
                        merchant = merchant,
                        amount = amount,
                        timestamp = System.currentTimeMillis(),
                        isDebit = true
                    )
                    card.copy(
                        balanceRemaining = newBalance,
                        transactions = listOf(newTx) + card.transactions
                    )
                } else card
            }
            state.copy(purchasedCards = updated)
        }
    }
}
