package com.tabula.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tabula.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

data class OnboardingPage(
    val title: String,
    val desc: String,
    val imageRes: Int
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val pages = remember {
        listOf(
            OnboardingPage(
                title = "欢迎使用",
                desc = "一切从零开始，重塑你的相册",
                imageRes = R.drawable.bg_onboarding_flow
            ),
            OnboardingPage(
                title = "直觉式整理",
                desc = "上滑 · 归档\n向右滑动 · 上一张\n向左滑动 · 下一张",
                imageRes = R.drawable.bg_onboarding_flow
            ),
            OnboardingPage(
                title = "安全且自由",
                desc = "所有运算完全本地运行。\n无需联网，你的隐私只属于你。",
                imageRes = R.drawable.bg_onboarding_finish
            ),
            OnboardingPage(
                title = "准备好了吗？",
                desc = "",
                imageRes = R.drawable.bg_onboarding_finish
            )
        )
    }

    val backgroundColor = Color(0xFF121212)
    val indicatorColor = Color.White
    val translationPx = with(LocalDensity.current) { 40.dp.toPx() }
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 40.dp),
            pageSpacing = (-24).dp,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val clampedOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)
            val scale = 1f - (0.15f * clampedOffset)
            val alpha = 1f - (0.3f * clampedOffset)
            val zIndex = when {
                clampedOffset < 0.01f -> 2f
                pageOffset < 0f -> 1f
                else -> 0f
            }

            val scope = rememberCoroutineScope()
            val swipeOffsetY = remember(page) { Animatable(0f) }
            val swipeAlpha = remember(page) { Animatable(1f) }
            var swipeLocked by remember(page) { mutableStateOf(false) }

            val swipeModifier = if (page == 1) {
                Modifier.pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            if (swipeLocked) return@detectVerticalDragGestures
                            change.consume()
                            scope.launch {
                                swipeOffsetY.snapTo(
                                    (swipeOffsetY.value + dragAmount).coerceAtMost(80f)
                                )
                            }
                        },
                        onDragEnd = {
                            if (swipeLocked) return@detectVerticalDragGestures
                            scope.launch {
                                if (swipeOffsetY.value < -180f) {
                                    swipeLocked = true
                                    val flyJob = launch {
                                        swipeOffsetY.animateTo(
                                            targetValue = -screenHeightPx,
                                            animationSpec = spring(dampingRatio = 0.85f, stiffness = 280f)
                                        )
                                    }
                                    val fadeJob = launch {
                                        swipeAlpha.animateTo(0f, animationSpec = tween(durationMillis = 200))
                                    }
                                    flyJob.join()
                                    fadeJob.join()
                                    delay(600)
                                    swipeOffsetY.snapTo(screenHeightPx * 0.35f)
                                    swipeAlpha.snapTo(0f)
                                    val riseJob = launch {
                                        swipeOffsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(dampingRatio = 0.9f, stiffness = 320f)
                                        )
                                    }
                                    val appearJob = launch {
                                        swipeAlpha.animateTo(1f, animationSpec = tween(durationMillis = 260))
                                    }
                                    riseJob.join()
                                    appearJob.join()
                                    swipeLocked = false
                                } else {
                                    swipeOffsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(dampingRatio = 0.9f, stiffness = 320f)
                                    )
                                }
                            }
                        }
                    )
                }
            } else {
                Modifier
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = pageOffset * translationPx
                        scaleX = scale
                        scaleY = scale
                        rotationZ = pageOffset * -10f
                        this.alpha = alpha
                    }
                    .zIndex(zIndex),
                contentAlignment = Alignment.Center
            ) {
                val scrim = if (page == 0 || page == 3) {
                    Color.White.copy(alpha = 0.9f)
                } else {
                    Color.Black.copy(alpha = 0.18f)
                }

                OnboardingCard(
                    backgroundResId = pages[page].imageRes,
                    scrimColor = scrim,
                    modifier = Modifier
                        .offset { IntOffset(0, swipeOffsetY.value.roundToInt()) }
                        .alpha(swipeAlpha.value)
                        .then(swipeModifier)
                ) {
                    OnboardingPageContent(
                        page = page,
                        data = pages[page],
                        onFinish = onFinish
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(pages.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 10.dp else 8.dp)
                        .background(
                            color = if (isSelected) indicatorColor else indicatorColor.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: Int,
    data: OnboardingPage,
    onFinish: () -> Unit
) {
    val isLightCard = page == 0 || page == 3
    val textColor = if (isLightCard) Color.Black else Color.White
    val accentBlue = Color(0xFF1E88E5)
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding_arrow")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_offset"
    )
    val sidePulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "side_pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (page) {
            0 -> {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tabula",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentBlue
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = data.desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
            1 -> {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(bottom = 4.dp)
                        .offset(y = bounceOffset.dp)
                )
                Text(
                    text = data.desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier
                            .size(26.dp)
                            .alpha(sidePulse)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier
                            .size(26.dp)
                            .alpha(sidePulse)
                    )
                }
            }
            2 -> {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = data.desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.85f)
                )
            }
            else -> {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "开始",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "释放",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentBlue
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "您的相册空间",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onFinish,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(52.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}
