package com.example.ui.art

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import com.example.model.ArtTheme
import com.example.model.MotifType
import com.example.model.ParticleEffectType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var alpha: Float,
    var rotation: Float,
    var vRot: Float,
    val seed: Float = Random.nextFloat()
)

private data class TouchRipple(
    val x: Float,
    val y: Float,
    var radius: Float = 0f,
    var alpha: Float = 0.8f,
    val color: Color
)

@Composable
fun ArtCanvas(
    theme: ArtTheme,
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    interactive: Boolean = true,
    showMantraOverlay: Boolean = false,
    contentAlpha: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArtBreathing")
    val breathProgress by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    val timePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "timePhase"
    )

    val particles = remember(theme.particleEffect) {
        val count = when (theme.particleEffect) {
            ParticleEffectType.NONE -> 0
            ParticleEffectType.RAIN -> 45
            ParticleEffectType.FALLING_PETALS -> 28
            ParticleEffectType.FLOATING_SPARKS -> 35
            ParticleEffectType.STARRY_GLOW -> 40
            ParticleEffectType.FLOATING_LEAVES -> 22
            ParticleEffectType.NEON_GRID_STREAM -> 30
            ParticleEffectType.FIRE_EMBERS -> 32
        }
        mutableStateListOf<Particle>().apply {
            repeat(count) {
                add(
                    Particle(
                        x = Random.nextFloat(),
                        y = Random.nextFloat(),
                        vx = (Random.nextFloat() - 0.5f) * 0.002f,
                        vy = Random.nextFloat() * 0.003f + 0.001f,
                        size = Random.nextFloat() * 8f + 3f,
                        alpha = Random.nextFloat() * 0.7f + 0.3f,
                        rotation = Random.nextFloat() * 360f,
                        vRot = (Random.nextFloat() - 0.5f) * 3f
                    )
                )
            }
        }
    }

    val touchRipples = remember { mutableStateListOf<TouchRipple>() }

    // Frame-based particle physics
    LaunchedEffect(theme.particleEffect) {
        var lastTime = 0L
        while (true) {
            withFrameNanos { frameTime ->
                if (lastTime != 0L) {
                    val dt = ((frameTime - lastTime) / 1_000_000_000f).coerceIn(0f, 0.05f)

                    // Update particles
                    for (p in particles) {
                        when (theme.particleEffect) {
                            ParticleEffectType.RAIN -> {
                                p.y += (0.6f + p.seed * 0.4f) * dt
                                p.x += 0.08f * dt
                                if (p.y > 1.1f) {
                                    p.y = -0.1f
                                    p.x = Random.nextFloat()
                                }
                            }
                            ParticleEffectType.FALLING_PETALS, ParticleEffectType.FLOATING_LEAVES -> {
                                p.y += (0.08f + p.seed * 0.06f) * dt
                                p.x += sin(timePhase + p.seed * 10f) * 0.04f * dt
                                p.rotation += p.vRot
                                if (p.y > 1.1f) {
                                    p.y = -0.1f
                                    p.x = Random.nextFloat()
                                }
                            }
                            ParticleEffectType.FLOATING_SPARKS, ParticleEffectType.FIRE_EMBERS -> {
                                p.y -= (0.09f + p.seed * 0.08f) * dt
                                p.x += sin(timePhase * 2 + p.seed * 5f) * 0.03f * dt
                                if (p.y < -0.1f) {
                                    p.y = 1.1f
                                    p.x = Random.nextFloat()
                                }
                            }
                            ParticleEffectType.NEON_GRID_STREAM -> {
                                p.y += (0.15f + p.seed * 0.2f) * dt
                                if (p.y > 1.1f) {
                                    p.y = -0.1f
                                    p.x = Random.nextFloat()
                                }
                            }
                            ParticleEffectType.STARRY_GLOW -> {
                                p.alpha = (0.3f + 0.6f * sin(timePhase + p.seed * 6.28f)).coerceIn(0.1f, 0.95f)
                            }
                            ParticleEffectType.NONE -> {}
                        }
                    }

                    // Update touch ripples
                    val iterator = touchRipples.iterator()
                    while (iterator.hasNext()) {
                        val ripple = iterator.next()
                        ripple.radius += 350f * dt
                        ripple.alpha -= 0.9f * dt
                        if (ripple.alpha <= 0f) {
                            iterator.remove()
                        }
                    }
                }
                lastTime = frameTime
            }
        }
    }

    val gradientBrush = remember(theme.gradientColors) {
        val colors = theme.gradientColors.map { Color(it) }
        if (colors.size >= 2) {
            Brush.verticalGradient(colors)
        } else {
            Brush.verticalGradient(listOf(Color(0xFF1E152A), Color(0xFF422835)))
        }
    }

    val moodColor = remember(theme.ambientMoodColor) { Color(theme.ambientMoodColor) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (interactive) {
                    Modifier.pointerInput(interactive) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.type == PointerEventType.Press) {
                                    val change = event.changes.firstOrNull()
                                    if (change != null) {
                                        touchRipples.add(
                                            TouchRipple(
                                                x = change.position.x,
                                                y = change.position.y,
                                                color = moodColor
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else Modifier
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = contentAlpha)
        ) {
            val w = size.width
            val h = size.height

            // 1. Background Gradient Base
            drawRect(
                brush = gradientBrush,
                topLeft = Offset.Zero,
                size = size
            )

            // 2. Ambient Mood Aura & Vignette
            val auraRadius = (w.coerceAtLeast(h) * 0.7f) * breathProgress
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        moodColor.copy(alpha = 0.22f + progress * 0.1f),
                        moodColor.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.45f),
                    radius = auraRadius
                ),
                center = Offset(w * 0.5f, h * 0.45f),
                radius = auraRadius
            )

            // 3. Draw Thematic Vector Motif
            drawMotif(
                motifType = theme.motifType,
                moodColor = moodColor,
                breathProgress = breathProgress,
                timePhase = timePhase,
                progress = progress,
                w = w,
                h = h
            )

            // 4. Draw Animated Particles
            drawParticles(
                particles = particles,
                effect = theme.particleEffect,
                moodColor = moodColor,
                w = w,
                h = h
            )

            // 5. Draw Interactive Touch Ripples
            for (ripple in touchRipples) {
                drawCircle(
                    color = ripple.color.copy(alpha = ripple.alpha.coerceIn(0f, 1f)),
                    radius = ripple.radius,
                    center = Offset(ripple.x, ripple.y),
                    style = Stroke(width = 3.5f)
                )
            }
        }
    }
}

