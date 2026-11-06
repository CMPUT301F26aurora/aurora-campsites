
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

@RunWith(AndroidJUnit4::class)
class AuroraScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    // US 01.11.01 - App Policy
    @Test
    fun appPolicy_displaysPolicyContent() {
        composeRule.setContent {
            AppPolicyScreen(
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
        var reservationsClicked = false

        composeRule.setContent {
            BookingConfirmationScreen(
                onSendNotification = {
                    notificationClicked = true
                },
                onDownloadCsv = {
                    csvClicked = true
                },
                onViewReservations = {
                    reservationsClicked = true
                }
            )
        }

        // Test notification button
        composeRule.onNodeWithText("Send a Notification")
            .performClick()

        // Test CSV download button
        composeRule.onNodeWithText("Download as CSV")
            .performClick()

        // Test reservations button
        composeRule.onNodeWithText("View Reservations")
            .performClick()

        // Verify all three callbacks were triggered
        composeRule.runOnIdle {
            assertTrue(notificationClicked)
            assertTrue(csvClicked)
            assertTrue(reservationsClicked)

        }
    }

    // US 01.13.01 - Leave a campsite review
    @Test
    fun leaveReview_submitReviewWorks() {
        var submittedReview: CampsiteReviewData? = null

        composeRule.setContent {
            LeaveReviewScreen(
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
        var qrClicked = false
        var editClicked = false

        composeRule.setContent {
            CampsiteDetailsScreen(
                campsiteName = "Test Campground",
                location = "Edmonton, Alberta",
                description = "WiFi, showers",
                maximumOccupancy = "6",
                mapLocation = "Edmonton, AB",
                tags = "Family, outdoor",
                onViewQRCode = {
                    qrClicked = true
                },
                onEditCampsite = {
                    editClicked = true
                }
            )
        }

        // Verify campsite information
        composeRule.onNodeWithText("Test Campground")
            .assertExists()

        composeRule.onNodeWithText("Edmonton, Alberta")
            .assertExists()

        composeRule.onNodeWithText("WiFi, showers")
            .assertExists()

        composeRule.onNodeWithText("6")
            .assertExists()

        composeRule.onNodeWithText("Edmonton, AB")
            .assertExists()

        composeRule.onNodeWithText("Family, outdoor")
            .assertExists()

        // Test QR Code button
        composeRule.onNodeWithText("View QR Code")
            .performClick()

        // Test Edit Campsite button
        composeRule.onNodeWithText("Edit Campsite")
            .performClick()

        // Verify button callbacks
        composeRule.runOnIdle {
            assertTrue(qrClicked)
            assertTrue(editClicked)
        }
    }

    // US 02.01.01 / US 02.01.02 - Register campsite
    @Test
    fun registerCampsite_validFormWorks() {
        var savedData: CampsiteFormData? = null

        composeRule.setContent {
            RegisterCampsiteScreen(
                onSaved = { savedData = it }
            )
        }

        // Find editable text fields in form order
        val fields = composeRule.onAllNodes(
            hasSetTextAction()
        )

        fields[0].performTextReplacement("Test Campground")
        fields[1].performTextReplacement("Edmonton")
        fields[2].performTextReplacement("WiFi and showers")
        fields[3].performTextReplacement("6")
        fields[4].performTextReplacement("Edmonton, AB")
        fields[5].performTextReplacement("Family")

        // Submit registration
        composeRule.onNode(
            hasText("Register Campsite") and hasClickAction()
        ).performScrollTo().performClick()

        // Verify submitted data
        composeRule.runOnIdle {
            assertEquals("Test Campground", savedData?.name)
            assertEquals("Edmonton", savedData?.location)
            assertEquals("WiFi and showers", savedData?.description)
            assertEquals("6", savedData?.occupancy)
            assertEquals("Edmonton, AB", savedData?.mapLocation)
            assertEquals("Family", savedData?.tags)
        }

        // Verify success message
        composeRule.onNodeWithText(
            "Registration Successful!"
        ).assertExists()
    }

    // US 02.01.01 - Required field validation
    @Test
    fun registerCampsite_emptyFormShowsError() {
        var saveCalled = false

        composeRule.setContent {
            RegisterCampsiteScreen(
                onSaved = { saveCalled = true }
            )
        }

        composeRule.onNode(
            hasText("Register Campsite") and hasClickAction()
        ).performScrollTo().performClick()

        composeRule.onNodeWithText(
            "Please complete all required fields and enter a valid occupancy."
        ).assertExists()

        composeRule.runOnIdle {
            assertTrue(!saveCalled)
        }
    }

    // US 01.04.01 - View lottery status
    @Test
    fun lotteryStatus_displaysResultsAndBackWorks() {
        var backClicked = false

        composeRule.setContent {
            AvailableBookingDatesScreen(
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
        val current = ReservationData(
            id = "R001",
            campsiteName = "Pine Creek",
            location = "Hinton",
            maximumOccupancy = 6,
            rating = 4.5,
            startDate = "2026-11-10",
            endDate = "2026-11-12"
        )

        val past = ReservationData(
            id = "R002",
            campsiteName = "Lake Camp",
            location = "Jasper",
            maximumOccupancy = 4,
            rating = 4.0,
            startDate = "2026-10-01",
            endDate = "2026-10-03"
        )

        composeRule.setContent {
            ReservationManagementScreen(
                currentReservations = listOf(current),
                pastReservations = listOf(past)
            )
        }

        // Current reservations appear first
        composeRule.onNodeWithText(
            "Campsite Name : Pine Creek"
        ).assertExists()

        composeRule.onNodeWithText(
            "Booked dates : 2026-11-10 to 2026-11-12"
        ).assertExists()

        // Switch to past reservations
        composeRule.onNodeWithText(
            "Past Reservations"
        ).performClick()

        composeRule.onNodeWithText(
            "Campsite Name : Lake Camp"
        ).assertExists()

        composeRule.onNodeWithText(
            "Booked dates : 2026-10-01 to 2026-10-03"
        ).assertExists()

        // Switch back to current reservations
        composeRule.onNodeWithText(
            "Current Reservations"
        ).performClick()

        composeRule.onNodeWithText(
            "Campsite Name : Pine Creek"
        ).assertExists()
    }

    // US 01.07.01 - Cancel a reservation
    @Test
    fun reservations_cancelReservationWorks() {
        val reservation = ReservationData(
            id = "R003",
            campsiteName = "Test Camp",
            location = "Edmonton",
            maximumOccupancy = 5,
            rating = 4.5,
            startDate = "2026-11-15",
            endDate = "2026-11-17"
        )

        var cancelledReservation: ReservationData? = null

        composeRule.setContent {
            ReservationManagementScreen(
                currentReservations = listOf(reservation),
                onConfirmCancellation = {
                    cancelledReservation = it
                }
            )
        }

        // Open cancellation confirmation
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

        // Verify correct reservation was passed
        composeRule.runOnIdle {
            assertEquals("R003", cancelledReservation?.id)
        }

        // Cancelled reservation disappears from current list
        composeRule.onNodeWithText(
            "No current reservations."
        ).assertExists()
    }

    // US 01.08.01 - Delete profile
    @Test
    fun profile_deleteConfirmationWorks() {
        var deleteCalled = false

        composeRule.setContent {
            ProfileManagementScreen(
                profile = ProfileSettingsData(
                    name = "Test User",
                    email = "test@example.com",
                    phoneNumber = "7801234567"
                ),
                onDeleteProfile = {
                    deleteCalled = true
                }
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

        composeRule.onNodeWithText("Profile Settings")
            .assertExists()

        composeRule.runOnIdle {
            assertTrue(!deleteCalled)
        }

        // Open confirmation again
        composeRule.onNodeWithText("Delete Profile")
            .performScrollTo()
            .performClick()

        // Confirm deletion
        composeRule.onNodeWithText("Yes, Delete my Profile")
            .performClick()

        // Verify deletion callback
        composeRule.runOnIdle {
            assertTrue(deleteCalled)
        }
    }

    // US 01.07.03 - Update profile information
    @Test
    fun profile_updateInformationWorks() {
        var updatedName = ""
        var updatedEmail = ""
        var updatedPhone = ""

        composeRule.setContent {
            ProfileManagementScreen(
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

        // Verify all callbacks
        composeRule.runOnIdle {
            assertEquals("New Name", updatedName)
            assertEquals("new@example.com", updatedEmail)
            assertEquals("7801234567", updatedPhone)
        }

        // Verify UI confirmation
        composeRule.onNodeWithText(
            "Phone updated in UI."
        ).assertExists()
    }



}
