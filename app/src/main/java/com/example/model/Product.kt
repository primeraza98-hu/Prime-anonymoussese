package com.example.model

import androidx.annotation.DrawableRes

data class DigitalProduct(
    val id: String,
    val name: String,
    val subtitle: String,
    val priceUsd: Double,
    val balanceUsd: Double,
    val category: String,
    val badge: String,
    val description: String,
    val highlights: List<String>,
    val supportedPlatforms: List<String>,
    @DrawableRes val imageRes: Int,
    val defaultCardholder: String,
    val defaultCardNumber: String,
    val expiry: String,
    val cvv: String,
    val rating: Float = 4.9f,
    val reviewCount: Int = 1240,
    val deliveryTimeSeconds: Int = 30
)

data class CartItem(
    val product: DigitalProduct,
    val quantity: Int = 1
)

data class PurchasedCard(
    val id: String,
    val productId: String,
    val title: String,
    val balanceRemaining: Double,
    val originalBalance: Double,
    val pricePaid: Double,
    val cardNumber: String,
    val expiry: String,
    val cvv: String,
    val cardholderName: String,
    val isFrozen: Boolean = false,
    val purchaseTimestamp: Long = System.currentTimeMillis(),
    val category: String,
    @DrawableRes val imageRes: Int,
    val transactions: List<CardTransaction> = emptyList()
)

data class CardTransaction(
    val id: String,
    val merchant: String,
    val amount: Double,
    val timestamp: Long,
    val isDebit: Boolean = true
)

enum class OrderStatus(val label: String, val colorHex: Long) {
    PENDING_APPROVAL("Pending Owner Approval", 0xFFF59E0B),
    APPROVED("Approved & Delivered", 0xFF10B981),
    REJECTED("Payment Declined", 0xFFEF4444)
}

data class PaymentOrder(
    val orderId: String,
    val customerEmail: String,
    val items: List<CartItem>,
    val totalPayable: Double,
    val totalBalance: Double,
    val paymentMethod: PaymentMethodType,
    val transactionId: String,
    val receiptUri: String?,
    val receiptName: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val status: OrderStatus = OrderStatus.PENDING_APPROVAL,
    val adminNote: String? = null
)

enum class PaymentMethodType(
    val label: String,
    val iconName: String,
    val accountTitle: String = "",
    val accountNumber: String = "",
    val instructions: String = ""
) {
    EASYPAISA(
        label = "Easypaisa Mobile Wallet",
        iconName = "easypaisa",
        accountTitle = "Naila Hussain",
        accountNumber = "03034763324",
        instructions = "Transfer to Easypaisa 03034763324 (Account Name: Naila Hussain). Owner reviews receipt before dispatching digital cards."
    ),
    BINANCE_PAY(
        label = "Binance Pay ID",
        iconName = "binance"
    ),
    CRYPTO_USDT(
        label = "USDT (TRC-20 / BEP-20)",
        iconName = "crypto"
    ),
    CRYPTO_BTC(
        label = "Bitcoin / Lightning",
        iconName = "btc"
    ),
    CREDIT_CARD(
        label = "Credit / Debit Card",
        iconName = "card"
    ),
    GOOGLE_PAY(
        label = "Google Pay",
        iconName = "gpay"
    )
}
