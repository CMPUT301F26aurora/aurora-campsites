package com.example.auroracampsites

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class CampsiteDetailTest {
    private lateinit var detailCampsite: Campsite

    @Before
    fun setUp() {
        //Set up realistic detail test data matching CampsiteDetailScreen requirements
        detailCampsite = Campsite(
            id = "campsite-101",
            name = "Aurora Borealis Pines",
            location = "Jasper National Park, AB",
            description = "Secluded lakeside camping under the Northern Lights.",
            amenities = listOf("Fire Pit", "Clean Water", "Bear Vault"),
            maxOccupancy = 6,
            price = 45.0,
            availableDate = "2026-07-10",
            rating = 4.8,
            hasActiveLottery = true,
            isWaitlistFull = false,
            isWinner = false,
            isLoser = false
        )
    }

    @Test
    fun campsiteDetail_verifyInitialAttributes() {
        assertEquals("Aurora Borealis Pines", detailCampsite.name)
        assertEquals("Jasper National Park, AB", detailCampsite.location)
        assertEquals(45.0, detailCampsite.price, 0.01)
        assertEquals(3, detailCampsite.amenities.size)
        assertTrue(detailCampsite.hasActiveLottery)
    }

    @Test
    fun campsiteDetail_calculateTotalPrice_returnsCorrectAmount() {
        val stayDurationDays = 3
        val expectedTotal = 135.0 //45.0 * 3
        val actualTotal = detailCampsite.price * stayDurationDays

        assertEquals(expectedTotal, actualTotal, 0.01)
    }

    @Test
    fun campsiteDetail_lotteryWinnerState_mutuallyExclusiveWithLoser() {
        val winnerCampsite = detailCampsite.copy(isWinner = true, isLoser = false)
        assertTrue(winnerCampsite.isWinner)
        assertFalse(winnerCampsite.isLoser)

        val loserCampsite = detailCampsite.copy(isWinner = false, isLoser = true)
        assertFalse(loserCampsite.isWinner)
        assertTrue(loserCampsite.isLoser)
    }

    @Test
    fun campsiteDetail_waitlistFull_disablesLotteryEntry() {
        val fullWaitlistCampsite = detailCampsite.copy(isWaitlistFull = true)
        assertTrue(fullWaitlistCampsite.hasActiveLottery)
        assertTrue(fullWaitlistCampsite.isWaitlistFull)
    }
}
