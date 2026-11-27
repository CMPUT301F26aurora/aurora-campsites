package com.example.auroracampsites.ui.qr

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.tasks.await

/** Opens Google's scanner and returns null when the user closes it. */
internal suspend fun scanCampsiteQr(context: Context): String? {
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    return try {
        GmsBarcodeScanning.getClient(context, options).startScan().await().rawValue.orEmpty()
    } catch (_: CancellationException) {
        // Closing the scanner is normal, but leaving the screen must cancel this call.
        currentCoroutineContext().ensureActive()
        null
    }
}
