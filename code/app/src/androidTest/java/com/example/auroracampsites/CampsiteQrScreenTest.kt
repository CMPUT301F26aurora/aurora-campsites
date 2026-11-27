package com.example.auroracampsites

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.auroracampsites.ui.navigation.DocumentIdNavType
import com.example.auroracampsites.ui.qr.CampsiteQrButton
import com.example.auroracampsites.ui.qr.CampsiteQrCode
import com.example.auroracampsites.ui.screens.HomeScreen
import kotlinx.coroutines.CompletableDeferred
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Covers QR display and the home-to-details route without opening a real camera. */
@RunWith(AndroidJUnit4::class)
class CampsiteQrScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun ownerCanDisplayTheSelectedCampsiteCode() {
        composeRule.setContent { CampsiteQrButton("site-1", "Pine Creek") }

        composeRule.onNodeWithText("Generate QR Code").performClick()
        composeRule.onNodeWithContentDescription("QR code for Pine Creek").assertExists()
    }

    @Test
    fun validScanOpensTheCorrectDetailsRoute() {
        val campsiteId = "site#2"
        showHome { CampsiteQrCode.payload(campsiteId) }

        composeRule.onNodeWithText("Scan Campsite QR Code").performClick()
        composeRule.onNodeWithText("Details: site#2").assertExists()
    }

    @Test
    fun scanKeepsTheLiteralNullDocumentId() {
        showHome { CampsiteQrCode.payload("null") }

        composeRule.onNodeWithText("Scan Campsite QR Code").performClick()
        composeRule.onNodeWithText("Details: null").assertExists()
    }

    @Test
    fun invalidScanStaysOnHomeAndShowsError() {
        showHome { "https://example.com/other" }

        composeRule.onNodeWithText("Scan Campsite QR Code").performClick()
        composeRule.onNodeWithText("This QR code is not for an Aurora campsite.").assertExists()
        composeRule.onNodeWithText("Scan Campsite QR Code").assertExists()
    }

    @Test
    fun scanButtonIsDisabledUntilTheScannerReturns() {
        val result = CompletableDeferred<String?>()
        showHome { result.await() }

        composeRule.onNodeWithText("Scan Campsite QR Code").performClick()
        composeRule.onNodeWithText("Scanning…").assertIsNotEnabled()
        composeRule.runOnIdle { result.complete(null) }
        composeRule.onNodeWithText("Scan Campsite QR Code").assertExists()
    }

    private fun showHome(scan: suspend () -> String?) {
        composeRule.setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "home") {
                composable("home") { HomeScreen(navController, scanQr = scan) }
                composable(
                    "campsite_details/{campsiteId}",
                    arguments = listOf(navArgument("campsiteId") { type = DocumentIdNavType })
                ) { entry ->
                    Text("Details: ${entry.arguments?.getString("campsiteId") ?: "(missing)"}")
                }
            }
        }
    }
}
