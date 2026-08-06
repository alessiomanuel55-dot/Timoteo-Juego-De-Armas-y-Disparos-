package com.example.ui.shop

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.billing.PlayBillingManager
import com.example.billing.PlayProduct
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayStoreShopDialog(
    billingManager: PlayBillingManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val gems by billingManager.gems.collectAsState()
    val isVipMonthly by billingManager.isVipMonthly.collectAsState()
    val isUltraYearly by billingManager.isUltraYearly.collectAsState()
    val hasGoldenSkin by billingManager.hasGoldenSkin.collectAsState()
    val hasNoAds by billingManager.hasNoAds.collectAsState()
    val statusMessage by billingManager.statusMessage.collectAsState()
    val availableProducts by billingManager.availableProducts.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = InApp Products & Gems, 1 = Subscriptions
    var selectedProductForPayment by remember { mutableStateOf<PlayProduct?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFF00E5FF))), RoundedCornerShape(24.dp)),
            color = Color(0xFF131722)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Bar Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Timo Store",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Timo Store 🛒",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Compras In-App y Suscripciones VIP",
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Gem Balance Capsule & Close Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E2838),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Diamond,
                                    contentDescription = "Gemas",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$gems",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF263238), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // VIP Status Banner if Subscribed
                if (isVipMonthly || isUltraYearly) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF332500)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "VIP Active",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isUltraYearly) "👑 ¡PASE ULTRA GALÁCTICO ACTIVO!" else "👑 ¡SUSCRIPCIÓN TIMOTEO VIP ACTIVA!",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Disfrutas de +50% Monedas, Aura Dorada y Vidas Extra.",
                                    color = Color(0xFFFFF59D),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Notification / Status Toast
                AnimatedVisibility(visible = statusMessage != null) {
                    statusMessage?.let { msg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF003B46)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Status",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = msg,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "OK",
                                    color = Color(0xFF80D8FF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { billingManager.clearStatusMessage() }
                                        .padding(horizontal = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Shop Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1E2838),
                    contentColor = Color(0xFF00E5FF),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF00E5FF)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "💎 Gemas & Paquetes",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) Color(0xFF00E5FF) else Color(0xFFB0BEC5)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "👑 Suscripciones VIP",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) Color(0xFFFFD700) else Color(0xFFB0BEC5)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Product Items List
                val filteredProducts = availableProducts.filter { it.isSubscription == (selectedTab == 1) }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts) { product ->
                        val isOwned = when (product.id) {
                            PlayBillingManager.SKU_GOLDEN_SKIN -> hasGoldenSkin
                            PlayBillingManager.SKU_NO_ADS -> hasNoAds
                            PlayBillingManager.SUB_VIP_MONTHLY -> isVipMonthly
                            PlayBillingManager.SUB_ULTRA_YEARLY -> isUltraYearly
                            else -> false
                        }

                        ProductCardItem(
                            product = product,
                            isOwned = isOwned,
                            onBuyClick = {
                                selectedProductForPayment = product
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer with Restore Purchases & Play Store Disclaimer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { billingManager.restorePurchases() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restaurar",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Restaurar Compras",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Pagos seguros vía Google Payments",
                        color = Color(0xFF78909C),
                        fontSize = 10.sp,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }

    // Modal Sheet Overlay: Official Google Payments Purchase Flow
    selectedProductForPayment?.let { product ->
        GooglePaymentsSheetDialog(
            product = product,
            billingManager = billingManager,
            activity = activity,
            onDismiss = { selectedProductForPayment = null }
        )
    }
}

@Composable
fun GooglePaymentsSheetDialog(
    product: PlayProduct,
    billingManager: PlayBillingManager,
    activity: Activity?,
    onDismiss: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    var processingStep by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("Saldo de Google Play ($15.00)") }

    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(enabled = !isProcessing) { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                color = Color(0xFF202124) // Google Payments Signature Dark Background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Drag Handle Bar
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .background(Color(0xFF5F6368), CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Header: Google Play Brand & App Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF303134)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "G",
                                        color = Color(0xFF4285F4),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Pay",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Gatito Timoteo: Nano Banana",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Google Play Billing Service",
                                    color = Color(0xFF9AA0A6),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // User Account Badge
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF3C4043)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF8AB4F8),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "A",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "alessiomanuel55@gmail.com",
                                    color = Color(0xFFE8EAED),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF3C4043))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Product Details Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = product.description,
                                color = Color(0xFFBDC1C6),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            if (product.isSubscription) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🔄 Renovación automática vía Google Play. Cancela cuando quieras.",
                                    color = Color(0xFF8AB4F8),
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = product.price,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Payment Method Picker Box
                    Text(
                        text = "Forma de pago",
                        color = Color(0xFF9AA0A6),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF303134),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5F6368)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Diamond,
                                    contentDescription = "GPay",
                                    tint = Color(0xFF8AB4F8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = selectedPaymentMethod,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Google Payments Inc. (Protegido)",
                                        color = Color(0xFF9AA0A6),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Text(
                                text = "Cambiar",
                                color = Color(0xFF8AB4F8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Processing State or Action Button
                    if (isProcessing) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2F31)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8AB4F8))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Exitoso",
                                        tint = Color(0xFF81C995),
                                        modifier = Modifier.size(28.dp)
                                    )
                                } else {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color(0xFF8AB4F8),
                                        strokeWidth = 3.dp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = processingStep,
                                    color = if (isCompleted) Color(0xFF81C995) else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                isProcessing = true
                                coroutineScope.launch {
                                    processingStep = "Conectando al servidor de Google Payments..."
                                    kotlinx.coroutines.delay(800)
                                    processingStep = "Verificando token de compra en Google Play..."
                                    kotlinx.coroutines.delay(800)
                                    processingStep = "¡Pago aprobado y confirmado!"
                                    isCompleted = true
                                    kotlinx.coroutines.delay(600)

                                    // Trigger Billing Client Purchase
                                    activity?.let { act ->
                                        billingManager.launchPurchase(act, product)
                                    } ?: run {
                                        billingManager.addGems(product.gemsAmount, "¡Compra completada con éxito!")
                                    }

                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (product.isSubscription) Color(0xFF8AB4F8) else Color(0xFF81C995)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (product.isSubscription) "Suscribirse con Google Play" else "Comprar con 1-clic",
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Al tocar 'Comprar', confirmas que has leído y aceptas las Condiciones del Servicio de Google Payments.",
                        color = Color(0xFF80868B),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCardItem(
    product: PlayProduct,
    isOwned: Boolean,
    onBuyClick: () -> Unit
) {
    val isSub = product.isSubscription
    val accentColor = if (isSub) Color(0xFFFFD700) else Color(0xFF00E5FF)
    val cardBg = if (isSub) Color(0xFF1F1A0A) else Color(0xFF1A2332)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isOwned) Color(0xFF4CAF50) else accentColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Emblem Box
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSub) Icons.Default.Star else Icons.Default.Diamond,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = product.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (product.id == PlayBillingManager.SUB_ULTRA_YEARLY || product.id == PlayBillingManager.SKU_GEMS_500) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFD32F2F)
                            ) {
                                Text(
                                    text = "POPULAR",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = product.description,
                        color = Color(0xFFB0BEC5),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action Button / Price Tag
            if (isOwned && isSub) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1B5E20)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTIVA",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            } else if (isOwned && !isSub) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF263238)
                ) {
                    Text(
                        text = "Comprado",
                        color = Color(0xFF80D8FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else {
                Button(
                    onClick = onBuyClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSub) Color(0xFFFFB300) else Color(0xFF00E5FF)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = product.price,
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
