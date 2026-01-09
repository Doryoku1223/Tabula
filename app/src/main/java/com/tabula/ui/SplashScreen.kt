package com.tabula.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tabula.R

@Composable
fun SplashScreen(
    onEnter: () -> Unit,
    modifier: Modifier = Modifier
) {
    var titleFinished by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        OnboardingCard(
            backgroundResId = R.drawable.bg_splash,
            isLightCard = false
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TypewriterText(
                    text = stringResource(R.string.splash_title),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    ),
                    onFinished = { titleFinished = true }
                )
                Spacer(modifier = Modifier.height(12.dp))
                TypewriterText(
                    text = stringResource(R.string.splash_subtitle),
                    start = titleFinished,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.Black.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = onEnter,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.splash_enter),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

    }
}
