package com.example.auroracampsites.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.auroracampsites.data.models.Reservation
import kotlinx.coroutines.CancellationException
import java.time.LocalDate

/** Displays the reservations for one campsite using the owner-scoped loader. */
@Composable
fun OwnerReservationsScreen(
    campsiteId: String,
    ownerId: String,
    loadReservations: suspend (String, String) -> List<Reservation>,
    onBack: () -> Unit,
    today: LocalDate = LocalDate.now()
) {
    var records by remember(campsiteId, ownerId) { mutableStateOf<List<Reservation>>(emptyList()) }
    var loading by remember(campsiteId, ownerId) { mutableStateOf(true) }
    var failed by remember(campsiteId, ownerId) { mutableStateOf(false) }
    var retry by remember(campsiteId, ownerId) { mutableStateOf(0) }
    var selected by rememberSaveable(campsiteId, ownerId) {
        mutableStateOf(OwnerReservationCategory.ACTIVE)
    }

    LaunchedEffect(campsiteId, ownerId, retry) {
        loading = true
        failed = false
        try {
            records = loadReservations(campsiteId, ownerId)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            records = emptyList()
            failed = true
        } finally {
            loading = false
        }
    }

    val categories = remember(records, today) { records.groupBy { ownerReservationCategory(it, today) } }
    val visibleRecords = categories[selected].orEmpty()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back to My Campsites") }
        Text("Manage Reservations", style = MaterialTheme.typography.headlineSmall)
        TabRow(selectedTabIndex = selected.ordinal) {
            OwnerReservationCategory.entries.forEach { category ->
                Tab(
                    selected = selected == category,
                    onClick = { selected = category },
                    text = { Text(category.label) }
                )
            }
        }
        when {
            loading -> CircularProgressIndicator()
            failed -> {
                Text("Unable to load reservations.", color = MaterialTheme.colorScheme.error)
                Button(onClick = {
                    loading = true
                    retry++
                }) { Text("Retry") }
            }
            else -> {
                if (!categories[null].isNullOrEmpty()) {
                    Text("Some reservations have an unsupported status or date.")
                }
                if (visibleRecords.isEmpty()) {
                    Text(selected.emptyMessage)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(visibleRecords, key = { it.id }) { reservation ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(reservation.campsiteName, style = MaterialTheme.typography.titleMedium)
                                    Text("Reservation: ${reservation.id}")
                                    Text("Dates: ${reservation.startDate} to ${reservation.endDate}")
                                    Text("Status: ${selected.label}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
