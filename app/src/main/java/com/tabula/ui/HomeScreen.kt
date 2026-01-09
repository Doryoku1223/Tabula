package com.tabula.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tabula.R
import com.tabula.viewmodel.HomeUiState
import com.tabula.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val backgroundColor = Color(0xFF121212)
    val permission = remember {
        if (Build.VERSION.SDK_INT >= 33) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val shouldShowRationale = activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
        } ?: false
        viewModel.onPermissionResult(granted, shouldShowRationale)
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        viewModel.setInitialPermission(granted)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        when (val state = uiState) {
            HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    TypewriterText(
                        text = stringResource(R.string.home_loading),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            HomeUiState.PermissionRequired -> {
                PermissionCard(
                    title = stringResource(R.string.home_permission_title),
                    body = stringResource(R.string.home_permission_body),
                    buttonText = stringResource(R.string.home_permission_action),
                    onClick = { permissionLauncher.launch(permission) }
                )
            }
            HomeUiState.PermissionDenied -> {
                PermissionCard(
                    title = stringResource(R.string.home_permission_denied_title),
                    body = stringResource(R.string.home_permission_denied_body),
                    buttonText = stringResource(R.string.home_permission_settings),
                    onClick = { context.openAppSettings() }
                )
            }
            is HomeUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = state.monthLabel,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(280.dp)
                            .height(420.dp)
                            .background(Color(0xFF2A2A2A), RoundedCornerShape(24.dp))
                    )

                    Text(
                        text = stringResource(R.string.home_photos_left, state.remainingCount),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    body: String,
    buttonText: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    val borderColor = Color.Black.copy(alpha = 0.2f)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .background(Color.White, shape)
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = body,
                color = Color.Black.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(text = buttonText)
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
