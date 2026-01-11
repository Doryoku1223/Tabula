@file:OptIn(ExperimentalMaterial3Api::class)

package com.tabula.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tabula.R
import com.tabula.data.CurationMode
import com.tabula.data.LanguageMode
import com.tabula.data.ThemeMode
import kotlin.math.roundToInt
import androidx.compose.foundation.isSystemInDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    sessionSize: Int,
    curationMode: CurationMode,
    themeMode: ThemeMode,
    languageMode: LanguageMode,
    isLimitedAccess: Boolean,
    onBack: () -> Unit,
    onSessionSizeChange: (Int) -> Unit,
    onCurationModeChange: (CurationMode) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageModeChange: (LanguageMode) -> Unit,
    onOpenAbout: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground
    val useDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val secondary = if (useDark) Color(0xFFAAAAAA) else Color(0xFF666666)
    val divider = if (useDark) Color(0xFF1F1F1F) else Color(0xFFF0F0F0)
    val segmentedBg = if (useDark) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)
    val segmentedBorder = if (useDark) Color(0xFF2A2A2A) else Color(0xFFF0F0F0)

    var showThemePicker by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }

    var sliderValue by remember(sessionSize) { mutableStateOf(sessionSize.toFloat()) }
    var lastHaptic by remember { mutableIntStateOf(sessionSize) }
    val haptic = LocalHapticFeedback.current

    Scaffold(
            containerColor = bg,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                            tint = fg
                        )
                    }
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = fg
                    )
                }
            }
    ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                SectionTitle(text = stringResource(R.string.settings_section_count), color = fg)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.settings_session_size_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = secondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = sliderValue.roundToInt().toString(),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    color = fg
                )
                Spacer(modifier = Modifier.height(12.dp))
                MinimalSlider(
                    value = sliderValue,
                    onValueChange = { value ->
                        sliderValue = value
                        val intValue = value.roundToInt().coerceIn(5, 25)
                        if (intValue != lastHaptic) {
                            lastHaptic = intValue
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    onValueChangeFinished = {
                        onSessionSizeChange(sliderValue.roundToInt().coerceIn(5, 25))
                    },
                    activeColor = fg,
                    inactiveColor = divider
                )

                Spacer(modifier = Modifier.height(32.dp))

                SectionTitle(text = stringResource(R.string.settings_section_mode), color = fg)
                Spacer(modifier = Modifier.height(12.dp))
                SegmentedControl(
                    options = listOf(
                        stringResource(R.string.settings_smart_clean),
                        stringResource(R.string.settings_random_walk)
                    ),
                    selectedIndex = if (curationMode == CurationMode.BURST) 0 else 1,
                    onSelected = { index ->
                        onCurationModeChange(if (index == 0) CurationMode.BURST else CurationMode.RANDOM)
                    },
                    bgColor = segmentedBg,
                    borderColor = segmentedBorder,
                    selectedColor = if (useDark) Color.White else Color.Black,
                    textColor = secondary,
                    selectedTextColor = if (useDark) Color.Black else Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                SectionTitle(text = stringResource(R.string.settings_section_preferences), color = fg)
                Spacer(modifier = Modifier.height(8.dp))
                SettingsRow(
                    label = stringResource(R.string.settings_theme_label),
                    value = themeLabel(themeMode),
                    onClick = { showThemePicker = true },
                    fg = fg,
                    secondary = secondary
                )
                HorizontalDivider(color = divider, thickness = 1.dp)
                SettingsRow(
                    label = stringResource(R.string.settings_language_label),
                    value = languageLabel(languageMode),
                    onClick = { showLanguagePicker = true },
                    fg = fg,
                    secondary = secondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                SectionTitle(text = stringResource(R.string.settings_section_data), color = fg)
                Spacer(modifier = Modifier.height(8.dp))
                SettingsRow(
                    label = stringResource(R.string.settings_about_us),
                    value = "",
                    onClick = onOpenAbout,
                    fg = fg,
                    secondary = secondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                TrustBadge(fg = fg, secondary = secondary)

                if (isLimitedAccess) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.settings_limited_access_message),
                        color = secondary,
                        style = MaterialTheme.typography.bodySmall
                    )
        }
    }
    }

    if (showThemePicker) {
        SelectionDialog(
            title = stringResource(R.string.settings_theme_label),
            options = listOf(
                stringResource(R.string.settings_theme_light) to ThemeMode.LIGHT,
                stringResource(R.string.settings_theme_dark) to ThemeMode.DARK,
                stringResource(R.string.settings_theme_system) to ThemeMode.SYSTEM
            ),
            selected = themeMode,
            onSelect = {
                onThemeModeChange(it)
                showThemePicker = false
            },
            onDismiss = { showThemePicker = false },
            bg = bg,
            fg = fg,
            secondary = secondary
        )
    }

    if (showLanguagePicker) {
        SelectionDialog(
            title = stringResource(R.string.settings_language_label),
            options = listOf(
                stringResource(R.string.settings_language_cn) to LanguageMode.CN,
                stringResource(R.string.settings_language_en) to LanguageMode.EN
            ),
            selected = languageMode,
            onSelect = {
                onLanguageModeChange(it)
                showLanguagePicker = false
            },
            onDismiss = { showLanguagePicker = false },
            bg = bg,
            fg = fg,
            secondary = secondary
        )
    }
}