private fun DrawScope.drawMotif(
    motifType: MotifType,
    moodColor: Color,
    breathProgress: Float,
    timePhase: Float,
    progress: Float,
    w: Float,
    h: Float
) {
    val cx = w * 0.5f
    val cy = h * 0.42f

    when (motifType) {
        MotifType.COZY_LAMP_CUP -> {
            // Desk Lamp Light Cone
            val lampCone = Path().apply {
                moveTo(cx + w * 0.25f, cy - h * 0.18f)
                lineTo(cx - w * 0.4f, cy + h * 0.25f)
                lineTo(cx + w * 0.45f, cy + h * 0.25f)
                close()
            }
            drawPath(
                path = lampCone,
                brush = Brush.radialGradient(
                    colors = listOf(
                        moodColor.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(cx + w * 0.25f, cy - h * 0.18f),
                    radius = w * 0.8f
                )
            )

            // Window Pane Behind with rain reflections
            val winW = w * 0.5f
            val winH = h * 0.28f
            val winL = cx - winW * 0.85f
            val winT = cy - winH * 0.9f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.08f),
                topLeft = Offset(winL, winT),
                size = Size(winW, winH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            // Window grid dividers
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(winL + winW * 0.5f, winT),
                end = Offset(winL + winW * 0.5f, winT + winH),
                strokeWidth = 3f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(winL, winT + winH * 0.45f),
                end = Offset(winL + winW, winT + winH * 0.45f),
                strokeWidth = 3f
            )

            // Desk Surface
            val deskY = cy + h * 0.14f
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(w * 0.08f, deskY),
                end = Offset(w * 0.92f, deskY),
                strokeWidth = 4f
            )

            // Steaming Coffee Cup
            val cupX = cx - w * 0.12f
            val cupY = deskY - 32f
            val cupW = 44f
            val cupH = 34f
            drawRoundRect(
                color = moodColor.copy(alpha = 0.9f),
                topLeft = Offset(cupX - cupW / 2, cupY),
                size = Size(cupW, cupH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
            // Cup Handle
            drawArc(
                color = moodColor.copy(alpha = 0.9f),
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cupX + cupW / 2 - 6f, cupY + 6f),
                size = Size(18f, 22f),
                style = Stroke(width = 4f)
            )
            // Rising Steam
            val steamPath = Path().apply {
                val steamOsc = sin(timePhase * 2) * 8f
                moveTo(cupX, cupY - 6f)
                cubicTo(
                    cupX + steamOsc, cupY - 20f,
                    cupX - steamOsc, cupY - 40f,
                    cupX + steamOsc * 0.5f, cupY - 55f
                )
            }
            drawPath(
                path = steamPath,
                color = Color.White.copy(alpha = 0.4f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Open Notebook on desk
            val bookW = 70f
            val bookH = 26f
            val bookX = cx + w * 0.08f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.75f),
                topLeft = Offset(bookX - bookW / 2, deskY - 14f),
                size = Size(bookW, bookH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            drawLine(
                color = moodColor.copy(alpha = 0.8f),
                start = Offset(bookX, deskY - 14f),
                end = Offset(bookX, deskY + 12f),
                strokeWidth = 2.5f
            )
        }

        MotifType.ZEN_BONSAI -> {
            // Sun/Moon disc in mist
            val sunRadius = w * 0.22f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        moodColor.copy(alpha = 0.4f),
                        moodColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy - h * 0.05f),
                    radius = sunRadius
                ),
                radius = sunRadius,
                center = Offset(cx, cy - h * 0.05f)
            )

            // Stacked Zen Stones (Cairn)
            val baseStoneY = cy + h * 0.16f
            val stoneSizes = listOf(
                Pair(w * 0.35f, 26f),
                Pair(w * 0.26f, 22f),
                Pair(w * 0.18f, 18f),
                Pair(w * 0.11f, 14f)
            )
            var curY = baseStoneY
            for (stone in stoneSizes) {
                drawRoundRect(
                    color = Color(0xFF232B32).copy(alpha = 0.95f),
                    topLeft = Offset(cx - stone.first / 2, curY - stone.second),
                    size = Size(stone.first, stone.second),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(stone.second / 2, stone.second / 2)
                )
                // Highlight rim
                drawRoundRect(
                    color = moodColor.copy(alpha = 0.35f),
                    topLeft = Offset(cx - stone.first / 2, curY - stone.second),
                    size = Size(stone.first, stone.second),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(stone.second / 2, stone.second / 2),
                    style = Stroke(width = 2f)
                )
                curY -= (stone.second - 4f)
            }

            // Bonsai Branch sweeping over
            val branch = Path().apply {
                moveTo(cx - w * 0.32f, baseStoneY + 10f)
                cubicTo(
                    cx - w * 0.25f, cy,
                    cx - w * 0.1f, cy - h * 0.08f,
                    cx + w * 0.22f, cy - h * 0.06f
                )
            }
            drawPath(
                path = branch,
                color = Color(0xFF1E262A),
                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Foliage clusters (Clouds of green/mood)
            val foliageCenters = listOf(
                Offset(cx + w * 0.2f, cy - h * 0.08f),
                Offset(cx + w * 0.08f, cy - h * 0.12f),
                Offset(cx - w * 0.05f, cy - h * 0.06f)
            )
            for (fc in foliageCenters) {
                drawCircle(
                    color = moodColor.copy(alpha = 0.8f),
                    radius = 28f * breathProgress,
                    center = fc
                )
            }
        }

        MotifType.ZEN_ENSO -> {
            // Glowing Japanese Enso Circle with breathing brush weight
            val ensoRadius = w * 0.28f * breathProgress
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        moodColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = ensoRadius * 1.5f
                ),
                radius = ensoRadius * 1.5f,
                center = Offset(cx, cy)
            )

            rotate(degrees = (timePhase * 15f), pivot = Offset(cx, cy)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            moodColor.copy(alpha = 0.95f),
                            moodColor.copy(alpha = 0.4f),
                            moodColor.copy(alpha = 0.05f),
                            moodColor.copy(alpha = 0.85f)
                        ),
                        center = Offset(cx, cy)
                    ),
                    startAngle = 40f,
                    sweepAngle = 290f,
                    useCenter = false,
                    topLeft = Offset(cx - ensoRadius, cy - ensoRadius),
                    size = Size(ensoRadius * 2, ensoRadius * 2),
                    style = Stroke(width = 16f, cap = StrokeCap.Round)
                )
            }

            // Red Artisan Seal (Hanko) in corner
            val sealSize = 28f
            val sealX = cx + ensoRadius * 0.7f
            val sealY = cy + ensoRadius * 0.7f
            drawRoundRect(
                color = Color(0xFFE53935).copy(alpha = 0.9f),
                topLeft = Offset(sealX, sealY),
                size = Size(sealSize, sealSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(sealX + 7f, sealY + sealSize / 2),
                end = Offset(sealX + sealSize - 7f, sealY + sealSize / 2),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(sealX + sealSize / 2, sealY + 7f),
                end = Offset(sealX + sealSize / 2, sealY + sealSize - 7f),
                strokeWidth = 2f
            )
        }

        MotifType.CYBER_GRID -> {
            // Neon Sun on Horizon
            val sunR = w * 0.22f
            val horizonY = cy + h * 0.08f
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        moodColor.copy(alpha = 0.9f),
                        Color(0xFFFF007F).copy(alpha = 0.7f)
                    ),
                    startY = horizonY - sunR,
                    endY = horizonY
                ),
                radius = sunR,
                center = Offset(cx, horizonY)
            )

            // Neon Skyline Silhouette
            val buildings = listOf(
                Pair(cx - w * 0.38f, 90f),
                Pair(cx - w * 0.22f, 130f),
                Pair(cx - w * 0.08f, 170f),
                Pair(cx + w * 0.1f, 140f),
                Pair(cx + w * 0.26f, 100f)
            )
            for (b in buildings) {
                val bw = 48f
                drawRect(
                    color = Color(0xFF0D0221),
                    topLeft = Offset(b.first, horizonY - b.second),
                    size = Size(bw, b.second)
                )
                // Neon antenna or roof light
                drawLine(
                    color = moodColor,
                    start = Offset(b.first + bw / 2, horizonY - b.second),
                    end = Offset(b.first + bw / 2, horizonY - b.second - 20f),
                    strokeWidth = 2f
                )
            }

            // Synthwave Perspective Grid
            for (i in 0..8) {
                val t = i / 8f
                val y = horizonY + (h - horizonY) * (t * t)
                drawLine(
                    color = moodColor.copy(alpha = (0.2f + t * 0.6f)),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.5f + t * 2f
                )
            }
            // Vanishing perspective rays
            for (i in -4..4) {
                val bottomX = cx + (i * w * 0.16f)
                drawLine(
                    color = moodColor.copy(alpha = 0.45f),
                    start = Offset(cx, horizonY),
                    end = Offset(bottomX, h),
                    strokeWidth = 2f
                )
            }
        }

        MotifType.COSMIC_PLANET -> {
            // Ringed Cosmic Planet
            val planetR = w * 0.2f * breathProgress
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        moodColor.copy(alpha = 0.95f),
                        Color(0xFF2A0845).copy(alpha = 0.95f),
                        Color(0xFF0F0C29)
                    ),
                    center = Offset(cx - planetR * 0.35f, cy - planetR * 0.35f),
                    radius = planetR * 1.3f
                ),
                radius = planetR,
                center = Offset(cx, cy)
            )

            // Planet Ring
            rotate(degrees = -25f, pivot = Offset(cx, cy)) {
                drawOval(
                    color = moodColor.copy(alpha = 0.6f),
                    topLeft = Offset(cx - planetR * 1.7f, cy - planetR * 0.35f),
                    size = Size(planetR * 3.4f, planetR * 0.7f),
                    style = Stroke(width = 6f)
                )
                drawOval(
                    color = Color.White.copy(alpha = 0.4f),
                    topLeft = Offset(cx - planetR * 1.9f, cy - planetR * 0.4f),
                    size = Size(planetR * 3.8f, planetR * 0.8f),
                    style = Stroke(width = 2.5f)
                )
            }

            // Distant Orbiting Moon
            val moonDist = planetR * 2.2f
            val moonAngle = timePhase * 0.6f
            val moonX = cx + cos(moonAngle) * moonDist
            val moonY = cy + sin(moonAngle) * (moonDist * 0.45f)
            drawCircle(
                color = Color(0xFFE2E8F0),
                radius = 8f,
                center = Offset(moonX, moonY)
            )
            drawCircle(
                color = moodColor.copy(alpha = 0.35f),
                radius = 16f,
                center = Offset(moonX, moonY)
            )
        }

        MotifType.AURORA_PEAKS -> {
            // Waving Aurora Ribbons
            for (layer in 0..2) {
                val auroraPath = Path().apply {
                    val baseRibbonY = cy - h * 0.15f + layer * 35f
                    moveTo(0f, baseRibbonY)
                    for (xStep in 0..10) {
                        val rx = (xStep / 10f) * w
                        val wave = sin((rx / w * 5f) + timePhase + layer) * 40f * breathProgress
                        lineTo(rx, baseRibbonY + wave)
                    }
                    lineTo(w, 0f)
                    lineTo(0f, 0f)
                    close()
                }
                drawPath(
                    path = auroraPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            moodColor.copy(alpha = 0.4f - layer * 0.1f),
                            Color.Transparent
                        )
                    )
                )
            }

            // Snow-capped Mountain Ridges
            val mountainY = cy + h * 0.18f
            val mtn1 = Path().apply {
                moveTo(0f, mountainY + 40f)
                lineTo(cx - w * 0.2f, cy - h * 0.05f)
                lineTo(cx + w * 0.15f, mountainY)
                lineTo(cx + w * 0.45f, cy - h * 0.02f)
                lineTo(w, mountainY + 50f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = mtn1,
                color = Color(0xFF0F1A1C)
            )
            // Snow tops
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(cx - w * 0.2f, cy - h * 0.05f),
                end = Offset(cx - w * 0.14f, cy + 15f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(cx + w * 0.45f, cy - h * 0.02f),
                end = Offset(cx + w * 0.38f, cy + 30f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        MotifType.SUN_DUNES -> {
            // Radiant Golden Sun
            val sunR = w * 0.22f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        moodColor.copy(alpha = 0.95f),
                        Color(0xFFFFB703).copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy - h * 0.04f),
                    radius = sunR * 1.5f
                ),
                radius = sunR * 1.5f,
                center = Offset(cx, cy - h * 0.04f)
            )

            // Flowing Sand Dunes
            val dune1 = Path().apply {
                val d1Y = cy + h * 0.08f
                moveTo(0f, d1Y)
                cubicTo(
                    w * 0.3f, d1Y - 40f,
                    w * 0.7f, d1Y + 30f,
                    w, d1Y - 10f
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = dune1,
                color = Color(0xFFB55D11)
            )

            val dune2 = Path().apply {
                val d2Y = cy + h * 0.18f
                moveTo(0f, d2Y + 20f)
                cubicTo(
                    w * 0.4f, d2Y + 40f,
                    w * 0.75f, d2Y - 30f,
                    w, d2Y + 10f
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = dune2,
                color = Color(0xFF6B3000)
            )
        }

        MotifType.SAKURA_BRANCH -> {
            // Twilight Moon
            val moonR = w * 0.18f
            drawCircle(
                color = Color.White.copy(alpha = 0.18f),
                radius = moonR,
                center = Offset(cx - w * 0.15f, cy - h * 0.08f)
            )

            // Waving Sakura Branch
            val branch = Path().apply {
                moveTo(w * 1.1f, cy - h * 0.16f)
                cubicTo(
                    cx + w * 0.2f, cy - h * 0.1f,
                    cx + w * 0.05f, cy,
                    cx - w * 0.3f, cy + 20f
                )
            }
            drawPath(
                path = branch,
                color = Color(0xFF2C1428),
                style = Stroke(width = 7f, cap = StrokeCap.Round)
            )

            // Blossom Clusters
            val blossomPoints = listOf(
                Offset(cx - w * 0.22f, cy + 15f),
                Offset(cx - w * 0.08f, cy + 4f),
                Offset(cx + w * 0.12f, cy - h * 0.06f),
                Offset(cx + w * 0.28f, cy - h * 0.12f)
            )
            for (bp in blossomPoints) {
                // 5 petals per flower
                for (pIdx in 0..4) {
                    val angle = (pIdx * 72f) * (Math.PI / 180f)
                    val px = bp.x + cos(angle).toFloat() * 12f
                    val py = bp.y + sin(angle).toFloat() * 12f
                    drawCircle(
                        color = moodColor.copy(alpha = 0.9f),
                        radius = 8f * breathProgress,
                        center = Offset(px, py)
                    )
                }
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = bp
                )
            }
        }

        MotifType.PIXEL_FIREPLACE -> {
            // Hearth stone arch
            val hearthW = w * 0.45f
            val hearthH = h * 0.22f
            val hearthT = cy - h * 0.06f
            drawRoundRect(
                color = Color(0xFF1E100A),
                topLeft = Offset(cx - hearthW / 2, hearthT),
                size = Size(hearthW, hearthH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
            )
            drawRoundRect(
                color = moodColor.copy(alpha = 0.4f),
                topLeft = Offset(cx - hearthW / 2, hearthT),
                size = Size(hearthW, hearthH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f),
                style = Stroke(width = 3f)
            )

            // Warm Fire Flames
            val flamePath = Path().apply {
                val flameBaseY = hearthT + hearthH - 12f
                val flameH = 65f * breathProgress
                val flicker = sin(timePhase * 4) * 8f
                moveTo(cx - 30f, flameBaseY)
                cubicTo(
                    cx - 20f + flicker, flameBaseY - flameH * 0.5f,
                    cx - 10f, flameBaseY - flameH * 0.8f,
                    cx + flicker, flameBaseY - flameH
                )
                cubicTo(
                    cx + 10f, flameBaseY - flameH * 0.8f,
                    cx + 20f - flicker, flameBaseY - flameH * 0.5f,
                    cx + 30f, flameBaseY
                )
                close()
            }
            drawPath(
                path = flamePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFEB3B),
                        moodColor,
                        Color(0xFFD32F2F)
                    )
                )
            )
        }

        MotifType.LOTUS_FLOWER -> {
            // Sacred Lotus
            val lotusBaseY = cy + 40f
            // Water Ripple Rings
            for (r in 1..3) {
                drawOval(
                    color = moodColor.copy(alpha = (0.35f / r)),
                    topLeft = Offset(cx - (w * 0.25f * r), lotusBaseY - 10f * r),
                    size = Size(w * 0.5f * r, 20f * r),
                    style = Stroke(width = 2f)
                )
            }

            // Lotus Petals
            for (p in -2..2) {
                val petalOffset = p * 18f
                val petalHeight = (60f - kotlin.math.abs(p) * 12f) * breathProgress
                val petalPath = Path().apply {
                    moveTo(cx, lotusBaseY)
                    cubicTo(
                        cx + petalOffset * 1.5f, lotusBaseY - petalHeight * 0.5f,
                        cx + petalOffset, lotusBaseY - petalHeight,
                        cx + petalOffset * 0.3f, lotusBaseY - petalHeight
                    )
                    cubicTo(
                        cx, lotusBaseY - petalHeight * 0.8f,
                        cx, lotusBaseY - petalHeight * 0.3f,
                        cx, lotusBaseY
                    )
                    close()
                }
                drawPath(
                    path = petalPath,
                    color = moodColor.copy(alpha = 0.85f)
                )
                drawPath(
                    path = petalPath,
                    color = Color.White.copy(alpha = 0.4f),
                    style = Stroke(width = 1.5f)
                )
            }
        }

        MotifType.HOURGLASS -> {
            // Minimalist Glowing Hourglass
            val hgW = w * 0.25f
            val hgH = h * 0.22f
            val hgT = cy - hgH / 2

            val hgPath = Path().apply {
                moveTo(cx - hgW / 2, hgT)
                lineTo(cx + hgW / 2, hgT)
                lineTo(cx, cy)
                lineTo(cx + hgW / 2, hgT + hgH)
                lineTo(cx - hgW / 2, hgT + hgH)
                lineTo(cx, cy)
                close()
            }
            drawPath(
                path = hgPath,
                color = moodColor.copy(alpha = 0.2f)
            )
            drawPath(
                path = hgPath,
                color = moodColor,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Flowing Sand stream
            drawLine(
                color = moodColor,
                start = Offset(cx, cy),
                end = Offset(cx, hgT + hgH - 12f),
                strokeWidth = 2.5f
            )

            // Bottom sand pile
            val sandW = hgW * 0.7f * (0.3f + progress * 0.7f)
            val sandPile = Path().apply {
                moveTo(cx - sandW / 2, hgT + hgH - 4f)
                lineTo(cx, hgT + hgH - 24f)
                lineTo(cx + sandW / 2, hgT + hgH - 4f)
                close()
            }
            drawPath(
                path = sandPile,
                color = moodColor.copy(alpha = 0.9f)
            )
        }
    }
}

