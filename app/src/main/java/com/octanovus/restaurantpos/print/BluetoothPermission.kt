package com.octanovus.restaurantpos.print

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

private fun needsBtPermission() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * Returns a function that ensures BLUETOOTH_CONNECT is granted (Android 12+),
 * invoking onResult(true) immediately on older versions or when already granted.
 * Must be called during composition because it registers an activity-result launcher.
 */
@Composable
fun rememberBluetoothPermission(onResult: (Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> onResult(granted) }

    return remember {
        {
            when {
                !_root_ide_package_.com.octanovus.restaurantpos.print.needsBtPermission() -> onResult(true) // pre-Android 12: no runtime grant needed
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED -> onResult(true)
                else -> launcher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
    }
}
