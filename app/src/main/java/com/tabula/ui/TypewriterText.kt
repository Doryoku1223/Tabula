package com.tabula.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    start: Boolean = true,
    typeDelayMs: Long = 60L,
    cursorBlinkMs: Long = 450L,
    style: TextStyle = TextStyle.Default,
    fontFamily: FontFamily = FontFamily.Monospace,
    onFinished: (() -> Unit)? = null
) {
    var visibleText by remember(text, start) { mutableStateOf("") }
    var cursorVisible by remember(start) { mutableStateOf(true) }

    LaunchedEffect(text, start) {
        visibleText = ""
        if (!start) return@LaunchedEffect
        text.forEachIndexed { index, _ ->
            visibleText = text.substring(0, index + 1)
            delay(typeDelayMs)
        }
        onFinished?.invoke()
    }

    LaunchedEffect(start) {
        if (!start) return@LaunchedEffect
        while (true) {
            cursorVisible = !cursorVisible
            delay(cursorBlinkMs)
        }
    }

    Text(
        text = visibleText + if (cursorVisible) "|" else " ",
        modifier = modifier,
        style = style.copy(fontFamily = fontFamily)
    )
}
