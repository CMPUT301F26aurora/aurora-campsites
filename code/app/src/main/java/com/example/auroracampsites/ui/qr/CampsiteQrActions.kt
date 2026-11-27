package com.example.auroracampsites.ui.qr

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Shows a QR code for the campsite selected in the owner's list. */
@Composable
internal fun CampsiteQrButton(campsiteId: String, campsiteName: String) {
    var showCode by rememberSaveable(campsiteId) { mutableStateOf(false) }
    Button(onClick = { showCode = true }, modifier = Modifier.fillMaxWidth()) {
        Text("Generate QR Code")
    }
    if (showCode) {
        val bitmap = remember(campsiteId) {
            runCatching {
                val matrix = CampsiteQrCode.matrix(campsiteId)
                val pixels = IntArray(matrix.width * matrix.height) { index ->
                    if (matrix[index % matrix.width, index / matrix.width]) Color.BLACK else Color.WHITE
                }
                Bitmap.createBitmap(pixels, matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
                    .asImageBitmap()
            }.getOrNull()
        }
        AlertDialog(
            onDismissRequest = { showCode = false },
            title = { Text("Campsite QR Code") },
            text = {
                if (bitmap == null) {
                    Text("Unable to generate a QR code for this campsite.")
                } else {
                    Image(bitmap = bitmap, contentDescription = "QR code for $campsiteName",
                        modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = { TextButton(onClick = { showCode = false }) { Text("Close") } }
        )
    }
}

/** Prevents overlapping scanner sessions and only opens validated campsite IDs. */
@Composable
internal fun ScanCampsiteQrButton(scan: (suspend () -> String?)?, onCampsiteScanned: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var scanning by remember { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    Button(
        enabled = !scanning,
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            if (scanning) return@Button
            scanning = true
            error = null
            scope.launch {
                try {
                    val value = if (scan == null) scanCampsiteQr(context) else scan()
                    if (value != null) {
                        val id = CampsiteQrCode.campsiteId(value)
                        if (id == null) error = "This QR code is not for an Aurora campsite."
                        else onCampsiteScanned(id)
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    error = "Unable to scan a campsite QR code. Please try again."
                } finally {
                    scanning = false
                }
            }
        }
    ) {
        Text(if (scanning) "Scanning…" else "Scan Campsite QR Code")
    }
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}
