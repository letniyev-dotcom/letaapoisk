package com.letaa.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.Animatable
import kotlin.random.Random

private data class Spark(val x: Float, val size: Float, val delay: Float, val duration: Float, val color: Color)

private val sparkColors = listOf(
    Color(0xFFFFB84D), Color(0xFFFF8A5C), Color(0xFFFF5E8A), Color(0xFFFFD27A),
)

/**
 * Огонёк «сжечь всё»: тёплое зарево снизу + летящие искры. Длится ~1.15с —
 * ровно столько, сколько занимает реальная очистка данных приложения.
 */
@Composable
fun BurnOverlay(modifier: Modifier = Modifier) {
    val glow = remember { Animatable(0f) }
    val sparks = remember {
        List(26) {
            Spark(
                x = Random.nextFloat(),
                size = 4f + Random.nextFloat() * 8f,
                delay = Random.nextFloat() * 0.35f,
                duration = 0.7f + Random.nextFloat() * 0.7f,
                color = sparkColors[it % sparkColors.size],
            )
        }
    }
    val clock = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        glow.animateTo(1f, tween(300, easing = LinearEasing))
        glow.animateTo(0f, tween(700, easing = LinearEasing))
    }
    LaunchedEffect(Unit) {
        clock.animateTo(1f, tween(1150, easing = LinearEasing))
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val h = size.height
        val w = size.width
        // тёплое зарево снизу
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFFFF965A).copy(alpha = 0.45f * glow.value),
                    Color(0xFFFF6E40).copy(alpha = 0.85f * glow.value),
                ),
            ),
            size = size,
        )
        // искры
        val t = clock.value
        sparks.forEach { s ->
            val local = ((t - s.delay) / s.duration).coerceIn(0f, 1f)
            if (local <= 0f) return@forEach
            val alpha = if (local < 0.12f) local / 0.12f else (1f - local)
            val y = h - local * h * 0.95f
            val sz = s.size * (1f - local * 0.8f)
            drawCircle(
                color = s.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                radius = sz,
                center = Offset(s.x * w, y),
            )
        }
    }
}
