package com.example.auroracampsites

/*
- User Stories:
- US 01.06.01 - View current reservations.
- US 01.07.01 - Cancel an existing reservation.
- Displays current and past reservations with
- campsite details, cancellation, and review options.
 */

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person

/**
 * Manages the display and lifecycle of user reservations, switching
 * between active bookings and past stays, handling cancellations, and provides
 * input for users to leave review.
 *
 * @property id  unique id for the reservation.
 * @property campsiteName The name of the reserved campsite.
 * @property location The location of the campsite.
 * @property maximumOccupancy The maximum guest capacity for the reservation.
 * @property rating The average review ma of the campsite.
 * @property startDate The beginning date of the reserved period.
 * @property endDate The end date of the reserved period.
 */

data class ReservationData(
    val id: String,
    val campsiteName: String,
    val location: String,
    val maximumOccupancy: Int,
    val rating: Double,
    val startDate: String,
    val endDate: String
)

@Composable
fun ReservationManagementScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var currentReservations by remember {
        mutableStateOf(listOf(
            ReservationData(
                id = "1",
                campsiteName = "Aurora Ridge Site A",
                location = "Banff National Park",
                maximumOccupancy = 4,
                rating = 4.9,
                startDate = "2026-06-01",
                endDate = "2026-06-05"
            )
        ))
    }

    var pastReservations by remember {
        mutableStateOf(emptyList<ReservationData>())
    }

    var selectedReservation by remember { mutableStateOf<ReservationData?>(null) }

    if (selectedReservation == null) {
        ActiveReservationsScreen(
            modifier = modifier,
            currentReservations = currentReservations,
            pastReservations = pastReservations,
            onCancelReservation = { reservation ->
                selectedReservation = reservation
            },
            onProfileClick = {
                navController.navigate("profile_settings")
            },
            onReviewClick = { reservation ->
                navController.navigate("leave_review")
            }
        )
    } else {
        val reservation = selectedReservation!!

        CancelReservationScreen(
            modifier = modifier,
            onConfirmCancel = {
                currentReservations = currentReservations.filter { it.id != reservation.id }
                pastReservations = pastReservations + reservation
                selectedReservation = null
            },
            onGoBack = {
                selectedReservation = null
            }
        )
    }
}

@Composable
fun ActiveReservationsScreen(
    modifier: Modifier = Modifier,
    currentReservations: List<ReservationData> = emptyList(),
    pastReservations: List<ReservationData> = emptyList(),
    onCancelReservation: (ReservationData) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onReviewClick: (ReservationData) -> Unit = {}
) {
    var showPast by remember { mutableStateOf(false) }
    val reservations = if (showPast) pastReservations else currentReservations

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AURORA CAMPSITES",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Settings",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                ReservationTab(
                    title = "Current Reservations",
                    selected = !showPast,
                    modifier = Modifier.weight(1f),
                    onClick = { showPast = false }
                )
                ReservationTab(
                    title = "Past Reservations",
                    selected = showPast,
                    modifier = Modifier.weight(1f),
                    onClick = { showPast = true }
                )
            }

            Spacer(Modifier.height(22.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (reservations.isEmpty()) {
                    Spacer(Modifier.height(40.dp))
                    Text(
                        text = if (showPast) "No past reservations." else "No current reservations.",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    reservations.forEachIndexed { index, reservation ->
                        ReservationCard(
                            index = index + 1,
                            reservation = reservation,
                            showCancel = !showPast,
                            onCancel = { onCancelReservation(reservation) },
                            onReview = { onReviewClick(reservation) }
                        )
                        Spacer(Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationTab(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(3.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD9D9D9),
            contentColor = if (selected)
                Color(0xFF222222)
            else
                Color(0xFF999999)
        )
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReservationCard(
    index: Int,
    reservation: ReservationData,
    showCancel: Boolean,
    onCancel: () -> Unit,
    onReview: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Gray),
        color = Color(0xFFF8F8F8)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("$index.", fontSize = 15.sp)

            Spacer(Modifier.height(6.dp))

            Text(
                "Campsite Name : ${reservation.campsiteName}",
                fontSize = 14.sp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Location : ${reservation.location}",
                fontSize = 14.sp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Maximum Occupancy : ${reservation.maximumOccupancy} guests",
                fontSize = 14.sp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Ratings: ${reservation.rating} ★",
                fontSize = 14.sp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Booked dates : ${reservation.startDate} to ${reservation.endDate}",
                fontSize = 14.sp
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (showCancel) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(2.dp, Color.Black),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Text(
                            text = "Cancel Reservation",
                            fontSize = 12.sp
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onReview,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color.Black),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Leave a review",
                            fontSize = 11.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CancelReservationScreen(
    modifier: Modifier = Modifier,
    onConfirmCancel: () -> Unit = {},
    onGoBack: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AURORA CAMPSITES",
                fontSize = 24.sp
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(Modifier.height(65.dp))

            Text(
                text = "Do you wish to cancel\nyour reservation?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(65.dp))

            Button(
                onClick = onConfirmCancel,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(70.dp),
                shape = RoundedCornerShape(5.dp),
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE6E6E9),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Yes, Cancel\nReservation",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onGoBack,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(70.dp),
                shape = RoundedCornerShape(5.dp),
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE6E6E9),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "No, Go Back",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}