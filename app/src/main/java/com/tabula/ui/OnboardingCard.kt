package com.tabula.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingCard(
    backgroundResId: Int,
    modifier: Modifier = Modifier,
    scrimColor: Color = Color.Black.copy(alpha = 0.3f),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    val borderColor = Color.Black.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .width(300.dp)
            .aspectRatio(9f / 16f)
            .background(Color.White, shape)
            .border(1.dp, borderColor, shape)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = backgroundResId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
