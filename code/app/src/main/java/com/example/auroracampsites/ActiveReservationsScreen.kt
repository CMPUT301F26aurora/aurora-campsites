
package com.example.auroracampsites

/*
 * User Stories:
 * US 01.06.01 - View current reservations.
 *US 01.07.01 - Cancel an existing reservation.
 *
 * Displays current and past reservations with
 * campsite details and cancellation options.
 *
 * (Sample data for UI testing; Firebase pending)
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
    modifier: Modifier = Modifier,
    currentReservations: List<ReservationData> = emptyList(),
    pastReservations: List<ReservationData> = emptyList(),
    onConfirmCancellation: (ReservationData) -> Unit = {}
) {
    // TODO(FIREBASE): Load current and past reservations
    // for the logged-in user from Firestore.
    // Pass them into this screen instead of sample data.

    var selectedReservation by remember {
        mutableStateOf<ReservationData?>(null)
    }

    // UI demo: locally hide reservations after cancellation.
    // TODO(FIREBASE): Remove this local-only cancellation logic
    // after connecting the team's reservation repository.
    var locallyCancelledIds by remember {
        mutableStateOf(setOf<String>())
    }

    val visibleCurrentReservations = currentReservations.filterNot {
        it.id in locallyCancelledIds
    }

    if (selectedReservation == null) {
        ActiveReservationsScreen(
            modifier = modifier,
            currentReservations = visibleCurrentReservations,
            pastReservations = pastReservations,
            onCancelReservation = { reservation ->
                selectedReservation = reservation
            }
        )
    } else {
        val reservation = selectedReservation!!

        CancelReservationScreen(
            modifier = modifier,
            onConfirmCancel = {
                // TODO(FIREBASE): Cancel reservation in Firestore.
                // Wait for database success before updating the UI.
                // Do not use local removal as real cancellation.

                locallyCancelledIds =
                    locallyCancelledIds + reservation.id

                onConfirmCancellation(reservation)
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
    onCancelReservation: (ReservationData) -> Unit = {}
) {
    var showPast by remember { mutableStateOf(false) }

    val reservations =
        if (showPast) pastReservations else currentReservations

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AURORA CAMPSITES",
                fontSize = 24.sp
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp)
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
                        text = if (showPast)
                            "No past reservations."
                        else
                            "No current reservations.",
                        color = Color.Gray,
                        modifier = Modifier.align(
                            Alignment.CenterHorizontally
                        )
                    )
                } else {
                    reservations.forEachIndexed { index, reservation ->
                        ReservationCard(
                            index = index + 1,
                            reservation = reservation,
                            showCancel = !showPast,
                            onCancel = {
                                onCancelReservation(reservation)
                            }
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
    onCancel: () -> Unit
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

            if (showCancel) {
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
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