package me.pengj.arcompose

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.*

private const val PARTICLE_QUANTITY = 100
private const val DEFAULT_SPEED = 2
private const val VARIANT_SPEED = 1
private const val DEFAULT_RADIUS = 4
private const val VARIANT_RADIUS = 10
private const val LINK_RADIUS = 200

// TODO: 30/09/2020 These should be measured but are only used to calculate
// the initial position
private const val WIDTH = 1080f
private const val HEIGHT = 2022f

@Composable
fun DynamicPointMesh(modifier: Modifier = Modifier, color: MeshColor) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
        )
    )

    // remove the array, only change the color
    val particles =
        (0 until PARTICLE_QUANTITY)
            .map { generateParticle(WIDTH, HEIGHT, color.mainColor) }
            .toTypedArray()

    Canvas(modifier = modifier) {
        // Unused but required for draw update
        // There may be a better way to do this
        @Suppress("UNUSED_VARIABLE") val progress = animatedProgress.absoluteValue

        particles.forEachIndexed { index, particle ->
            particles[index] = particle.update(size.width, size.height)

            drawCircle(
                color = particles[index].color,
                radius = particles[index].radius,
                center = Offset(particles[index].x, particles[index].y),
            )

            linkParticles(particles[index], particles, this, color.secondaryColor)
        }
    }
}

private data class Vector(val x: Float, val y: Float)

private data class Particle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val directionAngle: Float,
    val color: Color,
    val radius: Float,
    val vector: Vector
) {

    fun update(w: Float, h: Float): Particle {
        val border = calculateBorder(w, h)

        return border.copy(x = border.x + border.vector.x, y = border.y + border.vector.y)
    }

    private fun calculateBorder(w: Float, h: Float): Particle = copy(
        vector = vector.copy(
            x = when {
                x >= w || x <= 0 -> vector.x * -1
                else -> vector.x
            },
            y = when {
                y >= h || y <= 0 -> vector.y * -1
                else -> vector.y
            }
        ),
        x = when {
            x > w -> w
            x < 0 -> 0f
            else -> x
        },
        y = when {
            y > h -> h
            y < 0 -> 0f
            else -> y
        }
    )
}

private fun generateParticle(w: Float, h: Float, color: Color): Particle {
    val directionAngle = floor(Math.random() * 360)
    val speed = DEFAULT_SPEED + Math.random() * VARIANT_SPEED

    return Particle(
        x = Math.random().toFloat() * w,
        y = Math.random().toFloat() * h,
        speed = speed.toFloat(),
        directionAngle = directionAngle.toFloat(),
        color = color,
        radius = DEFAULT_RADIUS + Math.random().toFloat() * VARIANT_RADIUS,
        Vector(
            x = (cos(directionAngle) * speed).toFloat(),
            y = (sin(directionAngle) * speed).toFloat()
        )
    )
}

private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float =
    sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))

private fun linkParticles(
    currentParticle: Particle,
    particles: Array<Particle>,
    drawScope: DrawScope,
    color: Color
) {
    particles.forEach { particle ->
        val distance =
            calculateDistance(currentParticle.x, currentParticle.y, particle.x, particle.y);
        val opacity = 1 - distance / LINK_RADIUS
        if (opacity > 0) {
            drawScope.drawLine(
                color = color.copy(alpha = opacity),
                start = Offset(currentParticle.x, currentParticle.y),
                end = Offset(particle.x, particle.y),
                strokeWidth = 0.5f
            )
        }
    }
}