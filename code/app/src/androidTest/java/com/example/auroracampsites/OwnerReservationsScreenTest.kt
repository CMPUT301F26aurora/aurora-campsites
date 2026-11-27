package com.example.auroracampsites

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.auroracampsites.data.models.Campsite
import com.example.auroracampsites.data.models.Reservation
import com.example.auroracampsites.ui.screens.OwnerCampsiteDates
import com.example.auroracampsites.ui.screens.OwnerReservationsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/** Verifies owner reservation states and date labels without live Firestore writes. */
@RunWith(AndroidJUnit4::class)
class OwnerReservationsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun switchesBetweenActivePastAndCancelledReservations() {
        composeRule.setContent {
            OwnerReservationsScreen(
                campsiteId = "site-1",
                ownerId = "owner-1",
                loadReservations = { campsiteId, ownerId ->
                    assertEquals("site-1", campsiteId)
                    assertEquals("owner-1", ownerId)
                    listOf(
                        Reservation(id = "active", endDate = "2026-11-26"),
                        Reservation(id = "past", endDate = "2026-11-25"),
                        Reservation(id = "cancelled", endDate = "2026-11-25", status = "CANCELLED")
                    )
                },
                onBack = {},
                today = LocalDate.parse("2026-11-26")
            )
        }

        composeRule.onNodeWithText("Reservation: active").assertExists()
        composeRule.onNodeWithText("Past").performClick()
        composeRule.onNodeWithText("Reservation: past").assertExists()
        composeRule.onNodeWithText("Reservation: cancelled").assertDoesNotExist()
        composeRule.onNodeWithText("Cancelled").performClick()
        composeRule.onNodeWithText("Reservation: cancelled").assertExists()
        composeRule.onNodeWithText("Reservation: past").assertDoesNotExist()
    }

    @Test
    fun failureCanBeRetriedAndEmptyResultsHaveTheirOwnMessage() {
        var attempts = 0
        composeRule.setContent {
            OwnerReservationsScreen(
                campsiteId = "site-1",
                ownerId = "owner-1",
                loadReservations = { _, _ ->
                    if (attempts++ == 0) error("Permission denied")
                    emptyList()
                },
                onBack = {}
            )
        }

        composeRule.onNodeWithText("Unable to load reservations.").assertExists()
        composeRule.onNodeWithText("No active reservations.").assertDoesNotExist()
        composeRule.onNodeWithText("Retry").performClick()
        composeRule.onNodeWithText("No active reservations.").assertExists()
        composeRule.onNodeWithText("Past").performClick()
        composeRule.onNodeWithText("No past reservations.").assertExists()
        composeRule.onNodeWithText("Cancelled").performClick()
        composeRule.onNodeWithText("No cancelled reservations.").assertExists()
    }

    @Test
    fun bookedDateIsNotShownAsAvailable() {
        composeRule.setContent {
            Column {
                OwnerCampsiteDates(
                    Campsite(
                        availableDates = listOf("2026-11-26", "2026-11-27"),
                        bookedDates = listOf("2026-11-26")
                    )
                )
            }
        }

        composeRule.onNode(hasText("Booked Dates") and hasText("2026-11-26")).assertExists()
        composeRule.onNode(hasText("Available Dates") and hasText("2026-11-27")).assertExists()
        composeRule.onNode(hasText("Available Dates") and hasText("2026-11-26")).assertDoesNotExist()
    }
}
