package com.example.auroracampsites.ui.screens

import com.example.auroracampsites.data.models.Reservation
import java.time.LocalDate
import java.time.format.DateTimeParseException

internal enum class OwnerReservationCategory(val label: String, val emptyMessage: String) {
    ACTIVE("Active", "No active reservations."),
    PAST("Past", "No past reservations."),
    CANCELLED("Cancelled", "No cancelled reservations.")
}

/** Keeps cancelled records separate from previous stays, even if their dates have passed. */
internal fun ownerReservationCategory(
    reservation: Reservation,
    today: LocalDate
): OwnerReservationCategory? {
    if (reservation.status == "CANCELLED") return OwnerReservationCategory.CANCELLED
    if (reservation.status != "ACTIVE" && reservation.status != "COMPLETED") return null

    val endDate = try {
        LocalDate.parse(reservation.endDate)
    } catch (_: DateTimeParseException) {
        return null
    }
    return when {
        endDate.isBefore(today) -> OwnerReservationCategory.PAST
        reservation.status == "ACTIVE" -> OwnerReservationCategory.ACTIVE
        else -> null
    }
}
