package com.example.auroracampsites

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.auroracampsites.data.models.WaitList
import com.example.auroracampsites.ui.screens.OwnerWaitlistScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the owner waitlist UI with a controlled loader. */
@RunWith(AndroidJUnit4::class)
class OwnerWaitlistScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun refreshShowsEntrantsJoiningAndLeavingTheSelectedLottery() {
        var entries = emptyList<WaitList>()
        composeRule.setContent {
            OwnerWaitlistScreen("site-1", "round-1", "owner-1",
                loadWaitlist = { campsiteId, lotteryId, ownerId ->
                    assertEquals("site-1", campsiteId)
                    assertEquals("round-1", lotteryId)
                    assertEquals("owner-1", ownerId)
                    entries
                },
                onBack = {}
            )
        }

        composeRule.onNodeWithText("No entrants have joined this lottery.").assertExists()
        composeRule.runOnIdle { entries = listOf(WaitList(id = "entry-1", entrantID = "entrant-1")) }
        composeRule.onNodeWithText("Refresh Waitlist").performClick()
        composeRule.onNodeWithText("Entrant: entrant-1").assertExists()
        composeRule.runOnIdle { entries = emptyList() }
        composeRule.onNodeWithText("Refresh Waitlist").performClick()
        composeRule.onNodeWithText("Entrant: entrant-1").assertDoesNotExist()
        composeRule.onNodeWithText("No entrants have joined this lottery.").assertExists()
    }

    @Test
    fun readErrorIsNotAnEmptyListAndCanBeRetried() {
        var fail = true
        composeRule.setContent {
            OwnerWaitlistScreen("site-1", "round-1", "owner-1",
                loadWaitlist = { _, _, _ ->
                    check(!fail)
                    emptyList()
                },
                onBack = {}
            )
        }

        composeRule.onNodeWithText("Unable to load this lottery's waitlist.", substring = true)
            .assertExists()
        composeRule.onNodeWithText("No entrants have joined this lottery.").assertDoesNotExist()
        composeRule.runOnIdle { fail = false }
        composeRule.onNodeWithText("Retry").performClick()
        composeRule.onNodeWithText("No entrants have joined this lottery.").assertExists()
    }
}
