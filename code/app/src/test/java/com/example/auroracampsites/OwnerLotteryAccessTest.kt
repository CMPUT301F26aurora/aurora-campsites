package com.example.auroracampsites

import com.example.auroracampsites.data.models.Campsite
import com.example.auroracampsites.data.models.Lottery
import com.example.auroracampsites.data.repository.requireOwnedLottery
import org.junit.Assert.assertThrows
import org.junit.Test

/** Covers the ownership checks used before reading a lottery's waitlist. */
class OwnerLotteryAccessTest {
    private val campsite = Campsite(id = "site-1", ownerID = "owner-1")
    private val lottery = Lottery(id = "round-1", campsiteId = "site-1", ownerId = "owner-1")

    @Test
    fun acceptsLotteryForOwnedCampsite() {
        requireOwnedLottery(campsite, lottery, "owner-1")
    }

    @Test
    fun rejectsAnotherOwnersCampsiteOrLottery() {
        assertThrows(IllegalStateException::class.java) {
            requireOwnedLottery(campsite.copy(ownerID = "owner-2"), lottery, "owner-1")
        }
        assertThrows(IllegalStateException::class.java) {
            requireOwnedLottery(campsite, lottery.copy(ownerId = "owner-2"), "owner-1")
        }
    }

    @Test
    fun rejectsLotteryLinkedToAnotherCampsite() {
        assertThrows(IllegalStateException::class.java) {
            requireOwnedLottery(campsite, lottery.copy(campsiteId = "site-2"), "owner-1")
        }
    }

    @Test
    fun rejectsMissingOrDeletedDocuments() {
        assertThrows(IllegalStateException::class.java) {
            requireOwnedLottery(campsite, null, "owner-1")
        }
        assertThrows(IllegalStateException::class.java) {
            requireOwnedLottery(null, lottery, "owner-1")
        }
        assertThrows(IllegalStateException::class.java) {
            requireOwnedLottery(campsite.copy(isDeleted = true), lottery, "owner-1")
        }
    }
}
