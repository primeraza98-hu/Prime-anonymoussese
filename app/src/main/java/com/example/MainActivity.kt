package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.OrderStatus
import com.example.model.PaymentOrder
import com.example.model.PurchasedCard
import com.example.ui.screens.CartScreen
import com.example.ui.screens.FaqScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OwnerApprovalScreen
import com.example.ui.screens.ProductDetailDialog
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ShopViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: ShopViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val pendingApprovalsCount = uiState.orders.count { it.status == OrderStatus.PENDING_APPROVAL }

    // Back handling: if not on Home screen, Back returns to Home screen
    BackHandler(enabled = uiState.activeTab != 0 || uiState.selectedProductForDetail != null) {
        if (uiState.selectedProductForDetail != null) {
            viewModel.closeProductDetail()
        } else {
            viewModel.setActiveTab(0)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBarFintech(
                cartItemCount = uiState.cartItems.sumOf { it.quantity },
                vaultBalance = uiState.purchasedCards.sumOf { it.balanceRemaining },
                pendingApprovalsCount = pendingApprovalsCount,
                onCartClick = { viewModel.setActiveTab(1) },
                onVaultClick = { viewModel.setActiveTab(2) },
                onOwnerPortalClick = { viewModel.setActiveTab(3) }
            )
        },
        bottomBar = {
            FintechBottomNavigation(
                activeTab = uiState.activeTab,
                cartCount = uiState.cartItems.sumOf { it.quantity },
                vaultCount = uiState.purchasedCards.size,
                pendingApprovalsCount = pendingApprovalsCount,
                onTabSelect = { viewModel.setActiveTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0A0F1D))
        ) {
            when (uiState.activeTab) {
                0 -> HomeScreen(
                    uiState = uiState,
                    onCategorySelect = { viewModel.selectCategory(it) },
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onProductClick = { viewModel.openProductDetail(it) },
                    onAddToCart = { product ->
                        viewModel.addToCart(product)
                        Toast.makeText(context, "Added ${product.name} to cart", Toast.LENGTH_SHORT).show()
                    },
                    onInstantBuyClick = { product ->
                        viewModel.openProductDetail(product)
                    },
                    onNavigateToFaq = { viewModel.setActiveTab(4) }
                )
                1 -> CartScreen(
                    uiState = uiState,
                    onQuantityChange = { id, delta -> viewModel.updateCartQuantity(id, delta) },
                    onRemoveItem = { viewModel.removeFromCart(it) },
                    onApplyPromo = { viewModel.applyPromoCode(it) },
                    onCheckout = { email, method, trx, uri, name ->
                        viewModel.submitCheckoutOrder(email, method, trx, uri, name)
                    },
                    onBrowseCatalog = { viewModel.setActiveTab(0) }
                )
                2 -> VaultScreen(
                    purchasedCards = uiState.purchasedCards,
                    pendingOrders = uiState.orders,
                    onFreezeToggle = { viewModel.toggleFreezeCard(it) },
                    onSimulateSpend = { cardId, merchant, amount ->
                        viewModel.simulateSpend(cardId, merchant, amount)
                        Toast.makeText(context, "Spent $${String.format("%.2f", amount)} at $merchant", Toast.LENGTH_SHORT).show()
                    },
                    onNavigateToStore = { viewModel.setActiveTab(0) }
                )
                3 -> OwnerApprovalScreen(
                    orders = uiState.orders,
                    onApproveOrder = { viewModel.approveOrder(it) },
                    onRejectOrder = { viewModel.rejectOrder(it) }
                )
                4 -> FaqScreen()
            }

            // Product Detail Dialog
            uiState.selectedProductForDetail?.let { product ->
                ProductDetailDialog(
                    product = product,
                    onDismiss = { viewModel.closeProductDetail() },
                    onAddToCart = { prod, qty ->
                        viewModel.addToCart(prod, qty)
                        Toast.makeText(context, "Added $qty x ${prod.name} to cart", Toast.LENGTH_SHORT).show()
                    },
                    onInstantBuy = { prod, qty, email, method, trx, uri, name ->
                        viewModel.submitInstantBuyOrder(prod, qty, email, method, trx, uri, name)
                    }
                )
            }

            // Payment Approval Pending Dialog (Customer View)
            if (uiState.showOrderPendingDialog && uiState.lastSubmittedOrder != null) {
                OrderPendingApprovalDialog(
                    order = uiState.lastSubmittedOrder!!,
                    onDismiss = { viewModel.dismissOrderPendingDialog() },
                    onViewVault = {
                        viewModel.dismissOrderPendingDialog()
                        viewModel.setActiveTab(2)
                    },
                    onOpenOwnerPortal = {
                        viewModel.dismissOrderPendingDialog()
                        viewModel.setActiveTab(3)
                    }
                )
            }
        }
    }
}

