package com.example.auroracampsites.ui.screens

/*
 * US 01.11.01 - View app policy and guidelines.
 * Description: Displays privacy policy and usage guidelines.
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val defaultPolicy = """
    1. Information We Collect
    Aurora Campsites uses a device-based identifier to associate your profile with your activity. Profile information you provide may include your name, email address and phone number. Lottery entries and reservation records are stored in Firebase Firestore.

    2. How Information Is Used
    Information supports profile management, campsite registration, lottery participation and reservation management. Avoid entering sensitive or unnecessary personal information in public campsite descriptions or reviews.

    3. Profile and Notification Settings
    You can update your profile information and notification preference in Profile Settings. Deleting your profile removes the profile document but does not automatically remove associated reservation or campsite records.

    4. Data Storage
    The app uses Firebase Firestore to store application data. A device identifier is not a password or a verified account identity. This app is a course project and should not be used to store sensitive personal information.

    5. App Guidelines
    Provide accurate information when registering campsites or making reservations. Respect other users, campsite rules and cancellation policies. Do not misuse lottery entries, reservations or other users' information.

    6. Reviews and Reservations
    Reviews should reflect genuine experiences and use respectful language. Check reservation details and applicable campsite rules before confirming a booking.
""".trimIndent()

@Composable
fun AppPolicyScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    effectiveDate: String = "October 20 2026",
    policyText: String = "",
    onBack: () -> Unit = { navController.popBackStack() }
) {
    Surface(modifier = modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("AURORA CAMPSITES", fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart).height(36.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(2.dp, Color.Black),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
                ) { Text("Back", color = Color.Black, fontWeight = FontWeight.Bold) }
                Text("App Policy and\nGuidelines", fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center))
            }
            Spacer(Modifier.height(22.dp))
            HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 20.dp)) {
                Text("Effective as of: $effectiveDate", color = Color.DarkGray,
                    fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                Text("Please review the privacy policy and guidelines for using Aurora Campsites.",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, lineHeight = 19.sp)
                Spacer(Modifier.height(24.dp))
                Text("Privacy Policy", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
                Spacer(Modifier.height(24.dp))
                Text(policyText.ifBlank { defaultPolicy }, fontSize = 14.sp, lineHeight = 20.sp, color = Color.Black)
            }
        }
    }
}
