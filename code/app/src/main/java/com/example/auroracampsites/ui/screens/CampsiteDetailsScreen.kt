package com.example.auroracampsites.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.auroracampsites.data.models.Campsite
import com.example.auroracampsites.data.models.Reservation
import com.example.auroracampsites.data.repository.ReservationRepo
import com.example.auroracampsites.data.repository.UserRepository
import com.example.auroracampsites.data.repository.WaitListRepository
import com.example.auroracampsites.data.session.UserSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

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
 */
@Composable
fun CampsiteDetailsScreen(
    navController: NavController,
    campsite: Campsite = Campsite(),
    onBackClick: () -> Unit = { navController.popBackStack() }
) {
    val coroutineScope = rememberCoroutineScope()
    val reservationRepository = remember { ReservationRepo() }
    val context = LocalContext.current
    val currentUserId = remember(context) { UserSession.getCurrentUserId(context) }
    val userRepository = remember { UserRepository() }
    val waitListRepository = remember { WaitListRepository() }

    var activeLottery by remember(campsite.id) { mutableStateOf<com.example.auroracampsites.data.models.Lottery?>(null) }
    val registrationOpen = activeLottery?.let { waitListRepository.registrationOpen(it) } == true
    var waitListStatus by remember(campsite.id, currentUserId) { mutableStateOf<String?>(null) }
    var isWaitListLoading by remember(campsite.id) { mutableStateOf(true) }
    var isWaitListSubmitting by remember(campsite.id) { mutableStateOf(false) }
    var waitListError by remember(campsite.id) { mutableStateOf("") }
    var statusLoaded by remember(campsite.id) { mutableStateOf(false) }
    var refreshCount by remember(campsite.id) { mutableStateOf(0) }
    val hasJoinedLottery = waitListStatus == "Waiting"
    val hasWonLottery = statusLoaded && waitListStatus == "Won"
    val hasLostLottery = statusLoaded && waitListStatus == "Lost"
    var showLeaveConfirmation by remember(campsite.id) { mutableStateOf(false) }
    var showReservationDialog by remember(campsite.id) { mutableStateOf(false) }
    var selectedBookingDate by remember(campsite.id) {
        mutableStateOf(campsite.availableDates.firstOrNull { it !in campsite.bookedDates } ?: "")
    }
    var isBookingConfirmed by remember(campsite.id) { mutableStateOf(false) }
    var isBookingSubmitting by remember(campsite.id) { mutableStateOf(false) }
    var bookingError by remember(campsite.id) { mutableStateOf("") }

    LaunchedEffect(campsite.id, currentUserId, refreshCount) {
        isWaitListLoading = true
        statusLoaded = false
        waitListError = ""
        try {
            require(campsite.id.isNotBlank())
            activeLottery = waitListRepository.getActiveLottery(campsite.id)
            waitListStatus = activeLottery?.let { waitListRepository.getWaitListEntry(campsite.id, currentUserId, it.id)?.status }
            statusLoaded = true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            waitListError = if (campsite.id.isBlank()) "Invalid campsite." else "Unable to load waitlist status."
        } finally {
            isWaitListLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

        // Campsite Name
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
            text = "Max Occupancy: ${campsite.maxOccupancy}          •Available: ${campsite.availableDates}",
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
        if (activeLottery != null && registrationOpen) {
            Button(
                onClick = {
                    if (hasJoinedLottery) {
                        showLeaveConfirmation = true
                    } else if (statusLoaded && !isWaitListSubmitting) {
                        isWaitListSubmitting = true
                        waitListError = ""
                        coroutineScope.launch {
                            try {
                                userRepository.getOrCreateUser(currentUserId)
                                waitListRepository.joinWaitList(campsite.id, currentUserId, requireNotNull(activeLottery).id)
                                waitListStatus = waitListRepository.getWaitListEntry(campsite.id, currentUserId, requireNotNull(activeLottery).id)?.status
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                statusLoaded = false
                                waitListError = "Unable to join waitlist. Refresh your status."
                            } finally {
                                isWaitListSubmitting = false
                            }
                        }
                    }
                },
                enabled = statusLoaded && !isWaitListLoading && !isWaitListSubmitting &&
                        (waitListStatus == null || hasJoinedLottery),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(if (hasJoinedLottery) "leaveWaitlistButton" else "joinWaitlistButton")
            ) {
                Text(when {
                    isWaitListLoading -> "Loading..."
                    isWaitListSubmitting -> "Please wait..."
                    hasJoinedLottery -> "Leave Waitlist"
                    else -> "Join Waitlist"
                })
            }

            if (hasJoinedLottery) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ You have successfully joined the waitlist for ${campsite.name}!",
                    fontSize = 14.sp,
                    modifier = Modifier.testTag("waitlistConfirmationText")
                )
            }
        } else if (activeLottery != null) {
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("waitlistFullButton")
            ) {
                Text("Registration Closed")
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

        if (waitListError.isNotEmpty()) {
            Text(waitListError, color = MaterialTheme.colorScheme.error)
        }
        if (campsite.id.isNotBlank()) {
            Button(
                onClick = { refreshCount++ },
                enabled = !isWaitListLoading && !isWaitListSubmitting && !isBookingSubmitting
            ) { Text("Refresh lottery status") }
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
                        enabled = !isBookingConfirmed && !isWaitListLoading && !isWaitListSubmitting,
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

    } // END OF MAIN COLUMN


    // Leave Waitlist Confirmation Dialog
    if (showLeaveConfirmation) {
        AlertDialog(
            onDismissRequest = { if (!isWaitListSubmitting) showLeaveConfirmation = false },
            title = { Text("Leave Waitlist") },
            text = { Text("Are you sure you want to leave the waitlist for ${campsite.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isWaitListSubmitting) {
                            isWaitListSubmitting = true
                            waitListError = ""
                            coroutineScope.launch {
                                try {
                                    waitListRepository.leaveWaitList(campsite.id, currentUserId, requireNotNull(activeLottery).id)
                                    waitListStatus = null
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    statusLoaded = false
                                    waitListError = "Unable to leave waitlist. Refresh your status."
                                } finally {
                                    isWaitListSubmitting = false
                                    showLeaveConfirmation = false
                                }
                            }
                        }
                    },
                    enabled = !isWaitListSubmitting,
                    modifier = Modifier.testTag("confirmLeaveButton")
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLeaveConfirmation = false },
                    enabled = !isWaitListSubmitting,
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
            onDismissRequest = { if (!isBookingSubmitting) showReservationDialog = false },
            title = { Text("Select Reservation Date") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Selected Campsite: ${campsite.name}")
                    Text("Price: $${campsite.price} / night")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = selectedBookingDate,
                        enabled = !isBookingSubmitting,
                        onValueChange = { newValue -> selectedBookingDate = newValue },
                        label = { Text("Reservation Date (YYYY-MM-DD)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reservationDateInput")
                    )
                    if (bookingError.isNotEmpty()) {
                        Text(bookingError, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!hasWonLottery || isWaitListLoading) {
                            bookingError = "You must win the lottery before booking."
                        } else if (campsite.id.isBlank() ||
                            selectedBookingDate !in campsite.availableDates ||
                            selectedBookingDate in campsite.bookedDates
                        ) {
                            bookingError = "Please select a valid available date."
                        } else if (!isBookingSubmitting && !isBookingConfirmed) {
                            isBookingSubmitting = true
                            bookingError = ""
                            val date = selectedBookingDate
                            coroutineScope.launch {
                                try {
                                    userRepository.getOrCreateUser(currentUserId)
                                    val entry = waitListRepository.getWaitListEntry(campsite.id, currentUserId, requireNotNull(activeLottery).id)
                                    if (entry?.status != "Won") {
                                        waitListStatus = entry?.status
                                        bookingError = "You must win the lottery before booking."
                                    } else {
                                        val newReservation = Reservation(
                                            userId = currentUserId,
                                            campsiteId = campsite.id,
                                            campsiteName = campsite.name,
                                            location = campsite.location,
                                            maximumOccupancy = campsite.maxOccupancy,
                                            price = campsite.price,
                                            startDate = date,
                                            endDate = date,
                                            status = "ACTIVE",
                                            lotteryId = requireNotNull(activeLottery).id
                                        )
                                        val reservationId = reservationRepository.saveReservation(newReservation)
                                        if (reservationId.isNotBlank()) {
                                            isBookingConfirmed = true
                                            showReservationDialog = false
                                            navController.navigate("booking_confirmation/${android.net.Uri.encode(reservationId)}")
                                        } else {
                                            bookingError = "Booking failed. Please try again."
                                        }
                                    }
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    bookingError = "Booking failed. Please try again."
                                    Log.e("CampsiteDetailsScreen", "Error adding reservation", e)
                                } finally {
                                    isBookingSubmitting = false
                                }
                            }
                        }
                    },
                    enabled = !isBookingSubmitting && !isBookingConfirmed,
                    modifier = Modifier.testTag("confirmBookingButton")
                ) {
                    Text(if (isBookingSubmitting) "Booking..." else "Confirm Booking")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showReservationDialog = false },
                    enabled = !isBookingSubmitting,
                    modifier = Modifier.testTag("cancelBookingButton")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
