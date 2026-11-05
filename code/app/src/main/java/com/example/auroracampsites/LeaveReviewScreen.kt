
package com.example.auroracampsites

/*
 * User Story:
 * US 01.13.01 - Leave a campsite review.
 *
 * Description:
 * Allows users to rate a campsite from 1 to 5 stars
 * and optionally write a review.
 *
 * (UI only: Firebase integration pending)
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CampsiteReviewData(
    val campsiteId: String,
    val rating: Int,
    val comment: String
)

@Composable
fun LeaveReviewScreen(
    modifier: Modifier = Modifier,
    campsiteId: String = "",
    campsiteName: String = "",
    onBack: () -> Unit = {},
    onPostReview: (CampsiteReviewData) -> Unit = {}
) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AURORA CAMPSITES",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color.LightGray
            )

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.Start)
                    .height(34.dp),
                shape = RoundedCornerShape(50),
                border = BorderStroke(2.dp, Color.Black),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Text(
                    text = "Back",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFD9D9D9),
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(
                    text = "Leave a Review for:\n$campsiteName",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 25.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (star in 1..5) {
                    Text(
                        text = if (star <= selectedRating) "★" else "☆",
                        fontSize = 46.sp,
                        color = Color(0xFF222222),
                        modifier = Modifier.clickable {
                            selectedRating = star
                            showError = false
                        }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                placeholder = {
                    Text(
                        "Enter text (optional)...",
                        fontSize = 13.sp
                    )
                },
                shape = RoundedCornerShape(5.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF7F7F7),
                    unfocusedContainerColor = Color(0xFFF7F7F7),
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray
                )
            )

            if (showError) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Please select a rating before posting.",
                    color = Color.Red,
                    fontSize = 13.sp
                )
            }

            if (submitted) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Review submitted for processing.",
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(18.dp))

            OutlinedButton(
                onClick = {
                    if (selectedRating == 0) {
                        showError = true
                    } else {
                        showError = false

                        val review = CampsiteReviewData(
                            campsiteId = campsiteId,
                            rating = selectedRating,
                            comment = reviewText.trim()
                        )

                        // TODO(FIREBASE): Save the review to
                        // Firestore with the authenticated user ID.
                        // Verify campsite and review eligibility.
                        // Wait for success before showing confirmation.
                        onPostReview(review)

                        submitted = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .width(150.dp)
                    .height(42.dp),
                shape = RoundedCornerShape(5.dp),
                border = BorderStroke(1.5.dp, Color.Black),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFFF5F5F5),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Post Review",
                    fontSize = 12.sp
                )
            }
        }
    }
}
