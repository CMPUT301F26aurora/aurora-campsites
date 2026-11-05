package com.example.auroracampsites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.items

/** U.S.01.01.01 - U.S.01.01.06
 * Role/Pattern: Composable UI Screen (View) for searching campsites.
 * Renders search input, filter trigger, and matching campsite results.
 *
 * Outstanding Issues: Live Firestore connectivity to replace sample data.
 */
@Composable
fun CampsiteSearchScreen(navController: NavController) {
    var search by remember {
        mutableStateOf("")
    }


    // Master sample list with full campsite details
    val allCampsites = remember {
        listOf(
            Campsite(
                id = "1",
                name = "Jasper National Park",
                location = "Alberta, Canada",
                description = "Nestled in the heart of the Rocky Mountains with stunning alpine views and lake access.",
                amenities = listOf("Fire Pit", "Water Hookup", "Bear Proof Storage", "Showers"),
                maxOccupancy = 6,
                price = 55.0,
                availableDate = "2026-07-15",
                rating = 4.8,
                hasActiveLottery = true
            ),
            Campsite(
                id = "2",
                name = "Banff National Park",
                location = "Alberta, Canada",
                description = "Scenic campsite right near the lake with easy hiking trail access and full picnic facilities.",
                amenities = listOf("Fire Pit", "Picnic Table", "WiFi", "Electricity"),
                maxOccupancy = 4,
                price = 60.0,
                availableDate = "2026-06-01",
                rating = 4.9,
                hasActiveLottery = true
            ),
            Campsite(
                id = "3",
                name = "Elk Island National Park",
                location = "Alberta, Canada",
                description = "Quiet woodland campground perfect for stargazing, wildlife viewing, and canoeing.",
                amenities = listOf("Fire Pit", "Canoe Rental", "Picnic Table"),
                maxOccupancy = 2,
                price = 40.0,
                availableDate = "2026-08-10",
                rating = 4.3,
                hasActiveLottery = false
            )
        )
    }
    // List displayed on screen
    val displayList = remember {
        mutableStateListOf<Campsite>().apply { addAll(allCampsites) }
    }
    //Controls whether the filter dialog is visible
    var showFilterSheet by remember { mutableStateOf(false) }
    // State to track selected campsite for detailed view
    var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }

    //state for the mini searchbars/inputs inside the filter screen
    var provinceFilter by remember { mutableStateOf("") }
    var dateFilter by remember { mutableStateOf("") }
    var guestsFilter by remember { mutableStateOf("") }
    // Show detail view if a campsite is selected
    selectedCampsite?.let { campsite ->
        CampsiteDetailScreen(
            campsite = campsite,
            onBackClick = { selectedCampsite = null }
        )
        return
    }

    //helper function to filter campsites based on all active criteria
    fun applyFilters() {
        displayList.clear()
        val filtered = allCampsites.filter { campsite ->
            val matchesSearch = search.isBlank() ||
                    campsite.name.contains(search, ignoreCase = true) ||
                    campsite.location.contains(search, ignoreCase = true)

            val matchesProvince = provinceFilter.isBlank() ||
                    campsite.location.contains(provinceFilter, ignoreCase = true)

            val matchesDate = dateFilter.isBlank() ||
                    campsite.availableDate.contains(dateFilter, ignoreCase = true)

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

        // Back button to return to previous screen
        Button(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier.testTag("backButton")
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title text
        Text(
            text = "Campsite Search",
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Text field, Search button, and Filter button
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

        // List to display campsites
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
    // Filter Dialog (Screen with mini searchbars)
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
                        modifier = Modifier.fillMaxWidth().testTag("filterProvinceInput")
                    )
                    TextField(
                        value = dateFilter,
                        onValueChange = { dateFilter = it },
                        label = { Text("Booking Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth().testTag("filterDateInput")
                    )
                    TextField(
                        value = guestsFilter,
                        onValueChange = { guestsFilter = it },
                        label = { Text("Number of Guests") },
                        modifier = Modifier.fillMaxWidth().testTag("filterGuestsInput")
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
/**
 * Role/Pattern: Composable UI Screen (View) for displaying detailed campsite information.
 * Renders full campsite attributes including description, amenities, price, dates, and ratings.
 */
@Composable
fun CampsiteDetailScreen(
    campsite: Campsite,
    onBackClick: () -> Unit
) {
    // State to track if the user joined the lottery during this session
    var hasJoinedLottery by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Back button to return to search list
        Button(
            onClick = onBackClick,
            modifier = Modifier.testTag("detailBackButton")
        ) {
            Text("Back to Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campsite Name
        Text(
            text = campsite.name,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Location
        Text(
            text = "Location: ${campsite.location}",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rating and Price row
        Text(
            text = "Rating: ${campsite.rating} / 5.0          •$${campsite.price} / night",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Capacity and Available Date
        Text(
            text = "Max Occupancy: ${campsite.maxOccupancy}          •Available: ${campsite.availableDate}",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description Section
        Text(
            text = "About this Campsite",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = campsite.description,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Amenities Section
        Text(
            text = "Amenities",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        campsite.amenities.forEach { amenity ->
            Text(
                text = "• $amenity",
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lottery Action Section
        if (campsite.hasActiveLottery) {
            Button(
                onClick = { hasJoinedLottery = true },
                enabled = !hasJoinedLottery,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("joinLotteryButton")
            ) {
                Text(if (hasJoinedLottery) "Joined Lottery!" else "Join Live Lottery")
            }

            if (hasJoinedLottery) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ You have successfully joined the lottery for ${campsite.name}!",
                    fontSize = 14.sp,
                    modifier = Modifier.testTag("lotteryConfirmationText")
                )
            }
        } else {
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("noLotteryButton")
            ) {
                Text("No Active Lottery")
            }
        }
    }
}
