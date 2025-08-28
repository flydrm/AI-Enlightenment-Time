package com.enlightenment.presentation.components.animations

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier







/**
 * 页面过渡动画
 */
object PageTransitions {
    
    /**
     * 滑动进入动画
     */
    @OptIn(ExperimentalAnimationApi::class)
    val slideInTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        )
    }
    
    /**
     * 滑动退出动画
     */
    @OptIn(ExperimentalAnimationApi::class)
    val slideOutTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        )
    }
    
    /**
     * 淡入淡出过渡
     */
    val fadeTransition = fadeIn(
        animationSpec = tween(300)
    ) with fadeOut(
        animationSpec = tween(300)
    )
    
    /**
     * 缩放过渡
     */
    val scaleTransition = scaleIn(
        initialScale = 0.9f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) with scaleOut(
        targetScale = 1.1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
    
    /**
     * 共享元素过渡容器
     */
    @Composable
    fun SharedElementTransition(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        AnimatedContent(
            targetState = content,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with
                        fadeOut(animationSpec = tween(300))
            }
        ) {
            it()
        }
    }
}
/**
 * 页面切换动画包装器
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedPage(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = slideInVertically(
        initialOffsetY = { it },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    ) + fadeIn(),
    exit: ExitTransition = slideOutVertically(
        targetOffsetY = { -it },
        animationSpec = tween(300)
    ) + fadeOut(),
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        content = content
    )
}
/**
 * 交错动画列表
 */
@Composable
fun <T> StaggeredAnimatedList(
    items: List<T>,
    modifier: Modifier = Modifier,
    delayBetweenItems: Int = 50,
    itemContent: @Composable (T, Int) -> Unit
) {
    items.forEachIndexed { index, item ->
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = index * delayBetweenItems
                )
            ) + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = index * delayBetweenItems,
                    easing = FastOutSlowInEasing
                )
            )
        ) {
            itemContent(item, index)
        }
    }
}
/**
 * 展开收起动画
 */
@Composable
fun ExpandableContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeOut(),
        content = content
    )
}
