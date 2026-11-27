package com.example.auroracampsites

import com.example.auroracampsites.data.models.Campsite
import com.example.auroracampsites.data.models.Reservation
import com.example.auroracampsites.data.repository.loadReservationsForOwnedCampsite
import com.example.auroracampsites.ui.screens.OwnerReservationCategory
import com.example.auroracampsites.ui.screens.ownerReservationCategory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Covers reservation date boundaries and the owner check before a query. */
class OwnerReservationsTest {
    private val today = LocalDate.parse("2026-11-26")

    @Test
    fun categorizesCurrentAndPreviousStays() {
        assertEquals(OwnerReservationCategory.ACTIVE, category("ACTIVE", "2026-11-26"))
        assertEquals(OwnerReservationCategory.ACTIVE, category("ACTIVE", "2026-11-27"))
        assertEquals(OwnerReservationCategory.PAST, category("ACTIVE", "2026-11-25"))
        assertEquals(OwnerReservationCategory.PAST, category("COMPLETED", "2026-11-25"))
    }

    @Test
    fun cancelledRecordsNeverAppearAsPastOrActive() {
        assertEquals(OwnerReservationCategory.CANCELLED, category("CANCELLED", "2026-11-25"))
        assertEquals(OwnerReservationCategory.CANCELLED, category("CANCELLED", "2026-11-27"))
        assertEquals(OwnerReservationCategory.CANCELLED, category("CANCELLED", ""))
    }

    @Test
    fun unsupportedStatusOrDateDoesNotBecomeAnActiveReservation() {
        assertNull(category("ACTIVE", "invalid"))
        assertNull(category("UNKNOWN", "2026-11-25"))
        assertNull(category("COMPLETED", "2026-11-27"))
    }

    @Test
    fun ownedCampsiteQueriesReservationsByItsId() = runBlocking {
        val reservation = Reservation(id = "reservation-1", campsiteId = "site-1")
        val results = loadReservationsForOwnedCampsite(
            campsiteId = "site-1",
            ownerId = "owner-1",
            loadCampsite = { id -> Campsite(id = id, ownerID = "owner-1") },
            loadReservations = { id ->
                assertEquals("site-1", id)
                listOf(reservation)
            }
        )
        assertEquals(listOf(reservation), results)
    }

    @Test
    fun foreignMissingAndDeletedCampsitesNeverQueryReservations() = runBlocking {
        val sites = listOf(null, Campsite(id = "site-1", ownerID = "owner-2"),
            Campsite(id = "site-1", ownerID = "owner-1", isDeleted = true))
        for (site in sites) {
            val result = runCatching {
                loadReservationsForOwnedCampsite("site-1", "owner-1", { site }) {
                    throw AssertionError("The reservations query must not run.")
                }
            }
            assertTrue(result.exceptionOrNull() is IllegalStateException)
        }
    }

    @Test
    fun queryErrorsReachTheCaller() = runBlocking {
        val error = IllegalStateException("Read failed")
        val result = runCatching {
            loadReservationsForOwnedCampsite(
                "site-1", "owner-1", { Campsite(id = it, ownerID = "owner-1") }
            ) { throw error }
        }
        assertSame(error, result.exceptionOrNull())
    }

    private fun category(status: String, endDate: String): OwnerReservationCategory? =
        ownerReservationCategory(Reservation(status = status, endDate = endDate), today)
}
