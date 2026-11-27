package com.example.auroracampsites.data.repository

import com.example.auroracampsites.data.models.Campsite
import com.example.auroracampsites.data.models.Reservation

/** Checks campsite ownership before calling the scoped reservation query. */
internal suspend fun loadReservationsForOwnedCampsite(
    campsiteId: String,
    ownerId: String,
    loadCampsite: suspend (String) -> Campsite?,
    loadReservations: suspend (String) -> List<Reservation>
): List<Reservation> {
    require(campsiteId.isNotBlank() && ownerId.isNotBlank())
    val campsite = loadCampsite(campsiteId)
    check(campsite != null && !campsite.isDeleted && campsite.id == campsiteId && campsite.ownerID == ownerId) {
        "Campsite unavailable or not owned by this user."
    }
    return loadReservations(campsiteId)
}
