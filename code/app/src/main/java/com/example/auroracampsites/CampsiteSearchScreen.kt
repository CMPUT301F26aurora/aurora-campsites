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

/**
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

    //Master list populated from data source (currently empty until connected to Firestore)
    val allCampsites = remember { listOf<Campsite>() }

    //List displayed on screen
    val displayList = remember {
        mutableStateListOf<Campsite>().apply { addAll(allCampsites) }
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

        // Text field and search button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = search,
                onValueChange = {
                    search = it
                },
                label = {
                    Text("Search Campsites")
                },
                modifier = Modifier.testTag("searchInput")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    displayList.clear()
                    if (search.isBlank()) {
                        displayList.addAll(allCampsites)
                    } else {
                        displayList.addAll(
                            allCampsites.filter { campsite ->
                                campsite.name.contains(search, ignoreCase = true) ||
                                        campsite.location.contains(search, ignoreCase = true)
                            }
                        )
                    }
                },
                modifier = Modifier.testTag("searchButton")
            ) {
                Text("Search")
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = campsite.name,
                            fontSize = 20.sp
                        )
                        Text(
                            text = campsite.location,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
