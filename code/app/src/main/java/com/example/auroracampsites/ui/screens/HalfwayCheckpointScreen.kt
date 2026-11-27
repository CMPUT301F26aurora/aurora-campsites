package com.example.auroracampsites.ui.screens

import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import com.example.auroracampsites.data.session.UserSession
import com.example.auroracampsites.data.models.UserRole
import kotlinx.coroutines.CancellationException
import com.example.auroracampsites.data.models.Lottery
import com.example.auroracampsites.data.repository.LotteryRepository
import com.example.auroracampsites.data.models.Campsite
import com.example.auroracampsites.data.models.User
import com.example.auroracampsites.data.repository.CampsiteRepository
import com.example.auroracampsites.data.repository.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    userRepository: UserRepository = remember { UserRepository() },
    mode: String = "legacy"
) {
    val coroutineScope = rememberCoroutineScope()
    val lotteryRepository = remember { LotteryRepository() }
    var creatingLottery by remember { mutableStateOf(false) }
    var drawingLottery by remember { mutableStateOf(false) }
    var drawMessage by remember { mutableStateOf("") }
    var createdLottery by remember { mutableStateOf<Lottery?>(null) }

    // Real-time and fetched data from Firestore
    var campsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    var profiles by remember { mutableStateOf<List<User>>(emptyList()) }

    val context = LocalContext.current
    val currentOwnerId = remember(context) { UserSession.getCurrentUserId(context) }
    var currentUser by remember(currentOwnerId) { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(true) }
    var accessError by remember { mutableStateOf("") }
    var retry by remember { mutableStateOf(0) }
    val isAdmin = currentUser?.role == UserRole.ADMIN
    LaunchedEffect(currentOwnerId, retry) {
        loading = true
        currentUser = null
        profiles = emptyList()
        campsites = emptyList()
        accessError = ""
        try {
            val user = userRepository.getOrCreateUser(currentOwnerId)
            currentUser = user
            if (mode != "owner" && user.role == UserRole.ADMIN) profiles = userRepository.getAllUsers()
            loading = false
            if (mode == "admin" && user.role != UserRole.ADMIN) return@LaunchedEffect
            val flow = if (mode != "owner" && user.role == UserRole.ADMIN) campsiteRepository.getCampsitesRealtime()
            else campsiteRepository.getOwnerCampsitesRealtime(currentOwnerId)
            flow.collectLatest { campsites = it }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            currentUser = null
            profiles = emptyList()
            campsites = emptyList()
            accessError = "Unable to load management data."
            loading = false
        }
    }

    // Stores which page is visible: home, owner, lottery, created, adminCampsites, adminProfiles.
    var page by remember(mode) { mutableStateOf(if (mode == "owner") "owner" else "home") }

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
    val selectedCampsite = campsites.firstOrNull { it.id == selectedCampsiteId && it.ownerID == currentOwnerId }

    if (loading || currentUser == null) {
        Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
            Text(if (loading) "Loading profile..." else accessError)
            if (!loading) Button(onClick = { retry++ }) { Text("Retry") }
            TextButton(onClick = { navController.popBackStack() }) { Text("Back") }
        }
        return
    }

    if (mode == "admin" && !isAdmin) {
        Column(modifier = modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DemoHeading("Admin")
            Text("Access denied. Administrator permission is required.")
            DemoBackButton("Back to Home") { navController.popBackStack() }
        }
        return
    }

    // Confirmation dialog before removing a campsite (US 03.01.01).
    if (isAdmin && campsiteToDelete != null) {
        AlertDialog(
            onDismissRequest = { campsiteToDelete = null },
            title = { Text("Remove campsite?") },
            text = { Text("This will hide the campsite from listings while preserving historical records.") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        try {
                            val target = requireNotNull(campsiteToDelete)
                            campsiteRepository.deleteCampsite(target, currentOwnerId, admin = true)
                            accessError = ""
                        } catch (e: CancellationException) { throw e }
                        catch (e: Exception) { accessError = e.message ?: "Unable to remove campsite." }
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
    if (isAdmin && profileToDelete != null) {
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = { Text("Remove profile?") },
            text = { Text("This will permanently delete the profile from Firestore.") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        profileToDelete?.let { user ->
                            try {
                                check(isAdmin && userRepository.getUserById(currentOwnerId)?.role == UserRole.ADMIN)
                                userRepository.deleteProfile(user.id)
                                profiles = profiles.filter { it.id != user.id }
                            } catch (e: CancellationException) { throw e }
                            catch (e: Exception) { accessError = "Unable to remove profile." }
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

        if (accessError.isNotBlank()) Text(accessError, color = MaterialTheme.colorScheme.error)
        when (if (!isAdmin && page.startsWith("admin")) "home" else page) {
            "home" -> {
                DemoHeading(if (mode == "admin") "Admin" else "Campsite Owner Lottery and Admin Editing")
                if (mode != "admin") Text("Manage your campsites")
                DemoBackButton("Back to Home") { navController.popBackStack() }
                if (mode != "admin") Button(onClick = { page = "owner" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Campsite Owner")
                }
                if (isAdmin) Button(onClick = { page = "adminCampsites" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Manage Campsites")
                }
                if (isAdmin) Button(onClick = { page = "adminProfiles" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Manage User Profiles")
                }
            }

            "owner" -> {
                // US 02.03.01: show campsites belonging to the owner.
                DemoHeading(if (mode == "owner") "Manage Lotteries" else "My Campsites")
                val myCampsites = campsites.filter { it.ownerID == currentOwnerId }
                if (myCampsites.isEmpty()) {
                    Text("No campsites found in database.")
                }
                if (drawMessage.isNotBlank()) Text(drawMessage)
                myCampsites.forEach { campsite ->
                    DemoItemTitle(campsite.name)
                    Text("Location: ${campsite.location}")
                    if (campsite.activeLotteryId.isBlank()) {
                        Button(
                            onClick = {
                                selectedCampsiteId = campsite.id
                                formError = ""
                                page = "lottery"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Create Lottery") }
                    } else {
                        TextButton(onClick = {
                            val campsiteId = Uri.encode(campsite.id)
                            val lotteryId = Uri.encode(campsite.activeLotteryId)
                            navController.navigate("owner_waitlist/$campsiteId/$lotteryId")
                        }) { Text("View Waitlist") }
                        var round by remember(campsite.activeLotteryId) { mutableStateOf<Lottery?>(null) }
                        var roundError by remember(campsite.activeLotteryId) { mutableStateOf("") }
                        var refreshRound by remember(campsite.activeLotteryId) { mutableStateOf(0) }
                        LaunchedEffect(campsite.activeLotteryId, refreshRound) {
                            try {
                                roundError = ""
                                round = lotteryRepository.getLotteryById(campsite.activeLotteryId)
                                if (round == null) roundError = "Lottery not found."
                            } catch (e: CancellationException) { throw e }
                            catch (e: Exception) { roundError = e.message ?: "Unable to load lottery." }
                        }
                        val active = round
                        if (roundError.isNotBlank()) Text(roundError)
                        if (active == null) {
                            if (roundError.isBlank()) Text("Loading lottery...")
                        } else {
                            val today = java.time.LocalDate.now()
                            val end = runCatching { java.time.LocalDate.parse(active.bookingEnd) }.getOrNull()
                            val registrationEndDate = runCatching { java.time.LocalDate.parse(active.registrationEnd) }.getOrNull()
                            val expired = end != null && today.isAfter(end)
                            Text("Lottery status: ${active.status}")
                            Text("Booking ends: ${active.bookingEnd}")
                            val canDraw = active.status == "REGISTRATION" && !expired && end != null &&
                                    registrationEndDate != null && today.isAfter(registrationEndDate)
                            val canClose = expired && active.status in setOf("REGISTRATION", "DRAWN")
                            if (canDraw || canClose) {
                                Button(enabled = !drawingLottery, onClick = {
                                    drawingLottery = true
                                    drawMessage = ""
                                    coroutineScope.launch {
                                        try {
                                            if (canClose) {
                                                lotteryRepository.closeLottery(campsite.id, currentOwnerId, active.id)
                                                drawMessage = "Lottery closed. You can create a new lottery."
                                            } else {
                                                val count = lotteryRepository.drawLottery(campsite.id, currentOwnerId, active.id)
                                                drawMessage = "Lottery drawn: $count winner(s)."
                                            }
                                            refreshRound++
                                        } catch (e: CancellationException) { throw e }
                                        catch (e: Exception) { drawMessage = e.message ?: "Unable to update lottery." }
                                        finally { drawingLottery = false }
                                    }
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text(if (drawingLottery) "Working..." else if (canClose) "Close Lottery" else "Draw Lottery")
                                }
                            } else if (active.status == "DRAWN") {
                                Text("Booking period must end before closing this lottery.")
                            } else if (active.status == "REGISTRATION") {
                                Text("Registration must end before drawing this lottery.")
                            }
                        }
                        TextButton(onClick = { refreshRound++ }, enabled = !drawingLottery) { Text("Refresh Lottery") }
                    }
                    HorizontalDivider()
                }
                DemoBackButton(if (mode == "owner") "Back to Campsite Owner" else "Back to Home") {
                    if (mode == "owner") navController.popBackStack() else page = "home"
                }
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
                        val winners = winnersText.trim().toIntOrNull()
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
                            val configuration = Lottery(
                                campsiteId = selectedCampsite.id,
                                ownerId = currentOwnerId,
                                registrationStart = registrationStart,
                                registrationEnd = registrationEnd,
                                bookingStart = bookingStart,
                                bookingEnd = bookingEnd,
                                numberOfWinners = winners
                            )
                            creatingLottery = true
                            coroutineScope.launch {
                                try {
                                    val id = lotteryRepository.createLottery(configuration)
                                    createdLottery = configuration.copy(id = id)
                                    page = "created"
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    formError = e.message ?: "Unable to create lottery."
                                } finally {
                                    creatingLottery = false
                                }
                            }
                        }
                    },
                    enabled = !creatingLottery,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (creatingLottery) "Saving..." else "Create Lottery") }
                if (!creatingLottery) DemoBackButton("Back to My Campsites") { page = "owner" }
            }

            "created" -> {
                DemoHeading("Lottery Created")
                Text("The lottery has been saved to Firebase Firestore.")
                createdLottery?.let { lottery ->
                    Text("Lottery ID: ${lottery.id}")
                    Text("Campsite ID: ${lottery.campsiteId}")
                    Text("Registration: ${lottery.registrationStart} to ${lottery.registrationEnd}")
                    Text("Booking: ${lottery.bookingStart} to ${lottery.bookingEnd}")
                    Text("Number of winners: ${lottery.numberOfWinners}")
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
