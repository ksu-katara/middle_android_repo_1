package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.abs

/**
 * Метод для вычисления поворота карты в конкретной позиции
 */
fun calculateCardRotation(
    cardIndex: Int,
    cardCount: Int,
    isRotated: Boolean,
): Float {
    if (cardCount <= 1) return 0f

    return if (isRotated) {
        val angleStep = 180f / (cardCount - 1)
        90f - (cardIndex * angleStep)
    } else {
        val angleStep = 45f / (cardCount - 1)
        22.5f - (cardIndex * angleStep)
    }
}

@Immutable
data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: AnimationStep? = null,
)

enum class AnimationStep {
    MoveRight,
    MoveTop,
    FinalTurn;
}

@Composable
fun AnimatedCardStack(
    cards: List<CardData>,
) {
    val cardCount = cards.size
    var isRotated by remember { mutableStateOf(false) }

    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }
    var currentCards by remember(cards) { mutableStateOf(cards) }

    Box(
        Modifier
            .pointerInput(Unit) {
                var horizontalDragOffset = 0f
                var verticalDragOffset = 0f
                detectDragGestures(
                    onDragStart = {
                        horizontalDragOffset = 0f
                        verticalDragOffset = 0f
                    },
                    onDrag = { _, dragAmount ->
                        horizontalDragOffset += dragAmount.x
                        verticalDragOffset += dragAmount.y
                    },
                    onDragEnd = {
                        if (!animationState.isAnimating) {
                            val threshold = 100f
                            val isVerticalDominant =
                                abs(verticalDragOffset) > abs(horizontalDragOffset)
                            val isHorizontalDominant =
                                abs(horizontalDragOffset) > abs(verticalDragOffset)

                            when {
                                isVerticalDominant && abs(verticalDragOffset) > threshold -> {
                                    handleVerticalSwipe(
                                        verticalDragDistance = verticalDragOffset,
                                        onFanStateChange = { newFanState ->
                                            isRotated = newFanState
                                        }
                                    )
                                }

                                isHorizontalDominant && abs(horizontalDragOffset) > threshold -> {
                                    handleHorizontalSwipe(
                                        horizontalDragDistance = horizontalDragOffset,
                                        onCardsReorder = {
                                            animationState = CardSwapAnimationState(
                                                isAnimating = true,
                                                animationStep = AnimationStep.MoveRight,
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        currentCards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(
                    cardIndex = i,
                    cardCount = cardCount,
                    isRotated = isRotated,
                )
                val finalRotation = calculateCardRotation(
                    cardIndex = if (i == 0) cardCount - 1 else i - 1,
                    cardCount = cardCount,
                    isRotated = isRotated,
                )

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    finalRotation = finalRotation,
                    cardData = cardData,
                    isAnimating = animationState.isAnimating,
                    animationStep = animationState.animationStep,
                    onStepChange = { nextStep ->
                        animationState = animationState.copy(animationStep = nextStep)
                    },
                    onAnimationComplete = {
                        currentCards = reorderCards(currentCards)
                        animationState = animationState.copy(
                            isAnimating = false,
                            animationStep = null,
                        )
                    },
                )
            }
        }
    }
}

private fun handleVerticalSwipe(
    verticalDragDistance: Float,
    onFanStateChange: (Boolean) -> Unit,
) {
    if (verticalDragDistance > 0) onFanStateChange(true) else onFanStateChange(false)
}

private fun handleHorizontalSwipe(
    horizontalDragDistance: Float,
    onCardsReorder: () -> Unit,
) {
    onCardsReorder()
}

fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}