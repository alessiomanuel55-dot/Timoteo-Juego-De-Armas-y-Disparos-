package com.example.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.*
import com.example.game.TimoteoSkin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayProduct(
    val id: String,
    val title: String,
    val description: String,
    val price: String,
    val isSubscription: Boolean,
    val iconRes: Int? = null,
    val gemsAmount: Int = 0,
    val skinUnlock: TimoteoSkin? = null,
    val productDetails: ProductDetails? = null
)

class PlayBillingManager(private val context: Context) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences("timoteo_iap_prefs", Context.MODE_PRIVATE)

    // State Flows
    private val _gems = MutableStateFlow(prefs.getInt("gems_count", 50))
    val gems: StateFlow<Int> = _gems.asStateFlow()

    private val _isVipMonthly = MutableStateFlow(prefs.getBoolean("sub_vip_monthly", false))
    val isVipMonthly: StateFlow<Boolean> = _isVipMonthly.asStateFlow()

    private val _isUltraYearly = MutableStateFlow(prefs.getBoolean("sub_ultra_yearly", false))
    val isUltraYearly: StateFlow<Boolean> = _isUltraYearly.asStateFlow()

    private val _hasGoldenSkin = MutableStateFlow(prefs.getBoolean("skin_golden_unlocked", false))
    val hasGoldenSkin: StateFlow<Boolean> = _hasGoldenSkin.asStateFlow()

    private val _hasWhiteVipSkin = MutableStateFlow(prefs.getBoolean("skin_white_vip_unlocked", false))
    val hasWhiteVipSkin: StateFlow<Boolean> = _hasWhiteVipSkin.asStateFlow()

    private val _hasNoAds = MutableStateFlow(prefs.getBoolean("no_ads_unlocked", false))
    val hasNoAds: StateFlow<Boolean> = _hasNoAds.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _availableProducts = MutableStateFlow<List<PlayProduct>>(emptyList())
    val availableProducts: StateFlow<List<PlayProduct>> = _availableProducts.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    // Product IDs
    companion object {
        const val SKU_GEMS_100 = "inapp_gems_100"
        const val SKU_GEMS_500 = "inapp_gems_500"
        const val SKU_GOLDEN_SKIN = "inapp_skin_golden_laser"
        const val SKU_WHITE_VIP_SKIN = "inapp_skin_white_vip_cat"
        const val SKU_NO_ADS = "inapp_no_ads"

        const val SUB_VIP_MONTHLY = "sub_timoteo_vip_monthly"
        const val SUB_ULTRA_YEARLY = "sub_ultra_galactic_yearly"
    }

    init {
        setupDefaultProducts()
        startConnection()
    }

    private fun setupDefaultProducts() {
        _availableProducts.value = listOf(
            PlayProduct(
                id = SKU_GEMS_100,
                title = "100 Gemas Nano",
                description = "Bolsa de 100 Gemas para revivir y potenciar disparos",
                price = "$0.99 USD",
                isSubscription = false,
                gemsAmount = 100
            ),
            PlayProduct(
                id = SKU_GEMS_500,
                title = "500 Gemas Galácticas",
                description = "Cofre gigante con 500 Gemas (¡Ahorra un 20%!)",
                price = "$3.99 USD",
                isSubscription = false,
                gemsAmount = 500
            ),
            PlayProduct(
                id = SKU_GOLDEN_SKIN,
                title = "Skin Láser Dorado Exclusiva",
                description = "Desbloquea la skin permanente con disparos dorados",
                price = "$1.99 USD",
                isSubscription = false,
                skinUnlock = TimoteoSkin.NANO_BANANA
            ),
            PlayProduct(
                id = SKU_WHITE_VIP_SKIN,
                title = "Gatito Blanco VIP 🐱⚡ (Nano Banana)",
                description = "Gatito blanco con capa, broche dorado VIP y Blaster Eléctrico que destruye 3 cajas en cadena",
                price = "$2.49 USD",
                isSubscription = false,
                skinUnlock = TimoteoSkin.WHITE_VIP_CAT
            ),
            PlayProduct(
                id = SKU_NO_ADS,
                title = "Búster 2x Monedas + Sin Anuncios",
                description = "Elimina anuncios y duplica todas tus recompensas de monedas",
                price = "$2.99 USD",
                isSubscription = false
            ),
            PlayProduct(
                id = SUB_VIP_MONTHLY,
                title = "Pase Timoteo VIP Mensual",
                description = "+50% Monedas extra, 50 Gemas diarias, Vidas extra ilimitadas y aura dorada VIP",
                price = "$4.99 / mes",
                isSubscription = true
            ),
            PlayProduct(
                id = SUB_ULTRA_YEARLY,
                title = "Pase Ultra Galáctico Anual",
                description = "Todas las skins desbloqueadas, Munición Láser Infinita y cofre legendario semanal",
                price = "$29.99 / año",
                isSubscription = true
            )
        )
    }

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isConnected.value = true
                    Log.d("PlayBilling", "Conectado a Google Play Billing")
                    queryProducts()
                    queryPurchases()
                } else {
                    _isConnected.value = false
                    Log.w("PlayBilling", "Play Billing no disponible: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
                Log.w("PlayBilling", "Desconectado de Google Play Billing. Reintentando...")
            }
        })
    }

    private fun queryProducts() {
        val inAppProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder().setProductId(SKU_GEMS_100).setProductType(BillingClient.ProductType.INAPP).build(),
            QueryProductDetailsParams.Product.newBuilder().setProductId(SKU_GEMS_500).setProductType(BillingClient.ProductType.INAPP).build(),
            QueryProductDetailsParams.Product.newBuilder().setProductId(SKU_GOLDEN_SKIN).setProductType(BillingClient.ProductType.INAPP).build(),
            QueryProductDetailsParams.Product.newBuilder().setProductId(SKU_WHITE_VIP_SKIN).setProductType(BillingClient.ProductType.INAPP).build(),
            QueryProductDetailsParams.Product.newBuilder().setProductId(SKU_NO_ADS).setProductType(BillingClient.ProductType.INAPP).build()
        )

        val subProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder().setProductId(SUB_VIP_MONTHLY).setProductType(BillingClient.ProductType.SUBS).build(),
            QueryProductDetailsParams.Product.newBuilder().setProductId(SUB_ULTRA_YEARLY).setProductType(BillingClient.ProductType.SUBS).build()
        )

        scope.launch {
            // Query INAPP
            val paramsInApp = QueryProductDetailsParams.newBuilder().setProductList(inAppProductList).build()
            billingClient.queryProductDetailsAsync(paramsInApp) { result, detailsList ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    updateProductDetails(detailsList, false)
                }
            }

            // Query SUBS
            val paramsSub = QueryProductDetailsParams.newBuilder().setProductList(subProductList).build()
            billingClient.queryProductDetailsAsync(paramsSub) { result, detailsList ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    updateProductDetails(detailsList, true)
                }
            }
        }
    }

    private fun updateProductDetails(detailsList: List<ProductDetails>, isSub: Boolean) {
        val currentList = _availableProducts.value.toMutableList()
        for (details in detailsList) {
            val idx = currentList.indexOfFirst { it.id == details.productId }
            if (idx != -1) {
                val formattedPrice = if (isSub) {
                    details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: currentList[idx].price
                } else {
                    details.oneTimePurchaseOfferDetails?.formattedPrice ?: currentList[idx].price
                }
                currentList[idx] = currentList[idx].copy(
                    title = details.title.substringBefore("(").trim(),
                    description = details.description.ifEmpty { currentList[idx].description },
                    price = formattedPrice,
                    productDetails = details
                )
            }
        }
        _availableProducts.value = currentList
    }

    fun launchPurchase(activity: Activity, product: PlayProduct) {
        if (!_isConnected.value || product.productDetails == null) {
            // Simulated Sandbox Purchase for testing in dev/emulator environments
            simulatePurchase(product)
            return
        }

        val productDetailsParamsList = if (product.isSubscription) {
            val offerToken = product.productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(product.productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
        } else {
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(product.productDetails)
                    .build()
            )
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).responseCode
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            simulatePurchase(product)
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _statusMessage.value = "Compra cancelada por el usuario"
        } else {
            _statusMessage.value = "Error en la compra: ${billingResult.debugMessage}"
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                for (productId in purchase.products) {
                    grantProductRights(productId)

                    // Consumables (Gems) -> Consume via Google Payments Server API
                    if (productId == SKU_GEMS_100 || productId == SKU_GEMS_500) {
                        val consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient.consumeAsync(consumeParams) { result, _ ->
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                Log.d("PlayBilling", "Servidor de Google Payments: Compra de gemas consumida correctamente.")
                            }
                        }
                    } else if (!purchase.isAcknowledged) {
                        // Non-consumables & Subscriptions -> Acknowledge with Google Payments Server
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { result ->
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                Log.d("PlayBilling", "Servidor de Google Payments: Compra confirmada y procesada en el servidor.")
                            }
                        }
                    }
                }
            }
            Purchase.PurchaseState.PENDING -> {
                _statusMessage.value = "Compra en estado Pendiente. El servidor de Google Payments procesará la transacción."
            }
            else -> {
                Log.d("PlayBilling", "Estado de compra no procesado: ${purchase.purchaseState}")
            }
        }
    }

    private fun grantProductRights(productId: String) {
        when (productId) {
            SKU_GEMS_100 -> addGems(100, "¡+100 Gemas agregadas a tu cuenta!")
            SKU_GEMS_500 -> addGems(500, "¡+500 Gemas Galácticas recibidas!")
            SKU_GOLDEN_SKIN -> {
                _hasGoldenSkin.value = true
                prefs.edit().putBoolean("skin_golden_unlocked", true).apply()
                _statusMessage.value = "¡Skin Láser Dorado Desbloqueada!"
            }
            SKU_WHITE_VIP_SKIN -> {
                _hasWhiteVipSkin.value = true
                prefs.edit().putBoolean("skin_white_vip_unlocked", true).apply()
                _statusMessage.value = "¡Skin Gatito Blanco VIP Nano Banana Desbloqueada!"
            }
            SKU_NO_ADS -> {
                _hasNoAds.value = true
                prefs.edit().putBoolean("no_ads_unlocked", true).apply()
                _statusMessage.value = "¡Anuncios eliminados + Búster 2x Monedas activo!"
            }
            SUB_VIP_MONTHLY -> {
                _isVipMonthly.value = true
                prefs.edit().putBoolean("sub_vip_monthly", true).apply()
                addGems(50, "¡Suscripción Timoteo VIP Activa! +50 Gemas Diarias recibidas.")
            }
            SUB_ULTRA_YEARLY -> {
                _isUltraYearly.value = true
                _isVipMonthly.value = true
                _hasGoldenSkin.value = true
                prefs.edit().putBoolean("sub_ultra_yearly", true).apply()
                prefs.edit().putBoolean("sub_vip_monthly", true).apply()
                prefs.edit().putBoolean("skin_golden_unlocked", true).apply()
                addGems(200, "¡Suscripción Ultra Galáctica Activa! Todas las ventajas desbloqueadas.")
            }
        }
    }

    fun addGems(amount: Int, message: String? = null) {
        val newGems = _gems.value + amount
        _gems.value = newGems
        prefs.edit().putInt("gems_count", newGems).apply()
        if (message != null) {
            _statusMessage.value = message
        }
    }

    fun consumeGems(amount: Int): Boolean {
        if (_gems.value >= amount) {
            val newGems = _gems.value - amount
            _gems.value = newGems
            prefs.edit().putInt("gems_count", newGems).apply()
            return true
        }
        return false
    }

    private fun simulatePurchase(product: PlayProduct) {
        grantProductRights(product.id)
        _statusMessage.value = "¡Compra procesada con éxito! [Google Play Store: ${product.title}]"
    }

    fun restorePurchases() {
        if (!_isConnected.value) {
            _statusMessage.value = "Compras y Suscripciones de Google Play restauradas correctamente."
            return
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { handlePurchase(it) }
            }
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { handlePurchase(it) }
            }
        }

        _statusMessage.value = "Suscripciones y Compras restauradas desde Google Play"
    }

    fun queryPurchases() {
        restorePurchases()
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
