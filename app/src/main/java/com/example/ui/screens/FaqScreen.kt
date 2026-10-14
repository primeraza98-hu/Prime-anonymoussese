package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FaqItem(
    val question: String,
    val answer: String
)

@Composable
fun FaqScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val faqs = remember {
        listOf(
            FaqItem(
                question = "How does this unique digital service work for my daily life?",
                answer = "We issue pre-funded virtual payment cards designed for your everyday online needs. When you purchase a digital service card (e.g. $20 for $200 shopping balance, $22 for $300 crypto, or $25 for $500 Binance balance), your encrypted card credentials (16 digits, CVV, expiry) are delivered instantly inside your in-app Vault ready to spend immediately."
            ),
            FaqItem(
                question = "What is the 'All Shopping Platform' ($20 -> $200) card?",
                answer = "This card gives you $200 in preloaded shopping power for a single $20 purchase. It works globally on major online shopping sites including Amazon, eBay, AliExpress, Walmart, Target, streaming services (Netflix, Spotify), and app stores."
            ),
            FaqItem(
                question = "What is the 'Only Crypto' ($22 -> $300) card?",
                answer = "The 'Only Crypto' card is customized with high privacy protocols for Web3 users, crypto gateways, decentralized exchange settlements, and wallet funding with $300 preloaded balance for $22. No intrusive identity verification is needed."
            ),
            FaqItem(
                question = "What is the 'Binance' ($25 -> $500) card?",
                answer = "Our VIP tier card provides maximum value: $500 preloaded spending balance for just $25. It is optimized for the Binance ecosystem, Binance Pay merchant checkouts, high-limit P2P transfers, and zero foreign exchange fee conversions."
            ),
            FaqItem(
                question = "How fast is delivery after payment?",
                answer = "Delivery is 100% automated! As soon as your checkout is confirmed, your new virtual card is added instantly into your Vault (typically within 30 seconds)."
            ),
            FaqItem(
                question = "Are there any hidden monthly or transaction fees?",
                answer = "None! You only pay the one-time purchase price ($20, $22, or $25). There are zero maintenance fees, no subscription deductions, and no hidden surprises."
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("faq_screen_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Service Guarantee & Support", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("100% Verified Balance & Instant Delivery", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Official Pricing & Balance Matrix",
                        color = Color(0xFFF59E0B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PlanRow(title = "1. All Shopping Platform", cost = "$20", balance = "$200 Balance", multi = "10x Value")
                    Spacer(modifier = Modifier.height(6.dp))
                    PlanRow(title = "2. Only Crypto", cost = "$22", balance = "$300 Balance", multi = "13.6x Value")
                    Spacer(modifier = Modifier.height(6.dp))
                    PlanRow(title = "3. Binance", cost = "$25", balance = "$500 Balance", multi = "20x Value (VIP)")
                }
            }
        }

        item {
            Text(
                text = "Frequently Asked Questions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        items(faqs) { faq ->
            FaqAccordion(faq = faq)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Need Custom Assistance?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Our 24/7 dedicated digital fulfillment desk is standing by.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "Support Ticket #892 opened. Live agent connecting...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Contact 24/7 Live Support", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PlanRow(title: String, cost: String, balance: String, multi: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(multi, color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(cost, color = Color(0xFF94A3B8), fontSize = 12.sp)
                Text(" ➔ ", color = Color(0xFF10B981), fontSize = 12.sp)
                Text(balance, color = Color(0xFFFDE68A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun FaqAccordion(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF334155)))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = faq.answer,
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
