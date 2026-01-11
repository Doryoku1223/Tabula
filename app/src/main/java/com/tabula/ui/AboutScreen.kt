@file:OptIn(ExperimentalFoundationApi::class)

package com.tabula.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.tabula.BuildConfig
import com.tabula.R
import android.content.Intent
import android.net.Uri
import android.widget.Toast

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val githubUrl = stringResource(R.string.about_github_url)

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
                    text = stringResource(R.string.settings_about_us),
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                contentColor = fg,
                shape = RoundedCornerShape(22.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tabula_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(96.dp)
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = fg
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.settings_version_format, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyMedium,
                color = fg.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.about_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = fg
            )

            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                color = MaterialTheme.colorScheme.surface,
                contentColor = fg,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_version_date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = fg
                    )
                    Text(
                        text = stringResource(R.string.about_features),
                        style = MaterialTheme.typography.bodyMedium,
                        color = fg
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.about_github_label),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = fg
                        )
                        Text(
                            text = githubUrl,
                            style = MaterialTheme.typography.bodyMedium,
                            color = fg,
                            modifier = Modifier.combinedClickable(
                                onClick = {
                                    clipboard.setText(AnnotatedString(githubUrl))
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.about_github_copied),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onDoubleClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                                    context.startActivity(intent)
                                }
                            )
                        )
                        Text(
                            text = stringResource(R.string.about_github_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = fg.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}
