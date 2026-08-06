package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.LaserOrange
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.WoodBrown
import com.example.ui.theme.WoodDark
import com.example.ui.theme.YellowLaser

enum class CrateType(
    val baseHp: Int,
    val points: Int,
    val color: Color,
    val borderColors: Pair<Color, Color>,
    val label: String
) {
    WOOD(
        baseHp = 1,
        points = 1,
        color = WoodBrown,
        borderColors = Pair(WoodDark, Color(0xFFB07844)),
        label = "MADERA"
    ),
    STEEL(
        baseHp = 2,
        points = 3,
        color = Color(0xFF607D8B),
        borderColors = Pair(Color(0xFF37474F), Color(0xFF90A4AE)),
        label = "ACERO"
    ),
    GOLD(
        baseHp = 1,
        points = 0,
        color = Color(0xFFFFD700),
        borderColors = Pair(Color(0xFFB8860B), Color(0xFFFFF59D)),
        label = "ORO"
    ),
    EXP(
        baseHp = 1,
        points = 10,
        color = NeonGreen,
        borderColors = Pair(Color(0xFF2E7D32), Color(0xFFA5D6A7)),
        label = "EXP"
    ),
    BOMB(
        baseHp = 1,
        points = 0,
        color = Color(0xFFD32F2F),
        borderColors = Pair(Color(0xFF8B0000), Color(0xFFFF8A80)),
        label = "PELIGRO"
    )
}

enum class ParticleType {
    WOOD_CHIP,
    SPARK,
    EXPLOSION_FIRE,
    SMOKE,
    STAR
}

data class Bullet(
    val id: Long,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float = 10f,
    val color: Color = YellowLaser,
    val isPowerShot: Boolean = false
)

data class Crate(
    val id: Long,
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    var speedY: Float,
    val type: CrateType,
    var hp: Int,
    val maxHp: Int,
    var rotationAngle: Float = 0f,
    val rotationSpeed: Float = 0f
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    val color: Color,
    var life: Float,
    val maxLife: Float,
    val type: ParticleType = ParticleType.WOOD_CHIP,
    val rotation: Float = 0f
)

data class FloatingText(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var life: Float = 1.0f,
    val maxLife: Float = 1.0f,
    val vy: Float = -2.5f
)

data class MuzzleFlash(
    val x: Float,
    val y: Float,
    val angle: Float,
    var life: Float = 1.0f
)

data class TimoteoState(
    var x: Float = 0f,
    var y: Float = 0f,
    var gunAngleRad: Float = -Math.PI.toFloat() / 2, // Upward
    var eyeBlinkProgress: Float = 0f,
    var recoiling: Float = 0f
)

data class ActivePowerUp(
    val type: PowerUpType,
    var durationMs: Long
)

enum class PowerUpType(val title: String, val duration: Long, val color: Color) {
    TRIPLE_SHOT("RÁFAGA TRIPLE", 8000L, YellowLaser),
    SLOW_MO("TIEMPO LENTO", 7000L, NeonCyan),
    BOMB("BOMBA ATÓMICA", 0L, NeonGreen)
}

data class DroppedPowerUp(
    val id: Long,
    val type: PowerUpType,
    var x: Float,
    var y: Float,
    var vy: Float = 2f,
    val radius: Float = 22f
)