@Composable
fun TopAppBarFintech(
    cartItemCount: Int,
    vaultBalance: Double,
    pendingApprovalsCount: Int,
    onCartClick: () -> Unit,
    onVaultClick: () -> Unit,
    onOwnerPortalClick: () -> Unit
) {
    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Identity
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "Logo",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Prime Market Hub",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Digital Life Services",
                        fontSize = 10.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Right Actions: Owner Portal quick access, Vault Balance & Cart
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Owner Portal pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (pendingApprovalsCount > 0) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (pendingApprovalsCount > 0) Color(0xFFF59E0B) else Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .clickable { onOwnerPortalClick() }
                        .testTag("top_bar_owner_portal_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Owner Portal",
                            tint = if (pendingApprovalsCount > 0) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (pendingApprovalsCount > 0) "Owner ($pendingApprovalsCount)" else "Owner",
                            color = if (pendingApprovalsCount > 0) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Vault balance chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.clickable { onVaultClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Vault Balance",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$${String.format("%.0f", vaultBalance)}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Cart Icon with Badge
                IconButton(
                    onClick = onCartClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("top_bar_cart_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (cartItemCount > 0) {
                                Badge(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.Black
                                ) {
                                    Text("$cartItemCount", fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Shopping Cart",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FintechBottomNavigation(
    activeTab: Int,
    cartCount: Int,
    vaultCount: Int,
    pendingApprovalsCount: Int,
    onTabSelect: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0F172A),
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = activeTab == 0,
            onClick = { onTabSelect(0) },
            icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Store") },
            label = { Text("Store", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = Color(0xFF10B981),
                unselectedIconColor = Color(0xFF94A3B8),
                selectedTextColor = Color(0xFF10B981),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_item_store")
        )

        NavigationBarItem(
            selected = activeTab == 1,
            onClick = { onTabSelect(1) },
            icon = {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(containerColor = Color(0xFF10B981), contentColor = Color.Black) {
                                Text("$cartCount", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                }
            },
            label = { Text("Cart", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = Color(0xFF10B981),
                unselectedIconColor = Color(0xFF94A3B8),
                selectedTextColor = Color(0xFF10B981),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_item_cart")
        )

        NavigationBarItem(
            selected = activeTab == 2,
            onClick = { onTabSelect(2) },
            icon = {
                BadgedBox(
                    badge = {
                        if (vaultCount > 0) {
                            Badge(containerColor = Color(0xFF10B981), contentColor = Color.Black) {
                                Text("$vaultCount", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = "Vault")
                }
            },
            label = { Text("My Cards", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = Color(0xFF10B981),
                unselectedIconColor = Color(0xFF94A3B8),
                selectedTextColor = Color(0xFF10B981),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_item_vault")
        )

        NavigationBarItem(
            selected = activeTab == 3,
            onClick = { onTabSelect(3) },
            icon = {
                BadgedBox(
                    badge = {
                        if (pendingApprovalsCount > 0) {
                            Badge(containerColor = Color(0xFFF59E0B), contentColor = Color.Black) {
                                Text("$pendingApprovalsCount", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Owner Approvals")
                }
            },
            label = { Text("Approvals", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = Color(0xFFF59E0B),
                unselectedIconColor = Color(0xFF94A3B8),
                selectedTextColor = Color(0xFFF59E0B),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_item_owner_approvals")
        )

        NavigationBarItem(
            selected = activeTab == 4,
            onClick = { onTabSelect(4) },
            icon = { Icon(Icons.Default.HelpOutline, contentDescription = "Guide") },
            label = { Text("Guide", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = Color(0xFF10B981),
                unselectedIconColor = Color(0xFF94A3B8),
                selectedTextColor = Color(0xFF10B981),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_item_guide")
        )
    }
}

@Composable
fun OrderPendingApprovalDialog(
    order: PaymentOrder,
    onDismiss: () -> Unit,
    onViewVault: () -> Unit,
    onOpenOwnerPortal: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .testTag("order_pending_approval_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                    modifier = Modifier.size(62.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = "Pending Approval",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Request Sent to App Owner!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Order #${order.orderId} has been submitted for owner payment verification.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Payment Gateway:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(order.paymentMethod.label, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TRX ID:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(order.transactionId, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Balance to Unlock:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("$${String.format("%.0f", order.totalBalance)} USD", color = Color(0xFFF59E0B), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "💡 As soon as the app owner reviews and accepts your payment (Easypaisa: Naila Hussain 03034763324), your virtual card will appear directly in your Vault.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onViewVault,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.Black)
                ) {
                    Text("Check Status in My Vault", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Fast track testing button for the app owner
                OutlinedButton(
                    onClick = onOpenOwnerPortal,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("App Owner: Review & Accept Now", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
