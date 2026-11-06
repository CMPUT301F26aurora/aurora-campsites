
package com.example.auroracampsites

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the nine halfway-checkpoint user stories.
 *
 * These tests check the screens, buttons, and input fields.
 * The app currently uses sample data, so these tests do not
 * check Firestore or the backend.
 *
 * Each test starts with a fresh screen.
 */
@RunWith(AndroidJUnit4::class)
class HalfwayCheckpointScreenTest {

    // Creates a Compose test environment for each test.
    @get:Rule
    val composeRule = createComposeRule()

    /**
     * Opens the halfway checkpoint screen.
     * Each test uses a new screen with the original sample data.
     */
    private fun openScreen() {
        composeRule.setContent {
            HalfwayCheckpointScreen()
        }
    }


    /**
     * Finds and clicks a button with the given text.
     * Only clickable elements are selected, so page titles
     * with the same text will not be clicked by mistake.
     */
    private fun tap(label: String, index: Int = 0) {
        composeRule.onAllNodes(
            hasText(label) and hasClickAction()
        )[index]
            .performScrollTo()
            .performClick()
    }


    /**
     * Enters text into a form field.
     * This is mainly used for the lottery registration form.
     */
    private fun enter(label: String, value: String) {
        composeRule.onNodeWithText(label)
            .performScrollTo()
            .performTextInput(value)
    }

    /**
     * US 02.03.01
     * Checks that owners can view their own campsites.
     * Campsites belonging to other owners should not appear.
     */
    @Test
    fun ownerCanViewOwnCampsites() {
        openScreen()

        tap("Campsite Owner")

        composeRule.onNodeWithText("My Campsites")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Pine Creek Campground")
            .assertExists()

        composeRule.onNodeWithText("Lakeview Campsite")
            .assertExists()

        composeRule.onNodeWithText("Riverbend Campsite")
            .assertDoesNotExist()
    }

    /**
     * US 02.04.01
     * Checks that an owner can open the lottery form
     * for one of their campsites.
     */
    @Test
    fun ownerCanOpenLotteryForm() {
        openScreen()

        tap("Campsite Owner")
        tap("Create Lottery")

        composeRule.onNodeWithText(
            "Campsite: Pine Creek Campground"
        ).assertExists()

        composeRule.onNodeWithText("Registration Period")
            .assertExists()

        composeRule.onNodeWithText("Booking Period")
            .assertExists()

        composeRule.onNodeWithText("Number of Winners")
            .assertExists()
    }

    /**
     * US 02.04.01
     * US 02.04.03
     * US 02.04.04
     * US 02.04.05
     *
     * Checks that an owner can create a lottery using
     * registration dates, booking dates, and a winner count.
     *
     * The information is saved locally for now.
     */
    @Test
    fun ownerCanCreateLotteryWithPeriodsAndWinnerCount() {
        openScreen()

        tap("Campsite Owner")
        tap("Create Lottery")

        // Enter the registration period.
        enter(
            "Registration Start (YYYY-MM-DD)",
            "2026-11-10"
        )

        enter(
            "Registration End (YYYY-MM-DD)",
            "2026-11-15"
        )

        // Enter the booking period.
        enter(
            "Booking Start (YYYY-MM-DD)",
            "2026-12-01"
        )

        enter(
            "Booking End (YYYY-MM-DD)",
            "2026-12-05"
        )

        // Set the number of lottery winners.
        enter("Winners", "3")

        // Create the lottery using the entered information.
        tap("Create Lottery")

        // Check the confirmation screen.
        composeRule.onNodeWithText("Lottery Created")
            .assertExists()

        // Check that the registration dates were saved.
        composeRule.onNodeWithText(
            "Registration: 2026-11-10 to 2026-11-15"
        ).assertExists()

        // Check that the booking dates were saved.
        composeRule.onNodeWithText(
            "Booking: 2026-12-01 to 2026-12-05"
        ).assertExists()

        // Check that the winner count was saved.
        composeRule.onNodeWithText(
            "Number of winners: 3"
        ).assertExists()
    }

    /**
     * US 02.04.03
     * Checks that the registration end date cannot
     * be earlier than the registration start date.
     */
    @Test
    fun invalidRegistrationPeriodShowsError() {
        openScreen()

        tap("Campsite Owner")
        tap("Create Lottery")

        // Enter an invalid registration period.
        enter(
            "Registration Start (YYYY-MM-DD)",
            "2026-11-20"
        )

        enter(
            "Registration End (YYYY-MM-DD)",
            "2026-11-10"
        )

        // Fill in the remaining required fields.
        enter(
            "Booking Start (YYYY-MM-DD)",
            "2026-12-01"
        )

        enter(
            "Booking End (YYYY-MM-DD)",
            "2026-12-05"
        )

        enter("Winners", "3")

        tap("Create Lottery")

        // The app should show an error instead of creating the lottery.
        composeRule.onNodeWithText(
            "Registration end must not be before start."
        ).assertExists()

        composeRule.onNodeWithText("Lottery Created")
            .assertDoesNotExist()
    }

