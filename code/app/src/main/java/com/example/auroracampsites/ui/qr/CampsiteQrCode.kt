package com.example.auroracampsites.ui.qr

import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.net.URLDecoder
import java.net.URLEncoder

/** Encodes the campsite document ID for Aurora's in-app scanner. */
internal object CampsiteQrCode {
    private const val PREFIX = "aurora-campsites://campsite/"
    private const val CHARSET = "UTF-8"

    /** Produces a stable payload for one Firestore document ID. */
    fun payload(campsiteId: String): String {
        require(isDocumentId(campsiteId))
        return PREFIX + URLEncoder.encode(campsiteId, CHARSET).replace("+", "%20")
    }

    /** Accepts only Aurora payloads that round-trip to one campsite document ID. */
    fun campsiteId(scannedValue: String): String? {
        if (!scannedValue.startsWith(PREFIX)) return null
        val decoded = try {
            URLDecoder.decode(scannedValue.removePrefix(PREFIX), CHARSET)
        } catch (_: IllegalArgumentException) {
            return null
        }
        return decoded.takeIf { isDocumentId(it) && payload(it) == scannedValue }
    }

    /** Generates QR pixels with the default quiet zone so the code is easier to scan. */
    fun matrix(campsiteId: String): BitMatrix =
        QRCodeWriter().encode(payload(campsiteId), BarcodeFormat.QR_CODE, 512, 512)

    private fun isDocumentId(id: String): Boolean =
        id.isNotBlank() && '/' !in id && id != "." && id != ".." &&
            !(id.startsWith("__") && id.endsWith("__")) && id.toByteArray(Charsets.UTF_8).size <= 1500
}
