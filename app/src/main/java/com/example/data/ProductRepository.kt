package com.example.data

import com.example.R
import com.example.model.CardTransaction
import com.example.model.DigitalProduct
import com.example.model.PurchasedCard

object ProductRepository {

    val products: List<DigitalProduct> = listOf(
        DigitalProduct(
            id = "prod_all_shopping",
            name = "All Shopping Platform",
            subtitle = "Universal Digital Shopping Service Card",
            priceUsd = 20.0,
            balanceUsd = 200.0,
            category = "Shopping",
            badge = "10x Value ($200 Balance)",
            description = "Unique digital prepaid service card preloaded with $200 ready-to-spend balance for just $20. Usable worldwide across all online retail, e-commerce, and subscription platforms without restrictions.",
            highlights = listOf(
                "Loaded with $200.00 spending balance",
                "Instant digital delivery within 30 seconds",
                "Accepted on all major shopping platforms worldwide",
                "No monthly maintenance or hidden transaction fees",
                "Full 3D Secure dynamic OTP compatibility"
            ),
            supportedPlatforms = listOf(
                "Amazon", "eBay", "AliExpress", "Walmart", "Target",
                "Netflix", "Spotify", "Apple Store", "Google Play"
            ),
            imageRes = R.drawable.card_all_shopping,
            defaultCardholder = "VALUED MEMBER",
            defaultCardNumber = "4567 8901 2345 6789",
            expiry = "12/28",
            cvv = "482"
        ),
        DigitalProduct(
            id = "prod_only_crypto",
            name = "Only Crypto",
            subtitle = "Web3, Wallets & Decentralized Payments",
            priceUsd = 22.0,
            balanceUsd = 300.0,
            category = "Crypto",
            badge = "High Privacy ($300 Balance)",
            description = "Specialized crypto-focused digital card loaded with $300 balance for $22. Built specifically for seamless crypto on-ramps, Web3 gateways, decentralized exchange settlements, and privacy-first transactions.",
            highlights = listOf(
                "Pre-funded with $300.00 crypto spending balance",
                "Zero intrusive KYC verification required",
                "Optimized for crypto wallets & DEX gateways",
                "High privacy with encrypted virtual credentials",
                "Supports multi-chain fiat conversion (USDT/BTC/ETH)"
            ),
            supportedPlatforms = listOf(
                "MetaMask", "Trust Wallet", "Uniswap", "PancakeSwap",
                "Coinbase Pay", "Kraken", "OKX", "Bitget"
            ),
            imageRes = R.drawable.card_only_crypto,
            defaultCardholder = "CRYPTO HOLDER",
            defaultCardNumber = "5421 8765 4321 0987",
            expiry = "10/29",
            cvv = "715"
        ),
        DigitalProduct(
            id = "prod_binance",
            name = "Binance",
            subtitle = "VIP Elite Exchange & P2P High Balance",
            priceUsd = 25.0,
            balanceUsd = 500.0,
            category = "Binance VIP",
            badge = "Max Value ($500 Balance)",
            description = "Top-tier premium VIP card loaded with $500 spending balance for just $25. Dedicated for Binance ecosystem users, Binance Pay, high-limit P2P trading, and priority cross-border settlements.",
            highlights = listOf(
                "Massive $500.00 preloaded spending balance",
                "20x Value return on purchase price ($25 -> $500)",
                "Direct Binance Pay and P2P instant compatibility",
                "0% Foreign exchange conversion fees",
                "Dedicated 24/7 VIP priority support concierge"
            ),
            supportedPlatforms = listOf(
                "Binance Pay", "Binance P2P", "Binance Card Portal",
                "KuCoin", "Bybit", "Bitfinex", "Global Crypto Hub"
            ),
            imageRes = R.drawable.card_binance_gold,
            defaultCardholder = "BINANCE VIP",
            defaultCardNumber = "5420 8800 1234 5678",
            expiry = "10/29",
            cvv = "930"
        )
    )

    fun getInitialPurchasedCards(): List<PurchasedCard> {
        return listOf(
            PurchasedCard(
                id = "vault_sample_1",
                productId = "prod_all_shopping",
                title = "All Shopping Platform",
                balanceRemaining = 200.0,
                originalBalance = 200.0,
                pricePaid = 20.0,
                cardNumber = "4567 •••• •••• 6789",
                expiry = "12/28",
                cvv = "482",
                cardholderName = "ALEXANDER V.",
                isFrozen = false,
                purchaseTimestamp = System.currentTimeMillis() - 86400000L,
                category = "Shopping",
                imageRes = R.drawable.card_all_shopping,
                transactions = listOf(
                    CardTransaction("tx_1", "Amazon Prime", 14.99, System.currentTimeMillis() - 7200000L),
                    CardTransaction("tx_2", "Netflix US", 15.49, System.currentTimeMillis() - 36000000L)
                )
            )
        )
    }
}
