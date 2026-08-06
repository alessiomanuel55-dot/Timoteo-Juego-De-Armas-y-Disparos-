package com.example.game

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.drawscope.clipPath
import com.example.R
import com.example.ui.theme.DarkGameBg
import com.example.ui.theme.DarkGameCard
import com.example.ui.theme.HeartRed
import com.example.ui.theme.LaserOrange
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.WoodBrown
import com.example.ui.theme.WoodDark
import com.example.ui.theme.YellowLaser
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class TimoteoSkin(
    val id: String,
    val title: String,
    val badge: String,
    val description: String,
    val iconRes: Int
) {
    NANO_BANANA("nano_banana", "Nano Banana 🍌", "NANO BANANA 🍌", "Gatito Negro Tierno con Blaster Nano Banana", R.drawable.ic_timoteo_nanobanana),
    HD_CAT("hd_cat", "Gato Negro HD 🐱", "GATO NEGRO HD 🐱", "Gatito Negro Full Body Ultra HD", R.drawable.ic_timoteo_cat),
    VECTORIAL("vectorial", "Vectorial 🎨", "VECTORIAL 🎨", "Diseño Vectorial Procedural", R.drawable.ic_timoteo_nanobanana)
}

@Composable
fun TimoteoGameView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("timoteo_game_prefs", Context.MODE_PRIVATE) }
    var highScore by remember { mutableIntStateOf(prefs.getInt("high_score", 0)) }

    val soundManager = remember { SoundManager() }

    // Skins Bitmaps
    val nanoBananaBitmap = ImageBitmap.imageResource(id = R.drawable.ic_timoteo_nanobanana)
    val hdCatBitmap = ImageBitmap.imageResource(id = R.drawable.ic_timoteo_cat)
    var selectedSkin by remember { mutableStateOf(TimoteoSkin.NANO_BANANA) }

    // Game states
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var cratesDestroyed by remember { mutableIntStateOf(0) }
    var shotsFired by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var comboCount by remember { mutableIntStateOf(0) }

    var isGameOver by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }

    // Entities
    val bullets = remember { mutableStateListOf<Bullet>() }
    val crates = remember { mutableStateListOf<Crate>() }
    val particles = remember { mutableStateListOf<Particle>() }
    val floatingTexts = remember { mutableStateListOf<FloatingText>() }
    val muzzleFlashes = remember { mutableStateListOf<MuzzleFlash>() }
    val powerUps = remember { mutableStateListOf<ActivePowerUp>() }
    val droppedPowerUps = remember { mutableStateListOf<DroppedPowerUp>() }

    var screenWidth by remember { mutableFloatStateOf(1080f) }
    var screenHeight by remember { mutableFloatStateOf(1920f) }

    // Timoteo position and gun angle
    var catX by remember { mutableFloatStateOf(0f) }
    var targetCatX by remember { mutableFloatStateOf(0f) }
    var catY by remember { mutableFloatStateOf(1650f) }
    var gunAngleRad by remember { mutableFloatStateOf(-Math.PI.toFloat() / 2) }
    var recoilOffset by remember { mutableFloatStateOf(0f) }
    var screenShake by remember { mutableFloatStateOf(0f) }
    var walkAnimPhase by remember { mutableFloatStateOf(0f) }
    var isWalking by remember { mutableStateOf(false) }

    var lastCrateSpawnTime by remember { mutableLongStateOf(0L) }
    var nextEntityId by remember { mutableLongStateOf(1L) }

    fun restartGame() {
        bullets.clear()
        crates.clear()
        particles.clear()
        floatingTexts.clear()
        muzzleFlashes.clear()
        powerUps.clear()
        droppedPowerUps.clear()

        score = 0
        lives = 3
        cratesDestroyed = 0
        shotsFired = 0
        hits = 0
        comboCount = 0
        isGameOver = false
        isPaused = false
        targetCatX = if (screenWidth > 0) screenWidth / 2f else 540f
        catX = targetCatX
        gunAngleRad = -Math.PI.toFloat() / 2
        recoilOffset = 0f
        screenShake = 0f
        walkAnimPhase = 0f
        isWalking = false
    }

    // Trigger shoot bullet towards target touch coordinate (targetX, targetY)
    fun fireWeapon(targetX: Float, targetY: Float) {
        if (isGameOver || isPaused) return

        // Set target horizontal position for Timoteo to walk towards
        targetCatX = targetX.coerceIn(70f, (screenWidth - 70f).coerceAtLeast(100f))

        val dx = targetX - catX
        val dy = targetY - (catY - 50f)
        val angle = atan2(dy, dx)
        gunAngleRad = angle
        recoilOffset = 18f
        shotsFired++

        // Muzzle flash at gun tip
        val gunLength = 70f
        val muzzleX = catX + cos(angle) * gunLength
        val muzzleY = (catY - 50f) + sin(angle) * gunLength
        muzzleFlashes.add(MuzzleFlash(muzzleX, muzzleY, angle))

        soundManager.playLaserSound()

        val bulletSpeed = 32f

        val isTripleShotActive = powerUps.any { it.type == PowerUpType.TRIPLE_SHOT }

        if (isTripleShotActive) {
            val angles = listOf(angle - 0.20f, angle, angle + 0.20f)
            angles.forEach { a ->
                bullets.add(
                    Bullet(
                        id = nextEntityId++,
                        x = catX + cos(a) * gunLength,
                        y = (catY - 50f) + sin(a) * gunLength,
                        vx = cos(a) * bulletSpeed,
                        vy = sin(a) * bulletSpeed,
                        color = YellowLaser,
                        isPowerShot = true
                    )
                )
            }
        } else {
            bullets.add(
                Bullet(
                    id = nextEntityId++,
                    x = muzzleX,
                    y = muzzleY,
                    vx = cos(angle) * bulletSpeed,
                    vy = sin(angle) * bulletSpeed,
                    color = YellowLaser,
                    isPowerShot = false
                )
            )
        }
    }

    // Main 60FPS Game Loop
    LaunchedEffect(isGameOver, isPaused) {
        var previousFrameTimeNanos = 0L

        while (!isGameOver && !isPaused) {
            withFrameNanos { frameTimeNanos ->
                if (previousFrameTimeNanos == 0L) {
                    previousFrameTimeNanos = frameTimeNanos
                }
                val deltaTimeSec = (frameTimeNanos - previousFrameTimeNanos) / 1_000_000_000f
                previousFrameTimeNanos = frameTimeNanos

                val nowMs = System.currentTimeMillis()

                // 1. Update recoil, Timoteo horizontal walking, and screen shake decay
                val dxCat = targetCatX - catX
                if (kotlin.math.abs(dxCat) > 2f) {
                    val walkSpeed = 16f
                    val step = if (dxCat > 0) walkSpeed else -walkSpeed
                    if (kotlin.math.abs(step) >= kotlin.math.abs(dxCat)) {
                        catX = targetCatX
                    } else {
                        catX += step
                    }
                    walkAnimPhase += 0.35f
                    isWalking = true
                } else {
                    catX = targetCatX
                    isWalking = false
                    walkAnimPhase = 0f
                }

                if (recoilOffset > 0) {
                    recoilOffset = (recoilOffset - 2.5f).coerceAtLeast(0f)
                }
                if (screenShake > 0) {
                    screenShake = (screenShake - 1.5f).coerceAtLeast(0f)
                }

                // Update active power ups
                val iterator = powerUps.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.durationMs -= 16
                    if (p.durationMs <= 0) {
                        iterator.remove()
                    }
                }

                val isSlowMoActive = powerUps.any { it.type == PowerUpType.SLOW_MO }
                val speedMultiplier = if (isSlowMoActive) 0.45f else 1.0f

                // 2. Spawn Crates periodically
                val spawnInterval = (1800 - (score * 15).coerceAtMost(1200)).coerceAtLeast(600)
                if (nowMs - lastCrateSpawnTime > spawnInterval) {
                    lastCrateSpawnTime = nowMs

                    val crateWidth = 110f
                    val crateHeight = 110f
                    val spawnX = Random.nextFloat() * (screenWidth - crateWidth - 80f) + 40f

                    // Determine crate type based on score
                    val roll = Random.nextFloat()
                    val crateType = when {
                        roll < 0.12f && score > 5 -> CrateType.TNT
                        roll < 0.28f && score > 3 -> CrateType.STEEL
                        roll < 0.40f -> CrateType.GOLD
                        else -> CrateType.WOOD
                    }

                    val baseSpeed = 3.5f + (score * 0.08f).coerceAtMost(6.0f)

                    crates.add(
                        Crate(
                            id = nextEntityId++,
                            x = spawnX,
                            y = -crateHeight,
                            width = crateWidth,
                            height = crateHeight,
                            speedY = baseSpeed,
                            type = crateType,
                            hp = crateType.baseHp,
                            maxHp = crateType.baseHp,
                            rotationSpeed = (Random.nextFloat() - 0.5f) * 1.5f
                        )
                    )
                }

                // 3. Move Bullets
                val bulletIter = bullets.iterator()
                while (bulletIter.hasNext()) {
                    val bullet = bulletIter.next()
                    bullet.x += bullet.vx
                    bullet.y += bullet.vy

                    // Remove off-screen bullets
                    if (bullet.x < -50 || bullet.x > screenWidth + 50 || bullet.y < -50 || bullet.y > screenHeight + 50) {
                        bulletIter.remove()
                    }
                }

                // 4. Move Crates & Check Ground Collision
                val crateIter = crates.iterator()
                while (crateIter.hasNext()) {
                    val crate = crateIter.next()
                    crate.y += crate.speedY * speedMultiplier
                    crate.rotationAngle += crate.rotationSpeed

                    // Check if crate hit the ground line (Timoteo's zone)
                    if (crate.y + crate.height >= catY - 20f) {
                        crateIter.remove()
                        lives--
                        comboCount = 0
                        screenShake = 22f
                        soundManager.playLifeLostSound()

                        // Ground splash particles
                        for (i in 0..15) {
                            particles.add(
                                Particle(
                                    x = crate.x + crate.width / 2,
                                    y = catY,
                                    vx = (Random.nextFloat() - 0.5f) * 12f,
                                    vy = -Random.nextFloat() * 10f,
                                    radius = Random.nextFloat() * 8f + 4f,
                                    color = HeartRed,
                                    life = 1f,
                                    maxLife = 1f,
                                    type = ParticleType.SPARK
                                )
                            )
                        }

                        floatingTexts.add(
                            FloatingText(
                                id = nextEntityId++,
                                text = "-1 VIDA",
                                x = crate.x + crate.width / 2,
                                y = catY - 40f,
                                color = HeartRed
                            )
                        )

                        if (lives <= 0) {
                            isGameOver = true
                            soundManager.playGameOverSound()

                            if (score > highScore) {
                                highScore = score
                                prefs.edit().putInt("high_score", highScore).apply()
                            }
                        }
                    }
                }

                // 5. Move Dropped PowerUps
                val powerUpIter = droppedPowerUps.iterator()
                while (powerUpIter.hasNext()) {
                    val p = powerUpIter.next()
                    p.y += p.vy
                    if (p.y > catY - 30f) {
                        // Collect powerup
                        powerUpIter.remove()
                        soundManager.playComboSound(5)

                        if (p.type == PowerUpType.BOMB) {
                            // Destroy all current crates on screen
                            crates.forEach { crate ->
                                for (i in 0..12) {
                                    particles.add(
                                        Particle(
                                            x = crate.x + crate.width / 2,
                                            y = crate.y + crate.height / 2,
                                            vx = (Random.nextFloat() - 0.5f) * 14f,
                                            vy = (Random.nextFloat() - 0.5f) * 14f,
                                            radius = Random.nextFloat() * 8f + 4f,
                                            color = YellowLaser,
                                            life = 1f,
                                            maxLife = 1f,
                                            type = ParticleType.EXPLOSION_FIRE
                                        )
                                    )
                                }
                            }
                            score += crates.size * 2
                            cratesDestroyed += crates.size
                            crates.clear()

                            floatingTexts.add(
                                FloatingText(
                                    id = nextEntityId++,
                                    text = "¡BOMBAZO!",
                                    x = catX,
                                    y = catY - 120f,
                                    color = NeonGreen
                                )
                            )
                        } else {
                            powerUps.add(ActivePowerUp(p.type, p.type.duration))
                            floatingTexts.add(
                                FloatingText(
                                    id = nextEntityId++,
                                    text = "¡${p.type.title}!",
                                    x = catX,
                                    y = catY - 120f,
                                    color = p.type.color
                                )
                            )
                        }
                    } else if (p.y > screenHeight + 50) {
                        powerUpIter.remove()
                    }
                }

                // 6. Bullet vs Crate Collision Detection
                val bulletIter2 = bullets.iterator()
                while (bulletIter2.hasNext()) {
                    val bullet = bulletIter2.next()
                    var bulletHit = false

                    val crateIter2 = crates.iterator()
                    while (crateIter2.hasNext()) {
                        val crate = crateIter2.next()

                        // AABB vs Circle Collision
                        val closestX = bullet.x.coerceIn(crate.x, crate.x + crate.width)
                        val closestY = bullet.y.coerceIn(crate.y, crate.y + crate.height)
                        val distX = bullet.x - closestX
                        val distY = bullet.y - closestY
                        val distanceSq = (distX * distX) + (distY * distY)

                        if (distanceSq < (bullet.radius * bullet.radius)) {
                            bulletHit = true
                            hits++
                            crate.hp--

                            // Spark particles on impact
                            for (i in 0..6) {
                                particles.add(
                                    Particle(
                                        x = bullet.x,
                                        y = bullet.y,
                                        vx = (Random.nextFloat() - 0.5f) * 10f,
                                        vy = (Random.nextFloat() - 0.5f) * 10f,
                                        radius = Random.nextFloat() * 5f + 2f,
                                        color = YellowLaser,
                                        life = 1f,
                                        maxLife = 1f,
                                        type = ParticleType.SPARK
                                    )
                                )
                            }

                            if (crate.hp <= 0) {
                                crateIter2.remove()
                                cratesDestroyed++
                                comboCount++

                                soundManager.playExplosionSound()
                                if (comboCount > 2) {
                                    soundManager.playComboSound(comboCount)
                                }

                                val pointsEarned = crate.type.points * (1 + comboCount / 5)
                                score += pointsEarned

                                // Wood Explosion Particles
                                val particleCount = if (crate.type == CrateType.TNT) 25 else 14
                                val pColor = if (crate.type == CrateType.TNT) HeartRed else crate.type.color

                                for (i in 0..particleCount) {
                                    particles.add(
                                        Particle(
                                            x = crate.x + crate.width / 2,
                                            y = crate.y + crate.height / 2,
                                            vx = (Random.nextFloat() - 0.5f) * 16f,
                                            vy = (Random.nextFloat() - 0.5f) * 16f,
                                            radius = Random.nextFloat() * 10f + 4f,
                                            color = pColor,
                                            life = 1f,
                                            maxLife = 1f,
                                            type = if (crate.type == CrateType.TNT) ParticleType.EXPLOSION_FIRE else ParticleType.WOOD_CHIP
                                        )
                                    )
                                }

                                // Score popup
                                val comboText = if (comboCount >= 3) " COMBO x$comboCount!" else ""
                                floatingTexts.add(
                                    FloatingText(
                                        id = nextEntityId++,
                                        text = "+$pointsEarned$comboText",
                                        x = crate.x + crate.width / 2,
                                        y = crate.y,
                                        color = if (comboCount >= 3) NeonCyan else YellowLaser
                                    )
                                )

                                // Handle TNT explosion radius
                                if (crate.type == CrateType.TNT) {
                                    screenShake = 18f
                                    // Destroy adjacent crates
                                    val tntCenterX = crate.x + crate.width / 2
                                    val tntCenterY = crate.y + crate.height / 2

                                    val tntCrateIter = crates.iterator()
                                    while (tntCrateIter.hasNext()) {
                                        val otherCrate = tntCrateIter.next()
                                        val oCenterX = otherCrate.x + otherCrate.width / 2
                                        val oCenterY = otherCrate.y + otherCrate.height / 2
                                        val distToTNT = sqrt((oCenterX - tntCenterX) * (oCenterX - tntCenterX) + (oCenterY - tntCenterY) * (oCenterY - tntCenterY))
                                        if (distToTNT < 320f) {
                                            tntCrateIter.remove()
                                            cratesDestroyed++
                                            score += otherCrate.type.points
                                        }
                                    }
                                }

                                // Chance to drop power up
                                if (Random.nextFloat() < 0.15f) {
                                    val powerTypes = PowerUpType.values()
                                    val chosenPower = powerTypes[Random.nextInt(powerTypes.size)]
                                    droppedPowerUps.add(
                                        DroppedPowerUp(
                                            id = nextEntityId++,
                                            type = chosenPower,
                                            x = crate.x + crate.width / 2,
                                            y = crate.y + crate.height / 2
                                        )
                                    )
                                }
                            }
                            break
                        }
                    }

                    if (bulletHit) {
                        bulletIter2.remove()
                    }
                }

                // 7. Update Particles
                val particleIter = particles.iterator()
                while (particleIter.hasNext()) {
                    val p = particleIter.next()
                    p.x += p.vx
                    p.y += p.vy
                    p.vy += 0.35f // Gravity
                    p.life -= 0.035f
                    if (p.life <= 0) {
                        particleIter.remove()
                    }
                }

                // 8. Update Floating Texts
                val textIter = floatingTexts.iterator()
                while (textIter.hasNext()) {
                    val ft = textIter.next()
                    ft.y += ft.vy
                    ft.life -= 0.025f
                    if (ft.life <= 0) {
                        textIter.remove()
                    }
                }

                // 9. Update Muzzle Flashes
                val flashIter = muzzleFlashes.iterator()
                while (flashIter.hasNext()) {
                    val mf = flashIter.next()
                    mf.life -= 0.25f
                    if (mf.life <= 0) {
                        flashIter.remove()
                    }
                }
            }
        }
    }

    soundManager.isMuted = isMuted

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGameBg)
    ) {
        // Fullscreen Touch Canvas for Game World Rendering & Pointer Input
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isGameOver, isPaused) {
                    detectTapGestures { offset ->
                        fireWeapon(offset.x, offset.y)
                    }
                }
        ) {
            screenWidth = size.width
            screenHeight = size.height
            if (targetCatX == 0f) {
                targetCatX = size.width / 2f
                catX = targetCatX
            }
            catY = size.height - 180f

            // Handle screen shake translation offset
            val shakeOffsetX = if (screenShake > 0) (Random.nextFloat() - 0.5f) * screenShake else 0f
            val shakeOffsetY = if (screenShake > 0) (Random.nextFloat() - 0.5f) * screenShake else 0f

            translate(left = shakeOffsetX, top = shakeOffsetY) {
                // Background Night Sky Grid & Ground Gradient
                drawBackgroundGrid(size.width, size.height)

                // Ground Safety Line
                drawGroundLine(catY, size.width)

                // Draw Falling Crates
                crates.forEach { crate ->
                    drawCrate(crate)
                }

                // Draw Bullets
                bullets.forEach { bullet ->
                    drawBullet(bullet)
                }

                // Draw Muzzle Flashes
                muzzleFlashes.forEach { flash ->
                    drawMuzzleFlash(flash)
                }

                // Draw Particles
                particles.forEach { p ->
                    drawParticle(p)
                }

                // Draw Dropped PowerUps
                droppedPowerUps.forEach { p ->
                    drawDroppedPowerUp(p)
                }

                // Draw Timoteo Black Cat & Gun
                drawTimoteo(
                    catX = catX,
                    catY = catY,
                    gunAngleRad = gunAngleRad,
                    recoilOffset = recoilOffset,
                    isWalking = isWalking,
                    walkAnimPhase = walkAnimPhase,
                    selectedSkin = selectedSkin,
                    nanoBananaBitmap = nanoBananaBitmap,
                    hdCatBitmap = hdCatBitmap
                )

                // Draw Floating Damage/Score Texts
                floatingTexts.forEach { ft ->
                    drawText(
                        textMeasurer = textMeasurer,
                        text = ft.text,
                        topLeft = Offset(ft.x - 40f, ft.y),
                        style = TextStyle(
                            color = ft.color.copy(alpha = ft.life.coerceIn(0f, 1f)),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            }
        }

        // Top Game HUD Header Overlay (Score, Hearts, Control Buttons, Skin Pill)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timoteo Avatar & Score & Skin Pill Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = DarkGameCard,
                    border = androidx.compose.foundation.BorderStroke(2.dp, YellowLaser),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(46.dp)
                        .clickable {
                            selectedSkin = when (selectedSkin) {
                                TimoteoSkin.NANO_BANANA -> TimoteoSkin.HD_CAT
                                TimoteoSkin.HD_CAT -> TimoteoSkin.VECTORIAL
                                TimoteoSkin.VECTORIAL -> TimoteoSkin.NANO_BANANA
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(id = selectedSkin.iconRes),
                        contentDescription = "Timoteo Gatito Skin",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DarkGameCard.copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, YellowLaser.copy(alpha = 0.5f)),
                        shadowElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "$score pts",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    // Clickable Skin Switch Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = YellowLaser.copy(alpha = 0.20f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, YellowLaser),
                        modifier = Modifier.clickable {
                            selectedSkin = when (selectedSkin) {
                                TimoteoSkin.NANO_BANANA -> TimoteoSkin.HD_CAT
                                TimoteoSkin.HD_CAT -> TimoteoSkin.VECTORIAL
                                TimoteoSkin.VECTORIAL -> TimoteoSkin.NANO_BANANA
                            }
                        }
                    ) {
                        Text(
                            text = selectedSkin.badge,
                            color = YellowLaser,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Lives Counter (Hearts)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGameCard.copy(alpha = 0.85f))
                    .border(1.dp, HeartRed.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                repeat(3) { index ->
                    val isAlive = index < lives
                    Text(
                        text = if (isAlive) "❤️" else "🖤",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }

            // Action Buttons (Pause & Audio)
            Row {
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkGameCard.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Mute",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { isPaused = !isPaused },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkGameCard.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White
                    )
                }
            }
        }

        // Active PowerUp Display Bar
        if (powerUps.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                powerUps.forEach { p ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = p.type.color.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, p.type.color),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "⚡ ${p.type.title} (${(p.durationMs / 1000) + 1}s)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Pause Menu Modal Overlay
        AnimatedVisibility(
            visible = isPaused,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkGameCard),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, YellowLaser),
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "JUEGO PAUSADO",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { isPaused = false },
                        colors = ButtonDefaults.buttonColors(containerColor = YellowLaser),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CONTINUAR", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Game Over Dialog Overlay
        AnimatedVisibility(
            visible = isGameOver,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkGameCard),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, HeartRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🐱 GAME OVER 🐱",
                        color = HeartRed,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Timoteo Full Body Character Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black,
                        border = androidx.compose.foundation.BorderStroke(2.dp, YellowLaser),
                        shadowElevation = 8.dp,
                        modifier = Modifier.size(110.dp)
                    ) {
                        Image(
                            painter = painterResource(id = selectedSkin.iconRes),
                            contentDescription = "Gatito Timoteo Skin Active",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Skin activa: ${selectedSkin.title}",
                        color = YellowLaser,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    // Skin Selector Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TimoteoSkin.values().forEach { skin ->
                            val isSelected = skin == selectedSkin
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) YellowLaser.copy(alpha = 0.25f) else Color(0xFF1F2538),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) YellowLaser else Color.Gray.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .clickable { selectedSkin = skin }
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = skin.title,
                                    color = if (isSelected) YellowLaser else Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Puntuación", color = Color.LightGray, fontSize = 12.sp)
                            Text("$score", color = YellowLaser, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Récord", color = Color.LightGray, fontSize = 12.sp)
                            Text("$highScore", color = NeonCyan, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Cajas Destruidas", color = Color.LightGray, fontSize = 12.sp)
                            Text("$cratesDestroyed", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        val accuracy = if (shotsFired > 0) ((hits.toFloat() / shotsFired) * 100).toInt() else 0
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Precisión", color = Color.LightGray, fontSize = 12.sp)
                            Text("$accuracy%", color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { restartGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = YellowLaser),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reiniciar",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "JUGAR DE NUEVO",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// Canvas Drawing Helper Functions

private fun DrawScope.drawBackgroundGrid(width: Float, height: Float) {
    // Subtle cyber grid lines in dark blue background
    val gridSpacing = 80f
    val gridColor = Color(0xFF1B2035)

    var x = 0f
    while (x < width) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
        x += gridSpacing
    }

    var y = 0f
    while (y < height) {
        drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
        y += gridSpacing
    }
}

private fun DrawScope.drawGroundLine(catY: Float, width: Float) {
    // Danger floor line
    drawLine(
        color = HeartRed.copy(alpha = 0.6f),
        start = Offset(0f, catY + 10f),
        end = Offset(width, catY + 10f),
        strokeWidth = 4f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
    )

    drawRect(
        color = Color(0xFF141724),
        topLeft = Offset(0f, catY + 12f),
        size = Size(width, 200f)
    )
}

// Draw Timoteo Black Cat using PNG skin sprite image
private fun DrawScope.drawTimoteo(
    catX: Float,
    catY: Float,
    gunAngleRad: Float,
    recoilOffset: Float,
    isWalking: Boolean = false,
    walkAnimPhase: Float = 0f,
    selectedSkin: TimoteoSkin = TimoteoSkin.NANO_BANANA,
    nanoBananaBitmap: ImageBitmap? = null,
    hdCatBitmap: ImageBitmap? = null
) {
    // Vertical bounce during walking
    val bobY = if (isWalking) sin(walkAnimPhase.toDouble()).toFloat() * 6f else 0f
    val drawCatY = catY + bobY

    // Determine active PNG skin bitmap
    val activeBitmap = when (selectedSkin) {
        TimoteoSkin.HD_CAT -> hdCatBitmap ?: nanoBananaBitmap
        else -> nanoBananaBitmap ?: hdCatBitmap
    }

    // Floor shadow underneath Timoteo
    drawOval(
        color = Color.Black.copy(alpha = 0.40f),
        topLeft = Offset(catX - 55f, catY + 35f),
        size = Size(110f, 20f)
    )

    val pivotX = catX
    val pivotY = drawCatY - 15f

    // Laser Aim Guide Line
    drawLine(
        color = YellowLaser.copy(alpha = 0.45f),
        start = Offset(pivotX, pivotY),
        end = Offset(pivotX + cos(gunAngleRad) * 1200f, pivotY + sin(gunAngleRad) * 1200f),
        strokeWidth = 3.5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f), 0f)
    )

    // Animated Kitten Paws stepping (left & right legs with pink toe beans)
    val legAnimPhase = if (isWalking) walkAnimPhase * 1.6f else ((System.currentTimeMillis() % 1600) / 1600f * 2 * Math.PI).toFloat()
    val pawStepL_X = sin(legAnimPhase.toDouble()).toFloat() * (if (isWalking) 18f else 4f)
    val pawStepL_Y = kotlin.math.abs(cos(legAnimPhase.toDouble()).toFloat()) * (if (isWalking) 10f else 3f)

    val pawStepR_X = -sin(legAnimPhase.toDouble()).toFloat() * (if (isWalking) 18f else 4f)
    val pawStepR_Y = kotlin.math.abs(sin(legAnimPhase.toDouble()).toFloat()) * (if (isWalking) 10f else 3f)

    val pawY = drawCatY + 38f

    // Draw Animated Left Paw (Black Paw + Pink Toe Beans)
    val leftPawCenter = Offset(catX - 26f + pawStepL_X, pawY - pawStepL_Y)
    drawCircle(color = Color.Black, center = leftPawCenter, radius = 13f)
    drawCircle(color = Color(0xFF2E3248), center = leftPawCenter, radius = 13f, style = Stroke(width = 2f))
    drawCircle(color = Color(0xFFFF80AB), center = Offset(leftPawCenter.x, leftPawCenter.y + 2f), radius = 5.5f)
    drawCircle(color = Color(0xFFFF80AB), center = Offset(leftPawCenter.x - 5f, leftPawCenter.y - 6f), radius = 2.5f)
    drawCircle(color = Color(0xFFFF80AB), center = Offset(leftPawCenter.x, leftPawCenter.y - 7.5f), radius = 2.5f)
    drawCircle(color = Color(0xFFFF80AB), center = Offset(leftPawCenter.x + 5f, leftPawCenter.y - 6f), radius = 2.5f)

    // Draw Animated Right Paw (Black Paw + Pink Toe Beans)
    val rightPawCenter = Offset(catX + 26f + pawStepR_X, pawY - pawStepR_Y)
    drawCircle(color = Color.Black, center = rightPawCenter, radius = 13f)
    drawCircle(color = Color(0xFF2E3248), center = rightPawCenter, radius = 13f, style = Stroke(width = 2f))
    drawCircle(color = Color(0xFFFF80AB), center = Offset(rightPawCenter.x, rightPawCenter.y + 2f), radius = 5.5f)
    drawCircle(color = Color(0xFFFF80AB), center = Offset(rightPawCenter.x - 5f, rightPawCenter.y - 6f), radius = 2.5f)
    drawCircle(color = Color(0xFFFF80AB), center = Offset(rightPawCenter.x, rightPawCenter.y - 7.5f), radius = 2.5f)
    drawCircle(color = Color(0xFFFF80AB), center = Offset(rightPawCenter.x + 5f, rightPawCenter.y - 6f), radius = 2.5f)

    if (activeBitmap != null) {
        // Draw character sprite PNG bitmap centered at (catX, drawCatY)
        val spriteWidth = 150
        val spriteHeight = 150
        val spriteTopLeft = IntOffset((catX - spriteWidth / 2f).toInt(), (drawCatY - spriteHeight / 2f - 20f).toInt())
        val walkWaddle = if (isWalking) sin(walkAnimPhase.toDouble()).toFloat() * 7f else sin((System.currentTimeMillis() % 2000) / 2000f * 2 * Math.PI).toFloat() * 2.5f
        val tiltAngle = (Math.toDegrees(gunAngleRad.toDouble()).toFloat() + 90f).coerceIn(-25f, 25f) + walkWaddle

        val clipBounds = Path().apply {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    left = catX - spriteWidth / 2f + 12f,
                    top = drawCatY - spriteHeight / 2f - 12f,
                    right = catX + spriteWidth / 2f - 12f,
                    bottom = drawCatY + spriteHeight / 2f - 25f
                )
            )
        }

        rotate(degrees = tiltAngle, pivot = Offset(catX, drawCatY)) {
            clipPath(clipBounds) {
                drawImage(
                    image = activeBitmap,
                    dstOffset = spriteTopLeft,
                    dstSize = IntSize(spriteWidth, spriteHeight)
                )
            }
        }
    } else {
        // Simple fallback circle if bitmap is loading
        drawCircle(
            color = Color.Black,
            center = Offset(catX, drawCatY),
            radius = 45f
        )
    }

    // Muzzle Recoil Glow Effect on shot
    if (recoilOffset > 1f) {
        val muzzleX = pivotX + cos(gunAngleRad) * 80f
        val muzzleY = pivotY + sin(gunAngleRad) * 80f
        drawCircle(
            color = YellowLaser.copy(alpha = 0.85f),
            center = Offset(muzzleX, muzzleY),
            radius = 18f
        )
        drawCircle(
            color = Color.White,
            center = Offset(muzzleX, muzzleY),
            radius = 9f
        )
    }
}

// Draw Falling Wood / Colored Crate
private fun DrawScope.drawCrate(crate: Crate) {
    val cx = crate.x + crate.width / 2
    val cy = crate.y + crate.height / 2

    rotate(degrees = crate.rotationAngle, pivot = Offset(cx, cy)) {
        // Main Crate Box Body
        drawRoundRect(
            color = crate.type.color,
            topLeft = Offset(crate.x, crate.y),
            size = Size(crate.width, crate.height),
            cornerRadius = CornerRadius(12f)
        )

        // Crate Outer Bezel Border
        drawRoundRect(
            color = crate.type.borderColors.first,
            topLeft = Offset(crate.x, crate.y),
            size = Size(crate.width, crate.height),
            cornerRadius = CornerRadius(12f),
            style = Stroke(width = 6f)
        )

        // Wood Plank Slats or Metallic Cross Braces
        drawLine(
            color = crate.type.borderColors.first,
            start = Offset(crate.x + 8f, crate.y + 8f),
            end = Offset(crate.x + crate.width - 8f, crate.y + crate.height - 8f),
            strokeWidth = 5f
        )
        drawLine(
            color = crate.type.borderColors.first,
            start = Offset(crate.x + crate.width - 8f, crate.y + 8f),
            end = Offset(crate.x + 8f, crate.y + crate.height - 8f),
            strokeWidth = 5f
        )

        // Corner Metallic Brackets
        val bracketSize = 18f
        drawRect(
            color = crate.type.borderColors.second,
            topLeft = Offset(crate.x, crate.y),
            size = Size(bracketSize, bracketSize)
        )
        drawRect(
            color = crate.type.borderColors.second,
            topLeft = Offset(crate.x + crate.width - bracketSize, crate.y),
            size = Size(bracketSize, bracketSize)
        )
        drawRect(
            color = crate.type.borderColors.second,
            topLeft = Offset(crate.x, crate.y + crate.height - bracketSize),
            size = Size(bracketSize, bracketSize)
        )
        drawRect(
            color = crate.type.borderColors.second,
            topLeft = Offset(crate.x + crate.width - bracketSize, crate.y + crate.height - bracketSize),
            size = Size(bracketSize, bracketSize)
        )

        // Label Tag or HP Bar for steel crate
        if (crate.maxHp > 1) {
            val hpWidth = (crate.width - 20f) * (crate.hp.toFloat() / crate.maxHp)
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(crate.x + 10f, crate.y + crate.height - 18f),
                size = Size(crate.width - 20f, 8f)
            )
            drawRect(
                color = NeonGreen,
                topLeft = Offset(crate.x + 10f, crate.y + crate.height - 18f),
                size = Size(hpWidth, 8f)
            )
        }
    }
}