@Composable
private fun SectionTitle(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
private fun SettingsRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    fg: Color,
    secondary: Color,
    showArrow: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = fg
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondary
                )
            }
            if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = secondary,
                    modifier = Modifier.padding(start = if (value.isNotEmpty()) 8.dp else 0.dp)
                )
            }
        }
    }
}

@Composable
private fun MinimalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    activeColor: Color,
    inactiveColor: Color
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = 5f..25f,
        steps = 19,
        colors = SliderDefaults.colors(
            thumbColor = activeColor,
            activeTrackColor = activeColor,
            inactiveTrackColor = inactiveColor,
            activeTickColor = activeColor,
            inactiveTickColor = inactiveColor
        ),
        thumb = {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(activeColor)
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                colors = SliderDefaults.colors(
                    activeTrackColor = activeColor,
                    inactiveTrackColor = inactiveColor
                ),
                modifier = Modifier.height(2.dp)
            )
        }
    )
}

@Composable
private fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    bgColor: Color,
    borderColor: Color,
    selectedColor: Color,
    textColor: Color,
    selectedTextColor: Color,
    height: Dp = 42.dp
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .padding(2.dp)
    ) {
        val segmentWidth = maxWidth / options.size
        val indicatorOffset by animateDpAsState(targetValue = segmentWidth * selectedIndex, label = "segment")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(999.dp))
                .background(bgColor)
                .padding(2.dp)
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxSize()
                .clip(RoundedCornerShape(999.dp))
                .background(selectedColor)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val alpha by animateFloatAsState(targetValue = if (selected) 1f else 0.7f, label = "segmentAlpha")
                Text(
                    text = label,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelected(index) }
                        .padding(vertical = 10.dp),
                    color = if (selected) selectedTextColor else textColor.copy(alpha = alpha),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(999.dp))
                .background(Color.Transparent)
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(999.dp))
        )
    }
}

@Composable
private fun TrustBadge(fg: Color, secondary: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = secondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.settings_trust_badge),
            color = secondary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun <T> SelectionDialog(
    title: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    bg: Color,
    fg: Color,
    secondary: Color
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = bg,
            contentColor = fg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = fg
                )
                options.forEach { (label, value) ->
                    val isSelected = value == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(value) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) fg else secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun themeLabel(mode: ThemeMode): String {
    return when (mode) {
        ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
        ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
        ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    }
}

@Composable
private fun languageLabel(mode: LanguageMode): String {
    return when (mode) {
        LanguageMode.CN -> stringResource(R.string.settings_language_cn)
        LanguageMode.EN -> stringResource(R.string.settings_language_en)
    }
}
