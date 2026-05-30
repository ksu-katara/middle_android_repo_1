package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    finalRotation: Float,
    isAnimating: Boolean,
    animationStep: AnimationStep?,
    onStepChange: (AnimationStep) -> Unit,
    onAnimationComplete: () -> Unit,
) {
    val density = LocalDensity.current

    val animatedTranslationX by animateFloatAsState(
        targetValue = when {
            isAnimating && animationStep == AnimationStep.MoveRight && cardIndex == 0 -> {
                val moveDistance = with(density) { 50.dp.toPx() }
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                moveDistance * cos(rotationRad).toFloat()
            }
            isAnimating && animationStep == AnimationStep.MoveTop -> 0f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 300),
        finishedListener = {
            if (isAnimating && (animationStep == AnimationStep.MoveRight || animationStep == AnimationStep.MoveTop)) {
                handleAnimationStepComplete(
                    step = animationStep,
                    cardIndex = cardIndex,
                    onStepChange = onStepChange,
                    onAnimationComplete = onAnimationComplete
                )
            }
        },
        label = "translationX",
    )

    val animatedTranslationY by animateFloatAsState(
        targetValue = when {
            isAnimating && animationStep == AnimationStep.MoveRight && cardIndex == 0 -> {
                val moveDistance = with(density) { 50.dp.toPx() }
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                moveDistance * sin(rotationRad).toFloat()
            }
            isAnimating && animationStep == AnimationStep.MoveTop -> 0f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "translationY",
    )

    val animatedRotation by animateFloatAsState(
        targetValue = when {
            animationStep == AnimationStep.FinalTurn -> finalRotation
            isAnimating -> targetRotation
            else -> targetRotation
        },
        animationSpec = tween(durationMillis = if (animationStep == AnimationStep.FinalTurn) 300 else 800),
        finishedListener = {
            if (isAnimating && animationStep == AnimationStep.FinalTurn) {
                handleAnimationStepComplete(
                    step = animationStep,
                    cardIndex = cardIndex,
                    onStepChange = onStepChange,
                    onAnimationComplete = onAnimationComplete
                )
            }
        },
        label = "rotation",
    )

    val shouldBringToFront = isAnimating && (animationStep == AnimationStep.MoveTop || animationStep == AnimationStep.FinalTurn) && cardIndex == 0

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            .graphicsLayer {
                translationX = if (isAnimating) animatedTranslationX else 0f
                translationY = if (isAnimating) animatedTranslationY else 0f
                rotationZ = animatedRotation
                transformOrigin = TransformOrigin(0.5f, 1.0f)
            }
            .let { modifier ->
                if (shouldBringToFront) {
                    modifier.zIndex(1000f)
                } else {
                    modifier
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (4 + cardIndex).dp
        ),
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(cardData.imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

fun handleAnimationStepComplete(
    step: AnimationStep,
    cardIndex: Int,
    onStepChange: (AnimationStep) -> Unit,
    onAnimationComplete: () -> Unit
) {
    if (cardIndex == 0) {
        when (step) {
            AnimationStep.MoveRight -> onStepChange(AnimationStep.MoveTop)
            AnimationStep.MoveTop -> onStepChange(AnimationStep.FinalTurn)
            AnimationStep.FinalTurn -> onAnimationComplete()
        }
    }
}