    /**
     * US 02.04.04
     * Checks that the booking end date cannot
     * be earlier than the booking start date.
     */
    @Test
    fun invalidBookingPeriodShowsError() {
        openScreen()

        tap("Campsite Owner")
        tap("Create Lottery")

        // Enter valid registration dates.
        enter(
            "Registration Start (YYYY-MM-DD)",
            "2026-11-10"
        )

        enter(
            "Registration End (YYYY-MM-DD)",
            "2026-11-15"
        )

        // Enter an invalid booking period.
        enter(
            "Booking Start (YYYY-MM-DD)",
            "2026-12-10"
        )

        enter(
            "Booking End (YYYY-MM-DD)",
            "2026-12-01"
        )

        enter("Winners", "3")

        tap("Create Lottery")

        // The app should reject the invalid booking dates.
        composeRule.onNodeWithText(
            "Booking end must not be before start."
        ).assertExists()
    }

    /**
     * US 02.04.05
     * Checks that the number of lottery winners
     * must be greater than zero.
     */
    @Test
    fun invalidWinnerCountShowsError() {
        openScreen()

        tap("Campsite Owner")
        tap("Create Lottery")

        // Enter valid registration and booking dates.
        enter(
            "Registration Start (YYYY-MM-DD)",
            "2026-11-10"
        )

        enter(
            "Registration End (YYYY-MM-DD)",
            "2026-11-15"
        )

        enter(
            "Booking Start (YYYY-MM-DD)",
            "2026-12-01"
        )

        enter(
            "Booking End (YYYY-MM-DD)",
            "2026-12-05"
        )

        // Zero winners should not be accepted.
        enter("Winners", "0")

        tap("Create Lottery")

        // Check that the correct validation message appears.
        composeRule.onNodeWithText(
            "Enter a number of winners greater than zero."
        ).assertExists()
    }

    /**
     * US 03.04.01
     * Checks that an admin can browse the list
     * of registered campsites.
     */
    @Test
    fun adminCanBrowseCampsites() {
        openScreen()

        tap("Admin - Campsites")

        composeRule.onNodeWithText("Admin - Browse Campsites")
            .assertExists()

        // All three sample campsites should be visible.
        composeRule.onNodeWithText("Pine Creek Campground")
            .assertExists()

        composeRule.onNodeWithText("Lakeview Campsite")
            .assertExists()

        composeRule.onNodeWithText("Riverbend Campsite")
            .assertExists()
    }

    /**
     * US 03.01.01
     * Checks that cancelling a campsite removal
     * keeps the campsite in the list.
     */
    @Test
    fun adminCanCancelCampsiteRemoval() {
        openScreen()

        tap("Admin - Campsites")

        // Open the confirmation dialog for the first campsite.
        tap("Remove Campsite")

        composeRule.onNodeWithText("Remove campsite?")
            .assertExists()

        // Cancel instead of removing the campsite.
        composeRule.onNodeWithText("Cancel")
            .performClick()

        // The campsite should still exist.
        composeRule.onNodeWithText("Pine Creek Campground")
            .assertExists()
    }

    /**
     * US 03.01.01
     * Checks that an admin can remove a campsite
     * after confirming the action.
     */
    @Test
    fun adminCanRemoveCampsite() {
        openScreen()

        tap("Admin - Campsites")

        // Select the first campsite for removal.
        tap("Remove Campsite")

        // Confirm the removal.
        composeRule.onNodeWithText("Remove")
            .performClick()

        // The removed campsite should disappear.
        composeRule.onNodeWithText("Pine Creek Campground")
            .assertDoesNotExist()

        // Other campsites should remain.
        composeRule.onNodeWithText("Lakeview Campsite")
            .assertExists()
    }

    /**
     * US 03.05.01
     * Checks that an admin can browse
     * the registered user profiles.
     */
    @Test
    fun adminCanBrowseProfiles() {
        openScreen()

        tap("Admin - Profiles")

        composeRule.onNodeWithText("Admin - Browse Profiles")
            .assertExists()

        // Check the sample profiles.
        composeRule.onNodeWithText("Entrant 001")
            .assertExists()

        composeRule.onNodeWithText("Entrant 002")
            .assertExists()

        composeRule.onNodeWithText("Campsite Owner 001")
            .assertExists()
    }

    /**
     * US 03.02.01
     * Checks that cancelling a profile removal
     * does not delete the profile.
     */
    @Test
    fun adminCanCancelProfileRemoval() {
        openScreen()

        tap("Admin - Profiles")

        // Open the removal confirmation dialog.
        tap("Remove Profile")

        composeRule.onNodeWithText("Remove profile?")
            .assertExists()

        // Cancel the removal.
        composeRule.onNodeWithText("Cancel")
            .performClick()

        // The profile should still be displayed.
        composeRule.onNodeWithText("Entrant 001")
            .assertExists()
    }

    /**
     * US 03.02.01
     * Checks that an admin can remove a profile
     * after confirming the action.
     */
    @Test
    fun adminCanRemoveProfile() {
        openScreen()

        tap("Admin - Profiles")

        // Select the first profile for removal.
        tap("Remove Profile")

        // Confirm the removal.
        composeRule.onNodeWithText("Remove")
            .performClick()

        // The removed profile should no longer appear.
        composeRule.onNodeWithText("Entrant 001")
            .assertDoesNotExist()

        // Other profiles should remain in the list.
        composeRule.onNodeWithText("Entrant 002")
            .assertExists()
    }
}
