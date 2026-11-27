package com.example.auroracampsites

import com.example.auroracampsites.ui.qr.CampsiteQrCode
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

/** Checks that the displayed QR pixels encode the intended campsite. */
class CampsiteQrCodeTest {
    @Test
    fun generatedCodesDecodeToDifferentCampsiteIds() {
        val first = "first-campsite"
        val second = "second-campsite"
        assertNotEquals(CampsiteQrCode.matrix(first), CampsiteQrCode.matrix(second))
        assertEquals(first, CampsiteQrCode.campsiteId(readCode(first)))
        assertEquals(second, CampsiteQrCode.campsiteId(readCode(second)))
    }

    @Test
    fun specialCharactersSurviveTheRoundTrip() {
        val id = "Jasper #2 + café%23?"
        assertEquals(id, CampsiteQrCode.campsiteId(readCode(id)))
    }

    @Test
    fun rejectsInvalidOrUnrelatedScans() {
        listOf(
            "",
            "https://example.com/campsite/abc",
            "aurora-campsites://campsite/",
            "aurora-campsites://campsite/%",
            "aurora-campsites://campsite/%FF",
            "aurora-campsites://campsite/first%2Fsecond",
            "aurora-campsites://campsite/first?extra=true"
        ).forEach { assertNull(CampsiteQrCode.campsiteId(it)) }
        assertThrows(IllegalArgumentException::class.java) { CampsiteQrCode.matrix("first/second") }
    }

    private fun readCode(campsiteId: String): String {
        val matrix = CampsiteQrCode.matrix(campsiteId)
        val pixels = IntArray(matrix.width * matrix.height) { index ->
            if (matrix[index % matrix.width, index / matrix.width]) 0xff000000.toInt() else -1
        }
        val bitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(matrix.width, matrix.height, pixels)))
        return QRCodeReader().decode(bitmap, mapOf(DecodeHintType.PURE_BARCODE to true)).text
    }
}