private fun DrawScope.drawParticles(
    particles: List<Particle>,
    effect: ParticleEffectType,
    moodColor: Color,
    w: Float,
    h: Float
) {
    if (effect == ParticleEffectType.NONE) return

    for (p in particles) {
        val px = p.x * w
        val py = p.y * h

        when (effect) {
            ParticleEffectType.RAIN -> {
                drawLine(
                    color = Color.White.copy(alpha = p.alpha * 0.5f),
                    start = Offset(px, py),
                    end = Offset(px + 4f, py + p.size * 2.5f),
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
            }
            ParticleEffectType.FALLING_PETALS -> {
                rotate(degrees = p.rotation, pivot = Offset(px, py)) {
                    drawOval(
                        color = moodColor.copy(alpha = p.alpha),
                        topLeft = Offset(px - p.size, py - p.size * 0.6f),
                        size = Size(p.size * 2, p.size * 1.2f)
                    )
                }
            }
            ParticleEffectType.FLOATING_LEAVES -> {
                rotate(degrees = p.rotation, pivot = Offset(px, py)) {
                    drawOval(
                        color = Color(0xFF81C784).copy(alpha = p.alpha),
                        topLeft = Offset(px - p.size, py - p.size * 0.5f),
                        size = Size(p.size * 2, p.size)
                    )
                }
            }
            ParticleEffectType.FLOATING_SPARKS, ParticleEffectType.FIRE_EMBERS -> {
                drawCircle(
                    color = moodColor.copy(alpha = p.alpha),
                    radius = p.size * 0.6f,
                    center = Offset(px, py)
                )
            }
            ParticleEffectType.STARRY_GLOW -> {
                drawCircle(
                    color = Color.White.copy(alpha = p.alpha),
                    radius = p.size * 0.5f,
                    center = Offset(px, py)
                )
            }
            ParticleEffectType.NEON_GRID_STREAM -> {
                drawLine(
                    color = moodColor.copy(alpha = p.alpha),
                    start = Offset(px, py),
                    end = Offset(px, py + p.size * 3f),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }
            ParticleEffectType.NONE -> {}
        }
    }
}
