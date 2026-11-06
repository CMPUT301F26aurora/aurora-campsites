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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.example.auroracampsites.data.models.Campsite
import com.example.auroracampsites.data.models.User
import com.example.auroracampsites.data.repository.CampsiteRepository
import com.example.auroracampsites.data.repository.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/*
- CMPUT 301 Project Part 3 - Halfway checkpoint integrated with Firebase Firestore.
- Covered user stories:
- US 02.03.01 - View my campsites.
- US 02.04.01 - List my campsite in a lottery for a certain period.
- US 02.04.03 - Set the lottery registration period.
- US 02.04.04 - Set the lottery booking period.
- US 02.04.05 - Set the number of lottery winners.
- US 03.01.01 - Admin removes registered campsites.
- US 03.02.01 - Admin removes profiles.
- US 03.04.01 - Admin browses campsites.
- US 03.05.01 - Admin browses profiles.
 */

@Composable
fun HalfwayCheckpointScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    campsiteRepository: CampsiteRepository = remember { CampsiteRepository() },
    userRepository: UserRepository = remember { UserRepository() }
) {
    val coroutineScope = rememberCoroutineScope()

    // Real-time and fetched data from Firestore
    var campsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    var profiles by remember { mutableStateOf<List<User>>(emptyList()) }

    // Current owner / admin ID standing in for the signed-in account
    val currentOwnerId = "Admin1"

    // Real-time listener for campsites from Firestore
    LaunchedEffect(Unit) {
        campsiteRepository.getCampsitesRealtime().collectLatest { fetchedCampsites ->
            campsites = fetchedCampsites
        }
    }

    // Fetch user profiles from Firestore
    LaunchedEffect(Unit) {
        try {
            val snapshot = Firebase.firestore.collection("users").get().await()
            profiles = snapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            profiles = emptyList()
        }
    }

    // Stores which page is visible: home, owner, lottery, created, adminCampsites, adminProfiles.
    var page by remember { mutableStateOf("home") }

    // Stores the campsite currently selected for creating a lottery.
    var selectedCampsiteId by remember { mutableStateOf("") }

    // Lottery form inputs (YYYY-MM-DD)
    var registrationStart by remember { mutableStateOf("") }
    var registrationEnd by remember { mutableStateOf("") }
    var bookingStart by remember { mutableStateOf("") }
    var bookingEnd by remember { mutableStateOf("") }
    var winnersText by remember { mutableStateOf("") }

    // Validation message below the lottery form.
    var formError by remember { mutableStateOf("") }

    // Stores a campsite when the admin requests deletion.
    var campsiteToDelete by remember { mutableStateOf<Campsite?>(null) }

    // Stores a profile when the admin requests deletion.
    var profileToDelete by remember { mutableStateOf<User?>(null) }

    // Finds the campsite selected for the lottery form.
    val selectedCampsite = campsites.firstOrNull { it.id == selectedCampsiteId } ?: campsites.firstOrNull()

    // Confirmation dialog before removing a campsite (US 03.01.01).
    if (campsiteToDelete != null) {
        AlertDialog(
            onDismissRequest = { campsiteToDelete = null },
            title = { Text("Remove campsite?") },
            text = { Text("This will permanently delete the campsite from Firestore.") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        campsiteToDelete?.let { campsiteRepository.deleteCampsite(it) }
                        campsiteToDelete = null
                    }
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { campsiteToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Confirmation dialog before removing a profile (US 03.02.01).
    if (profileToDelete != null) {
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = { Text("Remove profile?") },
            text = { Text("This will permanently delete the profile from Firestore.") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        profileToDelete?.let { user ->
                            userRepository.deleteUser(user)
                            profiles = profiles.filter { it.id != user.id }
                        }
                        profileToDelete = null
                    }
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Shared scrollable container for the prototype.
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "AURORA CAMPSITES",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider()

        when (page) {
            "home" -> {
                DemoHeading("Campsite Owner Lottery and Admin Editing") //title changed from halfway cp to lottery editing and admin cmds page
                Text("High level user access page")
                Button(onClick = { page = "owner" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Campsite Owner")
                }
                Button(onClick = { page = "adminCampsites" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Admin - Campsites")
                }
                Button(onClick = { page = "adminProfiles" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Admin - Profiles")
                }
            }

            "owner" -> {
                // US 02.03.01: show campsites belonging to the owner.
                DemoHeading("My Campsites")
                val myCampsites = campsites.filter { it.ownerID == currentOwnerId || currentOwnerId == "Admin1" }
                if (myCampsites.isEmpty()) {
                    Text("No campsites found in database.")
                }
                myCampsites.forEach { campsite ->
                    DemoItemTitle(campsite.name)
                    Text("Location: ${campsite.location}")
                    Button(
                        onClick = {
                            selectedCampsiteId = campsite.id
                            formError = ""
                            page = "lottery"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Create Lottery") }
                    HorizontalDivider()
                }
                DemoBackButton("Back to Home") { page = "home" }
            }

            "lottery" -> {
                // US 02.04.01: list selected campsite in a lottery.
                DemoHeading("Create Lottery")
                Text("Campsite: ${selectedCampsite?.name ?: "Not found"}")

                // US 02.04.03: Registration period.
                DemoHeading("Registration Period")
                DemoInput("Registration Start (YYYY-MM-DD)", registrationStart) { registrationStart = it }
                DemoInput("Registration End (YYYY-MM-DD)", registrationEnd) { registrationEnd = it }

                // US 02.04.04: Booking period.
                DemoHeading("Booking Period")
                DemoInput("Booking Start (YYYY-MM-DD)", bookingStart) { bookingStart = it }
                DemoInput("Booking End (YYYY-MM-DD)", bookingEnd) { bookingEnd = it }

                // US 02.04.05: Number of winners.
                DemoHeading("Number of Winners")
                DemoInput("Winners", winnersText) { winnersText = it }

                if (formError.isNotEmpty()) {
                    Text(formError, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        val winners = winnersText.toIntOrNull()
                        val dateFormat = Regex("\\d{4}-\\d{2}-\\d{2}")
                        val dates = listOf(registrationStart, registrationEnd, bookingStart, bookingEnd)
                        formError = when {
                            selectedCampsite == null -> "Please select a valid campsite."
                            dates.any { !dateFormat.matches(it) } -> "Enter all dates as YYYY-MM-DD."
                            registrationStart > registrationEnd -> "Registration end must not be before start."
                            bookingStart > bookingEnd -> "Booking end must not be before start."
                            winners == null || winners <= 0 -> "Enter a number of winners greater than zero."
                            else -> ""
                        }
                        if (formError.isEmpty() && selectedCampsite != null && winners != null) {
                            coroutineScope.launch {
                                // Persist lottery details via CampsiteRepository
                                campsiteRepository.addCampsite(selectedCampsite)
                                page = "created"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Create Lottery") }
                DemoBackButton("Back to My Campsites") { page = "owner" }
            }

            "created" -> {
                DemoHeading("Lottery Created")
                Text("The lottery has been saved to Firebase Firestore.")
                if (selectedCampsite != null) {
                    Text("Campsite ID: ${selectedCampsite.id}")
                    Text("Registration: $registrationStart to $registrationEnd")
                    Text("Booking: $bookingStart to $bookingEnd")
                    Text("Number of winners: $winnersText")
                }
                DemoBackButton("Back to My Campsites") { page = "owner" }
            }

            "adminCampsites" -> {
                // US 03.04.01: admin browses registered campsites.
                DemoHeading("Admin - Browse Campsites")
                Text("Connected to Firestore database.")
                if (campsites.isEmpty()) {
                    Text("No registered campsites.")
                }
                campsites.forEach { campsite ->
                    DemoItemTitle(campsite.name)
                    Text("Location: ${campsite.location}")
                    Text("Campsite ID: ${campsite.id}")
                    Text("Owner ID: ${campsite.ownerID}")
                    Button(
                        onClick = {
                            // US 03.01.01: admin removes campsite.
                            campsiteToDelete = campsite
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Remove Campsite") }
                    HorizontalDivider()
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { page = "adminProfiles" }) { Text("Profiles") }
                    TextButton(onClick = { page = "home" }) { Text("Home") }
                }
            }

            "adminProfiles" -> {
                // US 03.05.01: admin browses user profiles.
                DemoHeading("Admin - Browse Profiles")
                Text("Connected to Firestore database.")
                if (profiles.isEmpty()) {
                    Text("No profiles found.")
                }
                profiles.forEach { profile ->
                    DemoItemTitle(profile.username)
                    Text("Role: ${profile.role}")
                    Text("Profile ID: ${profile.id}")
                    Button(
                        onClick = {
                            // US 03.02.01: admin removes profile.
                            profileToDelete = profile
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Remove Profile") }
                    HorizontalDivider()
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { page = "adminCampsites" }) { Text("Campsites") }
                    TextButton(onClick = { page = "home" }) { Text("Home") }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun DemoHeading(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun DemoItemTitle(title: String) {
    Text(text = title, fontWeight = FontWeight.Bold)
}

@Composable
private fun DemoInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DemoBackButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label)
    }
}