// Draw Glowing Yellow Bullet
private fun DrawScope.drawBullet(bullet: Bullet) {
    // Outer Glow
    drawCircle(
        color = LaserOrange.copy(alpha = 0.5f),
        center = Offset(bullet.x, bullet.y),
        radius = bullet.radius * 2.2f
    )

    // Inner Core
    drawCircle(
        color = bullet.color,
        center = Offset(bullet.x, bullet.y),
        radius = bullet.radius
    )
}

// Draw Muzzle Flash Effect
private fun DrawScope.drawMuzzleFlash(flash: MuzzleFlash) {
    val radius = 35f * flash.life
    drawCircle(
        color = YellowLaser.copy(alpha = flash.life.coerceIn(0f, 1f)),
        center = Offset(flash.x, flash.y),
        radius = radius
    )
    drawCircle(
        color = Color.White.copy(alpha = flash.life.coerceIn(0f, 1f)),
        center = Offset(flash.x, flash.y),
        radius = radius * 0.5f
    )
}

// Draw Particle Debris
private fun DrawScope.drawParticle(p: Particle) {
    val alpha = p.life.coerceIn(0f, 1f)
    val colorWithAlpha = p.color.copy(alpha = alpha)

    when (p.type) {
        ParticleType.WOOD_CHIP -> {
            drawRect(
                color = colorWithAlpha,
                topLeft = Offset(p.x - p.radius, p.y - p.radius),
                size = Size(p.radius * 2, p.radius * 1.5f)
            )
        }
        ParticleType.SPARK -> {
            drawCircle(
                color = colorWithAlpha,
                center = Offset(p.x, p.y),
                radius = p.radius
            )
        }
        ParticleType.EXPLOSION_FIRE -> {
            drawCircle(
                color = LaserOrange.copy(alpha = alpha),
                center = Offset(p.x, p.y),
                radius = p.radius * 1.8f
            )
            drawCircle(
                color = YellowLaser.copy(alpha = alpha),
                center = Offset(p.x, p.y),
                radius = p.radius
            )
        }
        else -> {
            drawCircle(color = colorWithAlpha, center = Offset(p.x, p.y), radius = p.radius)
        }
    }
}

// Draw Dropped PowerUp Icon
private fun DrawScope.drawDroppedPowerUp(p: DroppedPowerUp) {
    drawCircle(
        color = p.type.color.copy(alpha = 0.4f),
        center = Offset(p.x, p.y),
        radius = p.radius * 1.5f
    )
    drawCircle(
        color = p.type.color,
        center = Offset(p.x, p.y),
        radius = p.radius,
        style = Stroke(width = 4f)
    )
    drawCircle(
        color = Color.White,
        center = Offset(p.x, p.y),
        radius = p.radius * 0.6f
    )
}
