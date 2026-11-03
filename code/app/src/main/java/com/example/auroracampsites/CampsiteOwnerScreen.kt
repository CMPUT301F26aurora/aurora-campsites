
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
 * Displays campsite owner screens using sample data.
 * Firestore and QR code generation are not connected yet.
 */
private data class OwnerCampsite(
    val name: String,
    val location: String,
    val description: String,
    val occupancy: String,
    val mapLocation: String,
    val tags: String
)

@Composable
fun CampsiteOwnerScreen(modifier: Modifier = Modifier) {
    val campsites = remember {
        mutableStateListOf(
            OwnerCampsite(
                "Pine Creek Campground",
                "Hinton, Alberta",
                "Cookhouse, tennis court",
                "8",
                "Hinton, AB",
                "Family, outdoor"
            ),
            OwnerCampsite(
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
                OwnerTitle("My Campsites")

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
                OwnerTitle("Campsite Details")

                OwnerInfo("Campsite Name", selectedCampsite.name)
                OwnerInfo("Location", selectedCampsite.location)
                OwnerInfo(
                    "Description / Amenities",
                    selectedCampsite.description
                )
                OwnerInfo(
                    "Maximum Occupancy",
                    selectedCampsite.occupancy
                )
                OwnerInfo("Map Location", selectedCampsite.mapLocation)
                OwnerInfo("Tags", selectedCampsite.tags)

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
                OwnerTitle("Edit Campsite")

                OwnerInput("Campsite Name", campsiteName) {
                    campsiteName = it
                }

                OwnerInput("Location", location) {
                    location = it
                }

                OwnerInput("Description / Amenities", description) {
                    description = it
                }

                OwnerInput("Maximum Occupancy", occupancy) {
                    occupancy = it
                }

                OwnerInput("Map Location", mapLocation) {
                    mapLocation = it
                }

                OwnerInput("Tags", tags) {
                    tags = it
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        campsites[selectedIndex] = OwnerCampsite(
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
                OwnerTitle("Changes Saved Successfully!")

                OwnerInfo("Campsite Name", selectedCampsite.name)
                OwnerInfo("Location", selectedCampsite.location)
                OwnerInfo(
                    "Description / Amenities",
                    selectedCampsite.description
                )
                OwnerInfo(
                    "Maximum Occupancy",
                    selectedCampsite.occupancy
                )
                OwnerInfo("Map Location", selectedCampsite.mapLocation)
                OwnerInfo("Tags", selectedCampsite.tags)

                Button(
                    onClick = { currentPage = "details" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Campsite Details")
                }
            }

            "qr" -> {
                OwnerTitle("Campsite QR Code")

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
private fun OwnerTitle(title: String) {
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
private fun OwnerInfo(label: String, value: String) {
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
private fun OwnerInput(
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
