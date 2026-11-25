package com.example.auroracampsites.ui.screens

/*
 * User Story:
 * US 01.13.01 - Leave a campsite review.
 *
 * Description:
 * Allows users to rate a campsite from 1 to 5 stars
 * and optionally write a review.
 *
 * Firebase review submission uses the reservation ID.
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
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.example.auroracampsites.data.session.UserSession
import com.example.auroracampsites.data.repository.ReservationRepo
import com.example.auroracampsites.data.repository.RatingRepository
import com.example.auroracampsites.data.models.Reservation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Represents a campsite review submitted by a user, containing the target
 * campsite identifier, star rating, and optional textual feedback.
 *
 * @property campsiteId The unique id of the past campsite.
 * @property rating The star rating assigned by the user (1-5 stars).
 * @property comment Optional text comments provided by the user.
 */
data class CampsiteReviewData(
    val campsiteId: String,
    val rating: Int,
    val comment: String
)

@Composable
fun LeaveReviewScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    campsiteId: String = "",
    campsiteName: String = "",
    onBack: () -> Unit = { navController.popBackStack() }, // <--- Pops back to past reservations
    onPostReview: (suspend (CampsiteReviewData) -> Unit)? = null
) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

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
                        modifier = Modifier.clickable(enabled = !busy && !submitted) {
                            selectedRating = star
                            showError = false
                        }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = reviewText,
                enabled = !busy && !submitted,
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

            if (busy) CircularProgressIndicator()
            if (saveError.isNotBlank()) Text(saveError, color = Color.Red)
            if (submitted) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Review saved successfully.",
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(18.dp))

            OutlinedButton(
                enabled = !busy && !submitted && onPostReview != null,
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

                        if (!busy && !submitted && onPostReview != null) {
                            busy = true
                            saveError = ""
                            scope.launch {
                                try {
                                    onPostReview(review)
                                    submitted = true
                                } catch (e: CancellationException) { throw e }
                                catch (e: Exception) { saveError = "Unable to save review. It may already exist." }
                                finally { busy = false }
                            }
                        }
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

@Composable
fun ReservationReviewScreen(navController: NavController, reservationId: String) {
    val context = LocalContext.current
    val userId = remember(context) { UserSession.getCurrentUserId(context) }
    val reservations = remember { ReservationRepo() }
    val ratings = remember { RatingRepository() }
    var reservation by remember(reservationId) { mutableStateOf<Reservation?>(null) }
    var loading by remember(reservationId) { mutableStateOf(true) }
    var error by remember(reservationId) { mutableStateOf("") }
    var retry by remember { mutableIntStateOf(0) }
    LaunchedEffect(reservationId, userId, retry) {
        loading = true
        error = ""
        reservation = null
        try {
            val record = reservations.getReservationById(reservationId, userId)
            if (record == null || record.status != "ACTIVE" ||
                !LocalDate.parse(record.endDate).isBefore(LocalDate.now()) || record.campsiteId.isBlank()) {
                error = "This reservation is not eligible for a review."
            } else if (ratings.hasReview(reservationId)) {
                error = "You have already reviewed this reservation."
            } else reservation = record
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { error = "Unable to load review details." }
        finally { loading = false }
    }
    val record = reservation
    if (!loading && record != null) {
        LeaveReviewScreen(navController = navController, campsiteId = record.campsiteId,
            campsiteName = record.campsiteName,
            onPostReview = { ratings.postReview(reservationId, userId, it.rating, it.comment) })
    } else {
        Column(Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            if (loading) CircularProgressIndicator() else {
                Text(error)
                Button(onClick = { retry++ }) { Text("Retry") }
            }
            Button(onClick = { navController.popBackStack() }) { Text("Back") }
        }
    }
}
