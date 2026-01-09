package com.tabula.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tabula.data.CurationMode
import com.tabula.data.LanguageMode
import com.tabula.data.ThemeMode
import com.tabula.R
import com.tabula.BuildConfig
import androidx.compose.foundation.Image

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
    onRescanLibrary: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader(title = stringResource(R.string.settings_section_general))
                SettingsCard {
                    Text(
                        text = stringResource(R.string.settings_session_size_format, sessionSize),
                        style = MaterialTheme.typography.titleMedium,
                        color = fg
                    )
                    Slider(
                        value = sessionSize.toFloat(),
                        onValueChange = { onSessionSizeChange(it.toInt()) },
                        valueRange = 5f..50f,
                        steps = 45,
                        colors = SliderDefaults.colors(
                            activeTrackColor = fg,
                            inactiveTrackColor = fg.copy(alpha = 0.2f),
                            thumbColor = fg
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_curation_mode),
                        style = MaterialTheme.typography.titleMedium,
                        color = fg
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = curationMode == CurationMode.BURST,
                            onClick = { onCurationModeChange(CurationMode.BURST) },
                            label = { Text(text = stringResource(R.string.settings_smart_clean)) },
                            colors = chipColors(fg, bg)
                        )
                        FilterChip(
                            selected = curationMode == CurationMode.RANDOM,
                            onClick = { onCurationModeChange(CurationMode.RANDOM) },
                            label = { Text(text = stringResource(R.string.settings_random_walk)) },
                            colors = chipColors(fg, bg)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(onClick = onRescanLibrary) {
                        Text(
                            text = stringResource(R.string.settings_rescan_library),
                            color = fg
                        )
                    }
                }
            }

            item {
                SectionHeader(title = stringResource(R.string.settings_section_theme))
                SettingsCard {
                    Text(
                        text = stringResource(R.string.settings_theme),
                        style = MaterialTheme.typography.titleMedium,
                        color = fg
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = themeMode == ThemeMode.LIGHT,
                            onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                            label = { Text(text = stringResource(R.string.settings_theme_light)) },
                            colors = chipColors(fg, bg)
                        )
                        FilterChip(
                            selected = themeMode == ThemeMode.DARK,
                            onClick = { onThemeModeChange(ThemeMode.DARK) },
                            label = { Text(text = stringResource(R.string.settings_theme_dark)) },
                            colors = chipColors(fg, bg)
                        )
                        FilterChip(
                            selected = themeMode == ThemeMode.SYSTEM,
                            onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                            label = { Text(text = stringResource(R.string.settings_theme_system)) },
                            colors = chipColors(fg, bg)
                        )
                    }
                }
            }

            item {
                SectionHeader(title = stringResource(R.string.settings_section_region))
                SettingsCard {
                    Text(
                        text = stringResource(R.string.settings_language),
                        style = MaterialTheme.typography.titleMedium,
                        color = fg
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = languageMode == LanguageMode.CN,
                            onClick = { onLanguageModeChange(LanguageMode.CN) },
                            label = { Text(text = stringResource(R.string.settings_language_cn)) },
                            colors = chipColors(fg, bg)
                        )
                        FilterChip(
                            selected = languageMode == LanguageMode.EN,
                            onClick = { onLanguageModeChange(LanguageMode.EN) },
                            label = { Text(text = stringResource(R.string.settings_language_en)) },
                            colors = chipColors(fg, bg)
                        )
                    }
                }
            }

            item {
                SectionHeader(title = stringResource(R.string.settings_section_privacy))
                if (isLimitedAccess) {
                    LimitedAccessBanner()
                    Spacer(modifier = Modifier.height(8.dp))
                }
                SettingsCard {
                    Text(
                        text = stringResource(R.string.settings_privacy_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.settings_privacy_description),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                SectionHeader(title = stringResource(R.string.settings_section_about))
                SettingsCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.tabula_logo),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.app_name),
                                color = fg,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(R.string.settings_version_format, BuildConfig.VERSION_NAME),
                                color = fg.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.settings_about_description),
                        color = fg,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun chipColors(fg: Color, bg: Color) = FilterChipDefaults.filterChipColors(
    selectedContainerColor = fg,
    selectedLabelColor = bg,
    containerColor = bg,
    labelColor = fg
)

@Composable
private fun LimitedAccessBanner() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.settings_limited_access_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.settings_limited_access_message),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            content()
        }
    }
}
