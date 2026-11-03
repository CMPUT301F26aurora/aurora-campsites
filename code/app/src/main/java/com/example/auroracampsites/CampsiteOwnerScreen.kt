package com.example.auroracampsites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Campsite owner screens for viewing and editing campsites.
 * Firestore data and QR code generation still need to be connected.
 */
private data class Campsite(
    val name: String,
    val location: String,
    val description: String,
    val occupancy: String,
    val mapLocation: String,
    val tags: String
)

@Composable
fun CampsiteSearchScreen(modifier: Modifier = Modifier) {
    val campsites = remember {
        mutableStateListOf(
            Campsite(
                "Pine Creek Campground",
                "Hinton, Alberta",
                "Cookhouse, tennis court",
                "8",
                "Hinton, AB",
                "Family, outdoor"
            ),
            Campsite(
                "Lakeview Campsite",
                "Banff, Alberta",
                "",
                "6",
                "Banff, AB",
                "Lake, quiet"
            )
        )
    }

    var currentPage by remember { mutableStateOf("list") }
    var selectedIndex by remember { mutableStateOf(0) }

    var campsiteName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var occupancy by remember { mutableStateOf("") }
    var mapLocation by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    val selectedCampsite = campsites[selectedIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "AURORA CAMPSITES",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp)
        )

        when (currentPage) {
            "list" -> {
                ScreenTitle("My Campsites")

                Spacer(modifier = Modifier.height(16.dp))

                campsites.forEachIndexed { index, campsite ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = campsite.name,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Location: ${campsite.location}")
                        Text("Maximum Occupancy: ${campsite.occupancy}")
                        Text("Tags: ${campsite.tags}")

                        Button(
                            onClick = {
                                selectedIndex = index
                                currentPage = "details"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Details")
                        }
                    }
                }
            }

            "details" -> {
                ScreenTitle("Campsite Details")

                CampsiteInfo("Campsite Name", selectedCampsite.name)
                CampsiteInfo("Location", selectedCampsite.location)
                CampsiteInfo(
                    "Description / Amenities",
                    selectedCampsite.description
                )
                CampsiteInfo(
                    "Maximum Occupancy",
                    selectedCampsite.occupancy
                )
                CampsiteInfo("Map Location", selectedCampsite.mapLocation)
                CampsiteInfo("Tags", selectedCampsite.tags)

                Button(
                    onClick = {
                        campsiteName = selectedCampsite.name
                        location = selectedCampsite.location
                        description = selectedCampsite.description
                        occupancy = selectedCampsite.occupancy
                        mapLocation = selectedCampsite.mapLocation
                        tags = selectedCampsite.tags
                        currentPage = "edit"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Campsite")
                }

                TextButton(
                    onClick = { currentPage = "qr" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View QR Code")
                }

                TextButton(
                    onClick = { currentPage = "list" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to My Campsites")
                }
            }

            "edit" -> {
                ScreenTitle("Edit Campsite")

                CampsiteInput("Campsite Name", campsiteName) {
                    campsiteName = it
                }

                CampsiteInput("Location", location) {
                    location = it
                }

                CampsiteInput("Description / Amenities", description) {
                    description = it
                }

                CampsiteInput("Maximum Occupancy", occupancy) {
                    occupancy = it
                }

                CampsiteInput("Map Location", mapLocation) {
                    mapLocation = it
                }

                CampsiteInput("Tags", tags) {
                    tags = it
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        campsites[selectedIndex] = Campsite(
                            campsiteName,
                            location,
                            description,
                            occupancy,
                            mapLocation,
                            tags
                        )
                        currentPage = "saved"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Changes")
                }
            }

            "saved" -> {
                ScreenTitle("Changes Saved Successfully!")

                CampsiteInfo("Campsite Name", selectedCampsite.name)
                CampsiteInfo("Location", selectedCampsite.location)
                CampsiteInfo(
                    "Description / Amenities",
                    selectedCampsite.description
                )
                CampsiteInfo(
                    "Maximum Occupancy",
                    selectedCampsite.occupancy
                )
                CampsiteInfo("Map Location", selectedCampsite.mapLocation)
                CampsiteInfo("Tags", selectedCampsite.tags)

                Button(
                    onClick = { currentPage = "details" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Campsite Details")
                }
            }

            "qr" -> {
                ScreenTitle("Campsite QR Code")

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "QR Code",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "QR code generation is not available yet.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { currentPage = "details" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Campsite Details")
                }
            }
        }
    }
}

@Composable
private fun ScreenTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun CampsiteInfo(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun CampsiteInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}
