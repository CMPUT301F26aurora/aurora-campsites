/*
 * Related User Stories:
 * US 02.03.01 - View my campsites
 * US 02.04.03 - View campsite details
 * US 02.01.03 - Edit campsite information
 * US 03.01.01 - Admin/Owner removal of registered campsites
 */

package com.example.auroracampsites.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.auroracampsites.data.session.UserSession
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.auroracampsites.data.models.Campsite
import com.example.auroracampsites.data.repository.CampsiteRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CancellationException
import java.time.LocalDate

@Composable
fun CampsiteOwnerScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    repository: CampsiteRepository = remember { CampsiteRepository() }
) {
    val coroutineScope = rememberCoroutineScope()
    var campsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }

    val context = LocalContext.current
    val ownerId = remember(context) { UserSession.getCurrentUserId(context) }
    var error by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    // Real-time synchronization with Firestore
    LaunchedEffect(ownerId) {
        try {
            repository.getOwnerCampsitesRealtime(ownerId).collect { campsites = it }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { error = e.message ?: "Unable to load campsites." }
    }

    var currentPage by remember { mutableStateOf("list") }
    var selectedId by remember { mutableStateOf("") }

    // Form fields for editing
    var campsiteName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var occupancy by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var availableDate by remember { mutableStateOf("") }
    var cancellationPolicy by remember { mutableStateOf("") }
    var rules by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    val selectedCampsite = campsites.firstOrNull { it.id == selectedId }

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

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        when (currentPage) {
            "list" -> {
                OwnerTitle("My Campsites")
                Spacer(modifier = Modifier.height(16.dp))

                if (campsites.isEmpty()) {
                    Text(
                        text = "No campsites found. Register one to get started!",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    campsites.forEach { campsite ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = campsite.name, fontWeight = FontWeight.Bold)
                            Text("Location: ${campsite.location}")
                            Text("Maximum Occupancy: ${campsite.maxOccupancy}")
                            Text("Price: \$${campsite.price} / night")
                            Text("Tags: ${campsite.amenities.joinToString(", ")}")

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        selectedId = campsite.id
                                        currentPage = "details"
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("View Details")
                                }

                                // Deletes the campsite from Firestore using the repository
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            busy = true
                                            error = ""
                                            try { repository.deleteCampsite(campsite, ownerId) }
                                            catch (e: CancellationException) { throw e }
                                            catch (e: Exception) { error = e.message ?: "Unable to remove campsite." }
                                            finally { busy = false }
                                        }
                                    },
                                    enabled = !busy,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }

            "details" -> {
                if (selectedCampsite != null) {
                    OwnerTitle("Campsite Details")

                    OwnerInfo("Campsite Name", selectedCampsite.name)
                    OwnerInfo("Location", selectedCampsite.location)
                    OwnerInfo("Description / Amenities", selectedCampsite.description)
                    OwnerInfo("Maximum Occupancy", selectedCampsite.maxOccupancy.toString())
                    OwnerInfo("Price ($ / Night)", selectedCampsite.price.toString())
                    OwnerInfo(
                        label = "Available Dates",
                        value = selectedCampsite.availableDates.joinToString(", ")
                    )
                    OwnerInfo("Cancellation Policy", selectedCampsite.cancellationPolicy)
                    OwnerInfo("Campsite Rules", selectedCampsite.rules)
                    OwnerInfo("Tags", selectedCampsite.amenities.joinToString(", "))

                    Button(
                        onClick = {
                            campsiteName = selectedCampsite.name
                            location = selectedCampsite.location
                            description = selectedCampsite.description
                            occupancy = selectedCampsite.maxOccupancy.toString()
                            price = selectedCampsite.price.toString()
                            availableDate = selectedCampsite.availableDates.joinToString(", ")
                            cancellationPolicy = selectedCampsite.cancellationPolicy
                            rules = selectedCampsite.rules
                            tags = selectedCampsite.amenities.joinToString(", ")
                            currentPage = "edit"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Campsite")
                    }

                    TextButton(
                        onClick = { currentPage = "list" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to My Campsites")
                    }
                } else {
                    LaunchedEffect(Unit) { currentPage = "list" }
                }
            }

            "edit" -> {
                if (selectedCampsite != null) {
                    OwnerTitle("Edit Campsite")

                    OwnerInput("Campsite Name", campsiteName) { campsiteName = it }
                    OwnerInput("Location", location) { location = it }
                    OwnerInput("Description / Amenities", description) { description = it }
                    OwnerInput("Maximum Occupancy", occupancy) { occupancy = it }
                    OwnerInput("Price ($ / Night)", price) { price = it }
                    OwnerInput("Available Date", availableDate) { availableDate = it }
                    OwnerInput("Cancellation Policy", cancellationPolicy) { cancellationPolicy = it }
                    OwnerInput("Campsite Rules", rules) { rules = it }
                    OwnerInput("Tags", tags) { tags = it }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val count = occupancy.toIntOrNull()
                            val amount = price.toDoubleOrNull()
                            val dates = availableDate.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
                            if (campsiteName.isBlank() || location.isBlank() || description.isBlank() || count == null || count <= 0 || amount == null || !amount.isFinite() || amount < 0 || dates.isEmpty() || dates.any { runCatching { LocalDate.parse(it).toString() != it }.getOrDefault(true) }) {
                                error = "Enter valid campsite information and dates (YYYY-MM-DD)."
                                return@Button
                            }
                            val updatedCampsite = selectedCampsite.copy(
                                name = campsiteName,
                                location = location,
                                description = description,
                                maxOccupancy = count,
                                price = amount,
                                availableDates = dates,
                                cancellationPolicy = cancellationPolicy,
                                rules = rules,
                                amenities = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            )
                            coroutineScope.launch {
                                busy = true
                                error = ""
                                try {
                                    repository.updateOwnerCampsite(ownerId, updatedCampsite)
                                    currentPage = "saved"
                                } catch (e: CancellationException) { throw e }
                                catch (e: Exception) { error = e.message ?: "Unable to save changes." }
                                finally { busy = false }
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Changes")
                    }

                    TextButton(
                        onClick = { currentPage = "details" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }

            "saved" -> {
                if (selectedCampsite != null) {
                    OwnerTitle("Changes Saved Successfully!")

                    OwnerInfo("Campsite Name", selectedCampsite.name)
                    OwnerInfo("Location", selectedCampsite.location)
                    OwnerInfo("Description / Amenities", selectedCampsite.description)
                    OwnerInfo("Maximum Occupancy", selectedCampsite.maxOccupancy.toString())
                    OwnerInfo("Price ($ / Night)", selectedCampsite.price.toString())
                    OwnerInfo(label = "Available Dates", value = selectedCampsite.availableDates.joinToString(", "))
                    OwnerInfo("Cancellation Policy", selectedCampsite.cancellationPolicy)
                    OwnerInfo("Campsite Rules", selectedCampsite.rules)
                    OwnerInfo("Tags", selectedCampsite.amenities.joinToString(", "))

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
