package com.example.auroracampsites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Role/Pattern: Composable UI Screen (View) for displaying detailed campsite information.
 *
 * Purpose:
 * - Displays full campsite details, amenities, pricing, and availability.
 * - Manages waitlist sign-ups and lottery win/loss result notifications.
 *
 * Navigation & Connections:
 * - Connected from CampsiteSearchScreen when a user selects a campsite card.
 * - Uses the "Back to Search" action to return to the active search list.
 *
 * Issues:
 * - Needs ViewModel and Firestore integration to save user waitlist and reservation entries.
 */
@Composable
fun CampsiteDetailScreen(
    campsite: Campsite,
    onBackClick: () -> Unit
) {
    // Key all state variables to campsite.id so they reset when opening a different campsite
    var hasJoinedLottery by remember(campsite.id) { mutableStateOf(false) }
    var showLeaveConfirmation by remember { mutableStateOf(false) }

    // US 01.03.01 - Win Notification & Reservation States
    var hasWonLottery by remember(campsite.id) { mutableStateOf(campsite.isWinner) }
    var showReservationDialog by remember { mutableStateOf(false) }
    var selectedBookingDate by remember(campsite.id) { mutableStateOf(campsite.availableDate) }
    var isBookingConfirmed by remember(campsite.id) { mutableStateOf(false) }
    var hasLostLottery by remember(campsite.id) { mutableStateOf(campsite.isLoser) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Back button to return to search list
        Button(
            onClick = onBackClick,
            modifier = Modifier.testTag("detailBackButton")
        ) {
            Text("Back to Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        //Campsite Name
        Text(
            text = campsite.name,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Location: ${campsite.location}",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Rating: ${campsite.rating} / 5.0          •$${campsite.price} / night",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Max Occupancy: ${campsite.maxOccupancy}          •Available: ${campsite.availableDate}",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "About this Campsite",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = campsite.description,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Amenities",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        campsite.amenities.forEach { amenity ->
            Text(
                text = "• $amenity",
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Waitlist / Lottery Action Section
        if (campsite.hasActiveLottery && (!campsite.isWaitlistFull || hasJoinedLottery)) {
            Button(
                onClick = {
                    if (hasJoinedLottery) {
                        showLeaveConfirmation = true
                    } else {
                        hasJoinedLottery = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(if (hasJoinedLottery) "leaveWaitlistButton" else "joinWaitlistButton")
            ) {
                Text(if (hasJoinedLottery) "Leave Waitlist" else "Join Waitlist")
            }

            if (hasJoinedLottery) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ You have successfully joined the waitlist for ${campsite.name}!",
                    fontSize = 14.sp,
                    modifier = Modifier.testTag("waitlistConfirmationText")
                )
            }
        } else if (campsite.hasActiveLottery) {
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("waitlistFullButton")
            ) {
                Text("Waitlist Full")
            }
        } else {
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("noLotteryButton")
            ) {
                Text("No Active Lottery")
            }
        }

        // US 01.03.01 & US 01.03.02: Lottery Result Notifications ---
        if (hasWonLottery) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("winNotificationCard")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Congratulations! You won the lottery!",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You have been selected for ${campsite.name}. Please select your reservation date to confirm.",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showReservationDialog = true },
                        enabled = !isBookingConfirmed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("claimReservationButton")
                    ) {
                        Text(if (isBookingConfirmed) "Reservation Confirmed!" else "Select & Confirm Booking Date")
                    }
                }
            }
        } else if (hasLostLottery) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loseNotificationCard")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Not Selected for ${campsite.name}",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Thank you for participating in the lottery. Unfortunately, you were not selected for a reservation in this draw.",
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Simulation control buttons for testing win / lose
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    hasWonLottery = !hasWonLottery
                    if (hasWonLottery) hasLostLottery = false // Reset lose state when winning
                },
                modifier = Modifier.testTag("WinButton")
            ) {
                Text(if (hasWonLottery) "Reset Win" else " You Win")
            }

            Button(
                onClick = {
                    hasLostLottery = !hasLostLottery
                    if (hasLostLottery) hasWonLottery = false // Reset win state when losing
                },
                modifier = Modifier.testTag("LoseButton")
            ) {
                Text(if (hasLostLottery) "Reset Lose" else "You Lose")
            }
        }


        // Leave Waitlist Confirmation Dialog
        if (showLeaveConfirmation) {
            AlertDialog(
                onDismissRequest = { showLeaveConfirmation = false },
                title = { Text("Leave Waitlist") },
                text = { Text("Are you sure you want to leave the waitlist for ${campsite.name}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            hasJoinedLottery = false
                            showLeaveConfirmation = false
                        },
                        modifier = Modifier.testTag("confirmLeaveButton")
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showLeaveConfirmation = false },
                        modifier = Modifier.testTag("cancelLeaveButton")
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        // US 01.03.01 - Reservation Confirmation Dialog
        if (showReservationDialog) {
            AlertDialog(
                onDismissRequest = { showReservationDialog = false },
                title = { Text("Select Reservation Date") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Selected Campsite: ${campsite.name}")
                        Text("Price: $${campsite.price} / night")
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = selectedBookingDate,
                            onValueChange = { selectedBookingDate = it },
                            label = { Text("Reservation Date (YYYY-MM-DD)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reservationDateInput")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isBookingConfirmed = true
                            showReservationDialog = false
                        },
                        modifier = Modifier.testTag("confirmBookingButton")
                    ) {
                        Text("Confirm Booking")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showReservationDialog = false },
                        modifier = Modifier.testTag("cancelBookingButton")
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

    }
}
