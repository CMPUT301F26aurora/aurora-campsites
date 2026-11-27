package com.example.auroracampsites.data.repository

import com.example.auroracampsites.data.models.Campsite
import com.example.auroracampsites.data.models.Lottery

/** Checks that the selected lottery and campsite both belong to the current owner. */
internal fun requireOwnedLottery(
    campsite: Campsite?,
    lottery: Lottery?,
    ownerId: String
) {
    check(campsite != null && !campsite.isDeleted && lottery != null &&
        ownerId.isNotBlank() && campsite.id.isNotBlank() &&
        campsite.ownerID == ownerId && lottery.ownerId == ownerId &&
        lottery.campsiteId == campsite.id
    ) { "Lottery unavailable or not owned by this user." }
}
