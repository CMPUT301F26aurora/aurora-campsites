package com.example.auroracampsites

import com.example.auroracampsites.data.models.Campsite
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class CampsiteSearchTest {
    private lateinit var sampleCampsites: List<Campsite>

    @Before
    fun setUp() {
        // Set up realistic test data matching your default search list
        sampleCampsites = listOf(
            Campsite(
                id = "1",
                name = "Jasper National Park",
                location = "Alberta, Canada",
                description = "Nestled in the heart of the Rocky Mountains.",
                maxOccupancy = 6,
                price = 55.0,
                availableDates = listOf("2026-07-15"),
                rating = 4.8
            ),
            Campsite(
                id = "2",
                name = "Banff National Park",
                location = "Alberta, Canada",
                description = "Scenic campsite right near the lake.",
                maxOccupancy = 4,
                price = 60.0,
                availableDates = listOf("2026-06-01"),
                rating = 4.9
            ),
            Campsite(
                id = "3",
                name = "Elk Island National Park",
                location = "Alberta, Canada",
                description = "Quiet woodland campground perfect for stargazing.",
                maxOccupancy = 2,
                price = 40.0,
                availableDates = listOf("2026-08-10"),
                rating = 4.3
            )
        )
    }

    @Test
    fun filter_bySearchQuery_returnsMatchingCampsite() {
        val query = "Banff"
        val filtered = sampleCampsites.filter { campsite ->
            campsite.name.contains(query, ignoreCase = true) ||
                    campsite.location.contains(query, ignoreCase = true)
        }

        assertEquals(1, filtered.size)
        assertEquals("Banff National Park", filtered[0].name)
    }

    @Test
    fun filter_byMaxOccupancy_returnsSufficientCapacity() {
        val requiredGuests = 5
        val filtered = sampleCampsites.filter { campsite ->
            campsite.maxOccupancy >= requiredGuests
        }

        assertEquals(1, filtered.size)
        assertEquals("Jasper National Park", filtered[0].name)
    }

    @Test
    fun filter_byAvailableDate_returnsMatchingDate() {
        val dateQuery = "2026-08-10"
        val filtered = sampleCampsites.filter { campsite ->
            campsite.availableDates.contains(dateQuery)
        }

        assertEquals(1, filtered.size)
        assertEquals("Elk Island National Park", filtered[0].name)
    }

    @Test
    fun filter_noMatches_returnsEmptyList() {
        val query = "NonExistentPark"
        val filtered = sampleCampsites.filter { campsite ->
            campsite.name.contains(query, ignoreCase = true)
        }

        assertTrue(filtered.isEmpty())
    }

    @Test
    fun campsite_defaultValues_verifyModelDefaults() {
        val defaultCampsite = Campsite(id = "99", name = "Test Park", location = "AB")

        assertEquals(4, defaultCampsite.maxOccupancy)
        assertEquals(45.0, defaultCampsite.price, 0.01)
        assertTrue(defaultCampsite.hasActiveLottery)
        assertFalse(defaultCampsite.isWinner)
    }
}
