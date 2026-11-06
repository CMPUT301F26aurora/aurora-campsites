package com.example.auroracampsites.ui.screens

/*
- User Story:
- US 01.11.01 - View app policy and guidelines.
 *
- Description:
- Displays the app's privacy policy and guidelines
- with a scrollable page and Back button.
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

@Composable
fun AppPolicyScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    effectiveDate: String = "October 20 2026",
    policyText: String = "",
    onBack: () -> Unit = { navController.popBackStack() } // <--- Pops back to Profile Settings
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "AURORA CAMPSITES",
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color.LightGray
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .height(36.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(2.dp, Color.Black),
                    contentPadding = PaddingValues(
                        horizontal = 24.dp,
                        vertical = 0.dp
                    )
                ) {
                    Text(
                        text = "Back",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "App Policy and\nGuidelines",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(22.dp))

            HorizontalDivider(
                color = Color.DarkGray,
                thickness = 1.dp
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 20.dp)
            ) {
                Text(
                    text = "Effective as of: $effectiveDate",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Our app policy and app guidelines dictate " +
                            "the fair and expected usage of the Aurora " +
                            "Campsites app, Privacy Policy and App Guidelines. " +
                            "Please take a moment to review the terms of use below.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 19.sp
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Privacy Policy",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(Modifier.height(20.dp))

                HorizontalDivider(
                    color = Color.DarkGray,
                    thickness = 1.dp
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = policyText.ifBlank {
                        "Lorem ipsum dolor sit amet, consectetur " +
                                "adipiscing elit. Pellentesque eget lectus nisl. " +
                                "In metus mauris, aliquam non feugiat vel, " +
                                "tincidunt eu arcu. Suspendisse volutpat, " +
                                "turpis in tristique eleifend, leo nibh " +
                                "aliquet lacus, nec blandit mauris orci " +
                                "pellentesque velit. Sed a orci rutrum " +
                                "arcu feugiat pretium. Donec sagittis " +
                                "elit vitae justo pellentesque, imperdiet " +
                                "commodo diam commodo. Curabitur eleifend " +
                                "sed lorem nec bibendum. Fusce efficitur erat..."
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                )
            }
        }
    }
}