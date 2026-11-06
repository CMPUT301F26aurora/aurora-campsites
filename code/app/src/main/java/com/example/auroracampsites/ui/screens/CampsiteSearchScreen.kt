package com.example.auroracampsites.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.auroracampsites.data.models.Campsite
import com.example.auroracampsites.data.repository.CampsiteRepository

@Composable
fun CampsiteSearchScreen(navController: NavController) {
    var search by remember { mutableStateOf("") }

    val repository = remember { CampsiteRepository() }

    // Hold the full list and the filtered display list
    var allCampsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    val displayList = remember { mutableStateListOf<Campsite>() }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch campsites from Firestore when the screen launches
    LaunchedEffect(Unit) {
        isLoading = true
        allCampsites = repository.fetchCampsites()
        displayList.clear()
        displayList.addAll(allCampsites)
        isLoading = false
    }

    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }

    var provinceFilter by remember { mutableStateOf("") }
    var dateFilter by remember { mutableStateOf("") }
    var guestsFilter by remember { mutableStateOf("") }

    selectedCampsite?.let { campsite ->
        CampsiteDetailsScreen(
            navController = navController,
            campsite = campsite,
            onBackClick = { selectedCampsite = null }
        )
        return
    }

    fun applyFilters() {
        displayList.clear()
        val filtered = allCampsites.filter { campsite ->
            val matchesSearch = search.isBlank() ||
                    campsite.name.contains(search, ignoreCase = true) ||
                    campsite.location.contains(search, ignoreCase = true)

            val matchesProvince = provinceFilter.isBlank() ||
                    campsite.location.contains(provinceFilter, ignoreCase = true)

            val matchesDate = dateFilter.isBlank() ||
                    campsite.availableDates.any { it.contains(dateFilter, ignoreCase = true) }

            val matchesGuests = guestsFilter.toIntOrNull()?.let { reqGuests ->
                campsite.maxOccupancy >= reqGuests
            } ?: true

            matchesSearch && matchesProvince && matchesDate && matchesGuests
        }
        displayList.addAll(filtered)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.testTag("backButton")
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Campsite Search",
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search Campsites") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("searchInput")
            )

            Button(
                onClick = { applyFilters() },
                modifier = Modifier.testTag("searchButton")
            ) {
                Text("Search")
            }

            Button(
                onClick = { showFilterSheet = true },
                modifier = Modifier.testTag("filterButton")
            ) {
                Text("Filter")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(
                modifier = Modifier.testTag("campsiteList")
            ) {
                items(displayList) { campsite ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        onClick = { selectedCampsite = campsite },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("campsiteCard_${campsite.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = campsite.name,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Location: ${campsite.location}",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Max Occupancy: ${campsite.maxOccupancy} guests",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Rating: ${campsite.rating} / 5.0",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        AlertDialog(
            onDismissRequest = { showFilterSheet = false },
            title = { Text("Filter Campsites") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = provinceFilter,
                        onValueChange = { provinceFilter = it },
                        label = { Text("Province / State") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("filterProvinceInput")
                    )
                    TextField(
                        value = dateFilter,
                        onValueChange = { dateFilter = it },
                        label = { Text("Booking Date (YYYY-MM-DD)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("filterDateInput")
                    )
                    TextField(
                        value = guestsFilter,
                        onValueChange = { guestsFilter = it },
                        label = { Text("Number of Guests") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("filterGuestsInput")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        applyFilters()
                        showFilterSheet = false
                    },
                    modifier = Modifier.testTag("applyFilterButton")
                ) {
                    Text("Apply Filters")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        provinceFilter = ""
                        dateFilter = ""
                        guestsFilter = ""
                        applyFilters()
                        showFilterSheet = false
                    }
                ) {
                    Text("Reset")
                }
            }
        )
    }
}