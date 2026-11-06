package com.example.auroracampsites

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.navigation.compose.rememberNavController
import com.example.auroracampsites.ui.screens.*
import com.example.auroracampsites.data.models.*

@RunWith(AndroidJUnit4::class)
class AuroraScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    // US 01.11.01 - App Policy
    @Test
    fun appPolicy_displaysPolicyContent() {
        composeRule.setContent {
            val navController = rememberNavController()
            AppPolicyScreen(
                navController = navController,
                policyText = "Automated test policy content"
            )
        }

        composeRule.onNodeWithText(
            "Automated test policy content"
        ).assertExists()
    }

    // US 01.05.01 / US 01.05.02 - Booking Confirmation
    @Test
    fun bookingConfirmation_buttonsWork() {
        var notificationClicked = false
        var csvClicked = false

        composeRule.setContent {
            val navController = rememberNavController()
            BookingConfirmationScreen(
                navController = navController,
                onSendNotification = {
                    notificationClicked = true
                },
                onDownloadCsv = {
                    csvClicked = true
                }
            )
        }

        // Test notification button
        composeRule.onNodeWithText("Send a Notification")
            .performClick()

        // Test CSV download button
        composeRule.onNodeWithText("Download as CSV")
            .performClick()

        // Test reservations button (navigates via navController)
        composeRule.onNodeWithText("View Reservations")
            .performClick()

        // Verify callbacks were triggered
        composeRule.runOnIdle {
            assertTrue(notificationClicked)
            assertTrue(csvClicked)
        }
    }

    // US 01.13.01 - Leave a campsite review
    @Test
    fun leaveReview_submitReviewWorks() {
        var submittedReview: CampsiteReviewData? = null

        composeRule.setContent {
            val navController = rememberNavController()
            LeaveReviewScreen(
                navController = navController,
                campsiteId = "test-campsite-001",
                campsiteName = "Pine Creek Campground",
                onPostReview = { review ->
                    submittedReview = review
                }
            )
        }

        // Check campsite name
        composeRule.onNodeWithText(
            "Leave a Review for:\nPine Creek Campground"
        ).assertExists()

        // Initially all five stars are empty
        composeRule.onAllNodesWithText("☆")
            .assertCountEquals(5)

        // Select four stars
        composeRule.onAllNodesWithText("☆")[3]
            .performClick()

        // Enter a review comment
        composeRule.onNodeWithText(
            "Enter text (optional)..."
        ).performTextInput("Great campsite!")

        // Submit review
        composeRule.onNodeWithText("Post Review")
            .performClick()

        // Verify submitted review data
        composeRule.runOnIdle {
            assertEquals(
                "test-campsite-001",
                submittedReview?.campsiteId
            )
            assertEquals(4, submittedReview?.rating)
            assertEquals(
                "Great campsite!",
                submittedReview?.comment
            )
        }

        // Check confirmation message
        composeRule.onNodeWithText(
            "Review submitted for processing."
        ).assertExists()
    }

    // US 02.01.04 - View campsite details
    @Test
    fun campsiteDetails_displaysDataAndButtonsWork() {
        var backClicked = false

        composeRule.setContent {
            val navController = rememberNavController()
            CampsiteDetailsScreen(
                navController = navController,
                campsite = Campsite(
                    name = "Test Campground",
                    location = "Edmonton, Alberta",
                    description = "WiFi, showers",
                    maxOccupancy = 6,
                    price = 45.0
                ),
                onBackClick = {
                    backClicked = true
                }
            )
        }

        // Verify campsite information is rendered
        composeRule.onNodeWithText("Test Campground")
            .assertExists()

        composeRule.onNodeWithText("Location: Edmonton, Alberta")
            .assertExists()

        composeRule.onNodeWithText("WiFi, showers")
            .assertExists()

        // Test Back button action
        composeRule.onNodeWithText("Back to Search")
            .performClick()

        // Verify button callback was triggered
        composeRule.runOnIdle {
            assertTrue(backClicked)
        }
    }

    // US 02.01.01 / US 02.01.02 - Register campsite
    // US 02.01.01 / US 02.01.02 - Register campsite
    @Test
    fun registerCampsite_validFormWorks() {
        composeRule.setContent {
            val navController = rememberNavController()
            RegisterCampsiteScreen(
                navController = navController
            )
        }

        // Find editable text fields in form order:
        // [0] Campsite Name, [1] Location, [2] Description, [3] Max Occupancy, [4] Price, etc.
        val fields = composeRule.onAllNodes(
            hasSetTextAction()
        )

        if (fields.fetchSemanticsNodes().size >= 5) {
            fields[0].performTextReplacement("Test Campground")
            fields[1].performTextReplacement("Edmonton")
            fields[2].performTextReplacement("WiFi and showers")
            fields[3].performTextReplacement("6")
            fields[4].performTextReplacement("45")
        }

        // Submit registration
        composeRule.onNode(
            hasText("Register Campsite") and hasClickAction()
        ).performScrollTo().performClick()

        // Verify success message appears after valid submission
        composeRule.onNodeWithText(
            "Registration Successful!"
        ).assertExists()
    }

    // US 02.01.01 - Required field validation
    @Test
    fun registerCampsite_emptyFormShowsError() {
        composeRule.setContent {
            val navController = rememberNavController()
            RegisterCampsiteScreen(
                navController = navController
            )
        }

        // Submit form without filling fields
        composeRule.onNode(
            hasText("Register Campsite") and hasClickAction()
        ).performScrollTo().performClick()

        // Verify error message appears
        composeRule.onNodeWithText(
            "Please complete all required fields and enter valid occupancy/price numbers."
        ).assertExists()
    }


    // US 01.04.01 - View lottery status
    @Test
    fun lotteryStatus_displaysResultsAndBackWorks() {
        var backClicked = false

        composeRule.setContent {
            val navController = rememberNavController()
            AvailableBookingDatesScreen(
                navController = navController,
                onBack = { backClicked = true }
            )
        }

        // Verify screen title
        composeRule.onNodeWithText("Lottery Status")
            .assertExists()

        // Verify lottery information
        composeRule.onAllNodesWithText(
            "Lottery Name : Pine Creek Lottery"
        ).assertCountEquals(2)

        composeRule.onNodeWithText(
            "Reserve Timeline : 2026-11-08 to 2026-11-15"
        ).assertExists()

        composeRule.onAllNodesWithText(
            "Lottery Result : WIN"
        ).assertCountEquals(2)

        // Verify Back button
        composeRule.onNodeWithText("Back")
            .performClick()

        composeRule.runOnIdle {
            assertTrue(backClicked)
        }
    }

    // US 01.06.01 - View current and past reservations
    @Test
    fun reservations_viewCurrentAndPastWorks() {
        composeRule.setContent {
            val navController = rememberNavController()
            ReservationManagementScreen(
                navController = navController
            )
        }

        // Default current reservation appears first
        composeRule.onNodeWithText(
            "Campsite Name : Aurora Ridge Site A"
        ).assertExists()

        composeRule.onNodeWithText(
            "Booked dates : 2026-06-01 to 2026-06-05"
        ).assertExists()

        // Switch to past reservations
        composeRule.onNodeWithText(
            "Past Reservations"
        ).performClick()

        composeRule.onNodeWithText(
            "No past reservations."
        ).assertExists()

        // Switch back to current reservations
        composeRule.onNodeWithText(
            "Current Reservations"
        ).performClick()

        composeRule.onNodeWithText(
            "Campsite Name : Aurora Ridge Site A"
        ).assertExists()
    }

    // US 01.07.01 - Cancel a reservation
    @Test
    fun reservations_cancelReservationWorks() {
        composeRule.setContent {
            val navController = rememberNavController()
            ReservationManagementScreen(
                navController = navController
            )
        }

        // Open cancellation confirmation for the default reservation
        composeRule.onNodeWithText(
            "Cancel Reservation"
        ).performClick()

        // Verify confirmation screen
        composeRule.onNodeWithText(
            "Do you wish to cancel\nyour reservation?"
        ).assertExists()

        // Confirm cancellation
        composeRule.onNodeWithText(
            "Yes, Cancel\nReservation"
        ).performClick()

        // Cancelled reservation disappears from current list
        composeRule.onNodeWithText(
            "No current reservations."
        ).assertExists()
    }

    // US 01.08.01 - Delete profile
    @Test
    fun profile_deleteConfirmationWorks() {
        composeRule.setContent {
            val navController = rememberNavController()
            ProfileManagementScreen(
                navController = navController
            )
        }

        // Start from Profile Settings
        composeRule.onNodeWithText("Profile Settings")
            .assertExists()

        // Open deletion confirmation
        composeRule.onNodeWithText("Delete Profile")
            .performScrollTo()
            .performClick()

        // Verify confirmation message
        composeRule.onNodeWithText(
            "Do you wish to\ndelete your\nprofile?"
        ).assertExists()

        // Go back without deleting
        composeRule.onNodeWithText("No, Go Back")
            .performClick()

        // Verify we are back on Profile Settings
        composeRule.onNodeWithText("Profile Settings")
            .assertExists()

        // Open confirmation again
        composeRule.onNodeWithText("Delete Profile")
            .performScrollTo()
            .performClick()

        // Confirm deletion
        composeRule.onNodeWithText("Yes, Delete my Profile")
            .performClick()
    }

    // US 01.07.03 - Update profile information
    @Test
    fun profile_updateInformationWorks() {
        var updatedName = ""
        var updatedEmail = ""
        var updatedPhone = ""

        composeRule.setContent {
            val navController = rememberNavController()
            ProfileSettingsScreen(
                navController = navController,
                profile = ProfileSettingsData(
                    name = "Old Name",
                    email = "old@example.com",
                    phoneNumber = "7800000000"
                ),
                onUpdateName = { updatedName = it },
                onUpdateEmail = { updatedEmail = it },
                onUpdatePhone = { updatedPhone = it }
            )
        }

        // Update name
        composeRule.onNodeWithText("Enter new name...")
            .performTextInput("New Name")

        composeRule.onAllNodesWithText("Update")[0]
            .performClick()

        // Update email
        composeRule.onNodeWithText("Enter new email...")
            .performTextInput("new@example.com")

        composeRule.onAllNodesWithText("Update")[1]
            .performClick()

        // Update phone number
        composeRule.onNodeWithText("Enter new number...")
            .performTextInput("7801234567")

        composeRule.onAllNodesWithText("Update")[2]
            .performClick()

        // Verify all callbacks were triggered with updated values
        composeRule.runOnIdle {
            assertEquals("New Name", updatedName)
            assertEquals("new@example.com", updatedEmail)
            assertEquals("7801234567", updatedPhone)
        }

        // Verify UI confirmation message appears
        composeRule.onNodeWithText(
            "Phone updated in UI."
        ).assertExists()
    }
}