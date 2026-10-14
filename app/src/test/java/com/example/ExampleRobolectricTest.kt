package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ProductRepository
import com.example.model.OrderStatus
import com.example.model.PaymentMethodType
import com.example.viewmodel.ShopViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Prime Market Hub", appName)
    }

    @Test
    fun `verify Easypaisa payment method credentials`() {
        val easypaisa = PaymentMethodType.EASYPAISA
        assertEquals("Naila Hussain", easypaisa.accountTitle)
        assertEquals("03034763324", easypaisa.accountNumber)
    }

    @Test
    fun `verify three digital card products with exact prices and balances`() {
        val products = ProductRepository.products
        assertEquals(3, products.size)

        // 1st product: All Shopping Platform, price $20, balance $200
        val p1 = products.firstOrNull { it.name.contains("All Shopping", ignoreCase = true) }
        assertNotNull(p1)
        assertEquals(20.0, p1!!.priceUsd, 0.01)
        assertEquals(200.0, p1.balanceUsd, 0.01)

        // 2nd product: Only Crypto, price $22, balance $300
        val p2 = products.firstOrNull { it.name.contains("Crypto", ignoreCase = true) }
        assertNotNull(p2)
        assertEquals(22.0, p2!!.priceUsd, 0.01)
        assertEquals(300.0, p2.balanceUsd, 0.01)

        // 3rd product: Binance, price $25, balance $500
        val p3 = products.firstOrNull { it.name.contains("Binance", ignoreCase = true) }
        assertNotNull(p3)
        assertEquals(25.0, p3!!.priceUsd, 0.01)
        assertEquals(500.0, p3.balanceUsd, 0.01)
    }

    @Test
    fun `test owner payment approval workflow before product delivery`() {
        val viewModel = ShopViewModel()
        val p1 = ProductRepository.products[0] // All Shopping ($20 -> $200)

        viewModel.addToCart(p1, 1)
        val initialPurchasedCardsCount = viewModel.uiState.value.purchasedCards.size

        // Step 1: Customer submits payment (Easypaisa with TRX)
        viewModel.submitCheckoutOrder(
            email = "buyer@example.com",
            paymentMethod = PaymentMethodType.EASYPAISA,
            trxId = "TXN_77492019",
            receiptUri = null,
            receiptName = "receipt_screenshot.png"
        )

        val stateAfterSubmit = viewModel.uiState.value
        assertTrue(stateAfterSubmit.cartItems.isEmpty())
        val pendingOrder = stateAfterSubmit.orders.firstOrNull { it.transactionId == "TXN_77492019" }
        assertNotNull(pendingOrder)
        assertEquals(OrderStatus.PENDING_APPROVAL, pendingOrder!!.status)

        // Customer has NOT received the product yet!
        assertEquals(initialPurchasedCardsCount, stateAfterSubmit.purchasedCards.size)

        // Step 2: App Owner reviews and approves request
        viewModel.approveOrder(pendingOrder.orderId)

        val stateAfterApproval = viewModel.uiState.value
        val approvedOrder = stateAfterApproval.orders.firstOrNull { it.orderId == pendingOrder.orderId }
        assertNotNull(approvedOrder)
        assertEquals(OrderStatus.APPROVED, approvedOrder!!.status)

        // Customer NOW receives the digital card with $200 balance!
        assertEquals(initialPurchasedCardsCount + 1, stateAfterApproval.purchasedCards.size)
        val deliveredCard = stateAfterApproval.purchasedCards.first()
        assertEquals("All Shopping Platform", deliveredCard.title)
        assertEquals(200.0, deliveredCard.balanceRemaining, 0.01)
    }
}
