package com.letaa.app.ui.components

// Механика переходов взята из Letify (LetifyOverlay.kt), стиль Push:
// исходящий и входящий экраны движутся ОДНИМ общим Animatable<Float>
// (progress 0..1) — оба читают его в graphicsLayer, поэтому физически
// не могут рассинхронизироваться и едут как одна непрерывная лента.
//
//   progress = 1 → входящий экран полностью за правым краем
//   progress = 0 → входящий экран по центру, исходящий полностью слева
//
// Программное открытие/закрытие — ease-out tween 360мс (без пружинного
// овершута); пружины только на отпускании свайпа-назад, где важно
// сохранить скорость пальца.

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PushEasing = CubicBezierEasing(0.32f, 0.72f, 0.0f, 1.0f)
private const val PushDurationMs = 360
val PushSpec = tween<Float>(PushDurationMs, easing = PushEasing)

// Пружины только для gesture-release (свайп-назад): критически задемпфированы,
// нулевой овершут — иначе на полноэкранном push виден «подрагивающий» хвост.
private val OverlaySpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = 500f,
)
private val OverlayCancelSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = 600f,
)

private val SwipeBackEdgeWidth: Dp = 120.dp
private const val SwipeBackCommitFraction = 0.22f
private val SwipeBackCommitVelocityDpPerSec: Dp = 550.dp

typealias SlideProgress = Animatable<Float, AnimationVector1D>

/** Общий драйвер push-прогресса. Стартует в 1f (ничего не открыто). */
@Composable
fun rememberSlideProgress(): SlideProgress = remember { Animatable(1f) }

/**
 * Исходящий экран: уезжает влево в lockstep со входящим оверлеем,
 * читая тот же [progress] (push — одна непрерывная лента, без параллакса).
 */
@Composable
fun PushHost(
    progress: SlideProgress,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val p = progress.value.coerceIn(0f, 1f)
                translationX = -(1f - p) * size.width
            },
    ) {
        content()
    }
}

/**
 * Входящий экран поверх остальных: въезжает справа, поддерживает
 * свайп-назад от левого края и predictive back. [content] получает
 * animatedBack — анимированное закрытие для кнопки «назад».
 */
@Composable
fun PushOverlay(
    progress: SlideProgress,
    onDismissed: () -> Unit,
    swipeBackEnabled: Boolean = true,
    content: @Composable (animatedBack: () -> Unit) -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val edgeWidthPx = with(density) { SwipeBackEdgeWidth.toPx() }
    val velocityCommitPx = with(density) { SwipeBackCommitVelocityDpPerSec.toPx() }
    val currentOnDismissed by rememberUpdatedState(onDismissed)

    // Готовность до движения: держим экран за краем (progress=1) и ждём два
    // реальных кадра — первая тяжёлая композиция/раскладка целевого экрана
    // происходит ДО начала слайда, чтобы движение не роняло кадры.
    LaunchedEffect(Unit) {
        progress.snapTo(1f)
        withFrameNanos {}
        withFrameNanos {}
        progress.animateTo(targetValue = 0f, animationSpec = PushSpec)
    }

    val animatedBack: () -> Unit = remember(scope) {
        {
            scope.launch {
                try {
                    progress.animateTo(targetValue = 1f, animationSpec = PushSpec)
                } finally {
                    withContext(NonCancellable) { currentOnDismissed() }
                }
            }
        }
    }

    PredictiveBackHandler(enabled = true) { progressFlow ->
        try {
            progressFlow.collect { event ->
                if (event.progress < 0.999f) {
                    progress.snapTo(event.progress)
                }
            }
            withContext(NonCancellable) {
                progress.animateTo(targetValue = 1f, animationSpec = PushSpec)
                currentOnDismissed()
            }
        } catch (_: CancellationException) {
            withContext(NonCancellable) {
                progress.animateTo(targetValue = 0f, animationSpec = PushSpec)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(swipeBackEnabled) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (!swipeBackEnabled) return@awaitEachGesture
                    if (down.position.x > edgeWidthPx) return@awaitEachGesture

                    var consumedRight = false
                    val drag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, dragAmount ->
                        if (dragAmount > 0) {
                            consumedRight = true
                            change.consume()
                        }
                    } ?: return@awaitEachGesture
                    if (!consumedRight) return@awaitEachGesture

                    val velocityTracker = VelocityTracker()
                    velocityTracker.addPosition(down.uptimeMillis, down.position)
                    velocityTracker.addPosition(drag.uptimeMillis, drag.position)

                    val width = size.width.toFloat()
                    val initialDx = (drag.position.x - down.position.x).coerceAtLeast(0f)
                    var fingerProgress = (initialDx / width).coerceIn(0f, 1f)
                    scope.launch { progress.snapTo(fingerProgress) }

                    var released = false
                    while (!released) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            released = true
                        } else {
                            val dx = (change.position.x - down.position.x).coerceAtLeast(0f)
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            change.consume()
                            fingerProgress = (dx / width).coerceIn(0f, 1f)
                            scope.launch { progress.snapTo(fingerProgress) }
                        }
                    }

                    val velocity = velocityTracker.calculateVelocity().x
                    val shouldDismiss =
                        fingerProgress >= SwipeBackCommitFraction ||
                            velocity >= velocityCommitPx
                    if (shouldDismiss) {
                        scope.launch {
                            try {
                                progress.snapTo(fingerProgress)
                                progress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = OverlaySpring,
                                    initialVelocity = velocity / width,
                                )
                            } finally {
                                withContext(NonCancellable) { currentOnDismissed() }
                            }
                        }
                    } else {
                        scope.launch {
                            progress.snapTo(fingerProgress)
                            progress.animateTo(
                                targetValue = 0f,
                                animationSpec = OverlayCancelSpring,
                                initialVelocity = velocity / width,
                            )
                        }
                    }
                }
            }
            .graphicsLayer {
                val p = progress.value.coerceAtLeast(0f)
                translationX = p * size.width
            },
    ) {
        content(animatedBack)
    }
}
