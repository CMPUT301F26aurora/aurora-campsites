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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.auroracampsites.data.models.WaitList
import kotlinx.coroutines.CancellationException

/** Shows the entrants in one lottery and lets the owner reload the list. */
@Composable
fun OwnerWaitlistScreen(
    campsiteId: String,
    lotteryId: String,
    ownerId: String,
    loadWaitlist: suspend (String, String, String) -> List<WaitList>,
    onBack: () -> Unit
) {
    var entries by remember(campsiteId, lotteryId, ownerId) { mutableStateOf<List<WaitList>>(emptyList()) }
    var loading by remember(campsiteId, lotteryId, ownerId) { mutableStateOf(true) }
    var failed by remember(campsiteId, lotteryId, ownerId) { mutableStateOf(false) }
    var attempt by remember(campsiteId, lotteryId, ownerId) { mutableStateOf(0) }

    LaunchedEffect(campsiteId, lotteryId, ownerId, attempt) {
        loading = true
        failed = false
        try {
            entries = loadWaitlist(campsiteId, lotteryId, ownerId)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            entries = emptyList()
            failed = true
        } finally {
            loading = false
        }
    }

    fun reload() {
        loading = true
        attempt++
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back to Manage Lotteries") }
        Text("Lottery Waitlist", style = MaterialTheme.typography.headlineSmall)
        when {
            loading -> CircularProgressIndicator()
            failed -> {
                Text(
                    "Unable to load this lottery's waitlist. Check that the lottery is available and that you have access.",
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = { reload() }) { Text("Retry") }
            }
            else -> {
                Button(onClick = { reload() }) { Text("Refresh Waitlist") }
                if (entries.isEmpty()) {
                    Text("No entrants have joined this lottery.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(entries, key = { it.id }) { entry ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Entrant: ${entry.entrantID}")
                                    Text("Status: ${entry.status}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
