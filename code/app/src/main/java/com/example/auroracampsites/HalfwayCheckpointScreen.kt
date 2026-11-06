
package com.example.auroracampsites // Use the same package as the team's Android app.

import androidx.compose.foundation.layout.Arrangement // Adds spacing between items.
import androidx.compose.foundation.layout.Column // Places UI elements vertically.
import androidx.compose.foundation.layout.Row // Places buttons next to each other.
import androidx.compose.foundation.layout.Spacer // Adds empty space in the layout.
import androidx.compose.foundation.layout.fillMaxSize // Uses the full available screen.
import androidx.compose.foundation.layout.fillMaxWidth // Makes elements as wide as the screen.
import androidx.compose.foundation.layout.height // Controls vertical spacing.
import androidx.compose.foundation.layout.padding // Adds space around elements.
import androidx.compose.foundation.rememberScrollState // Remembers the scroll position.
import androidx.compose.foundation.verticalScroll // Lets long pages scroll.
import androidx.compose.material3.AlertDialog // Asks for confirmation before deleting.
import androidx.compose.material3.Button // Displays main action buttons.
import androidx.compose.material3.HorizontalDivider // Separates sections.
import androidx.compose.material3.OutlinedTextField // Displays editable form fields.
import androidx.compose.material3.Text // Displays text on screen.
import androidx.compose.material3.TextButton // Displays secondary action buttons.
import androidx.compose.runtime.Composable // Marks a function that draws UI.
import androidx.compose.runtime.getValue // Reads Compose state using 'by'.
import androidx.compose.runtime.mutableStateListOf // Stores lists that update the UI.
import androidx.compose.runtime.mutableStateOf // Stores individual UI values.
import androidx.compose.runtime.remember // Keeps values while the screen recomposes.
import androidx.compose.runtime.setValue // Updates Compose state using 'by'.
import androidx.compose.ui.Modifier // Controls the size and spacing of UI elements.
import androidx.compose.ui.text.font.FontWeight // Makes headings bold.
import androidx.compose.ui.text.style.TextAlign // Centers headings.
import androidx.compose.ui.unit.dp // Uses density-independent spacing.

/*
 * CMPUT 301 Project Part 3 - Halfway checkpoint frontend.
 *
 * Covered user stories:
 * US 02.03.01 - View my campsites.
 * US 02.04.01 - List my campsite in a lottery for a certain period.
 * US 02.04.03 - Set the lottery registration period.
 * US 02.04.04 - Set the lottery booking period.
 * US 02.04.05 - Set the number of lottery winners.
 * US 03.01.01 - Admin removes registered campsites.
 * US 03.02.01 - Admin removes profiles.
 * US 03.04.01 - Admin browses campsites.
 * US 03.05.01 - Admin browses profiles.
 *
 * FRONTEND DEMO ONLY:
 * The sample data below is stored in memory, not in Firebase.
 * The backend team will need to connect these actions to the repository.
 * No sign-in or administrator authorization is implemented here.
 */

// Small frontend model for showing a campsite in a list.
// This name is different from the team's existing Campsite class.
private data class DemoCampsite(
    val id: String, // Identifier to pass to the backend later.
    val name: String, // Campsite name displayed to the user.
    val location: String, // Campsite location displayed to the user.
    val ownerId: String // Owner identifier for the My Campsites filter.
)

// Small frontend model for displaying a user profile in the admin list.
private data class DemoProfile(
    val id: String, // Identifier for deleting or opening the profile later.
    val displayName: String, // Name shown in the list.
    val role: String // Role shown to the admin.
)

// Holds the lottery form values after the Create Lottery button is pressed.
private data class DemoLottery(
    val campsiteId: String, // The campsite that is listed in the lottery.
    val registrationStart: String, // Registration starting date.
    val registrationEnd: String, // Registration closing date.
    val bookingStart: String, // First date of the booking period.
    val bookingEnd: String, // Last date of the booking period.
    val winners: Int // Maximum number of winners selected by the owner.
)

/**
 * Shows a simple menu, campsite owner pages, and admin pages.
 * Call this composable from MainActivity to test the nine frontend stories.
 * All actions currently change local demo state only.
 */
@Composable
fun HalfwayCheckpointScreen(modifier: Modifier = Modifier) {
    // A fixed sample owner ID stands in for the signed-in account.
    // Backend: replace this with the real authenticated user ID.
    val currentOwnerId = "owner-001"

    // Sample campsites for the frontend demonstration.
    // Backend: load these from the campsite repository instead.
    val campsites = remember {
        mutableStateListOf(
            DemoCampsite("camp-001", "Pine Creek Campground", "Hinton, Alberta", "owner-001"),
            DemoCampsite("camp-002", "Lakeview Campsite", "Banff, Alberta", "owner-001"),
            DemoCampsite("camp-003", "Riverbend Campsite", "Edmonton, Alberta", "owner-002")
        )
    }

    // Sample profiles displayed by the admin pages.
    // Backend: load profiles from the correct collection or repository.
    val profiles = remember {
        mutableStateListOf(
            DemoProfile("user-001", "Entrant 001", "Entrant"),
            DemoProfile("user-002", "Entrant 002", "Entrant"),
            DemoProfile("owner-001", "Campsite Owner 001", "Campsite Owner")
        )
    }

    // Saves created lotteries in memory so the demo can show a result.
    // Backend: create the lottery in Firestore instead of only adding here.
    val lotteries = remember { mutableStateListOf<DemoLottery>() }

    // Stores which page is visible: home, owner, lottery, created, adminCampsites, adminProfiles.
    var page by remember { mutableStateOf("home") }

    // Stores the campsite currently selected for creating a lottery.
    var selectedCampsiteId by remember { mutableStateOf("camp-001") }

    // Text entered into the lottery form; the dates use YYYY-MM-DD.
    var registrationStart by remember { mutableStateOf("") }
    var registrationEnd by remember { mutableStateOf("") }
    var bookingStart by remember { mutableStateOf("") }
    var bookingEnd by remember { mutableStateOf("") }
    var winnersText by remember { mutableStateOf("") }

    // Shows validation messages below the lottery form.
    var formError by remember { mutableStateOf("") }

    // Stores a campsite ID when the admin requests a deletion.
    var campsiteToDelete by remember { mutableStateOf<String?>(null) }

    // Stores a profile ID when the admin requests a deletion.
    var profileToDelete by remember { mutableStateOf<String?>(null) }

    // Finds the campsite selected for the lottery form.
    val selectedCampsite = campsites.firstOrNull { it.id == selectedCampsiteId }

    // Show a confirmation dialog before removing any campsite.
    if (campsiteToDelete != null) {
        AlertDialog(
            onDismissRequest = { campsiteToDelete = null }, // Closes the dialog without changes.
            title = { Text("Remove campsite?") }, // Tells the admin what they are deleting.
            text = { Text("This will remove the campsite from this demo list.") },
            confirmButton = {
                TextButton(onClick = {
                    // Backend: ask the repository to delete this campsite by ID.
                    campsites.removeAll { it.id == campsiteToDelete }
                    campsiteToDelete = null // Closes the dialog after local removal.
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { campsiteToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Show a separate confirmation dialog before removing any profile.
    if (profileToDelete != null) {
        AlertDialog(
            onDismissRequest = { profileToDelete = null }, // Closes the dialog without changes.
            title = { Text("Remove profile?") },
            text = { Text("This will remove the profile from this demo list.") },
            confirmButton = {
                TextButton(onClick = {
                    // Backend: request profile deletion and check admin permissions.
                    profiles.removeAll { it.id == profileToDelete }
                    profileToDelete = null // Clears the selected profile after removal.
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Shared scrollable container for the whole prototype.
    Column(
        modifier = modifier
            .fillMaxSize() // Uses the screen space given by MainActivity.
            .verticalScroll(rememberScrollState()) // Allows scrolling on small devices.
            .padding(16.dp), // Keeps content away from the screen edges.
        verticalArrangement = Arrangement.spacedBy(10.dp) // Separates sections evenly.
    ) {
        Text(
            text = "AURORA CAMPSITES", // App name at the top of every page.
            modifier = Modifier.fillMaxWidth(), // Gives the title full width.
            textAlign = TextAlign.Center // Centers the title.
        )
        HorizontalDivider() // Separates the app header from page content.

        // Switch between simple frontend pages without adding a navigation library.
        when (page) {
            "home" -> {
                DemoHeading("Halfway Checkpoint") // Home page title.
                Text("Choose a section to test the frontend screens.")
                Button(onClick = { page = "owner" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Campsite Owner") // Opens the owner part of the demo.
                }
                Button(onClick = { page = "adminCampsites" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Admin - Campsites") // Opens the admin campsite list.
                }
                Button(onClick = { page = "adminProfiles" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Admin - Profiles") // Opens the admin profile list.
                }
            }

            "owner" -> {
                // US 02.03.01: show only campsites belonging to the sample owner.
                DemoHeading("My Campsites")
                val myCampsites = campsites.filter { it.ownerId == currentOwnerId }
                if (myCampsites.isEmpty()) {
                    Text("No campsites found.") // Empty state for owners without campsites.
                }
                myCampsites.forEach { campsite ->
                    // Show basic information for each owned campsite.
                    DemoItemTitle(campsite.name)
                    Text("Location: ${campsite.location}")
                    Button(
                        onClick = {
                            selectedCampsiteId = campsite.id // Remembers the selected campsite.
                            formError = "" // Clears any previous validation message.
                            page = "lottery" // Opens the lottery registration form.
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Create Lottery") } // US 02.04.01 entry point.
                    HorizontalDivider() // Separates campsite cards.
                }
                DemoBackButton("Back to Home") { page = "home" }
            }

            "lottery" -> {
                // US 02.04.01: list the selected campsite in a lottery.
                DemoHeading("Create Lottery")
                Text("Campsite: ${selectedCampsite?.name ?: "Not found"}")

                // US 02.04.03: two editable dates define the registration period.
                DemoHeading("Registration Period")
                DemoInput("Registration Start (YYYY-MM-DD)", registrationStart) {
                    registrationStart = it // Updates the registration start field.
                }
                DemoInput("Registration End (YYYY-MM-DD)", registrationEnd) {
                    registrationEnd = it // Updates the registration end field.
                }

                // US 02.04.04: two editable dates define the booking period.
                DemoHeading("Booking Period")
                DemoInput("Booking Start (YYYY-MM-DD)", bookingStart) {
                    bookingStart = it // Updates the booking start field.
                }
                DemoInput("Booking End (YYYY-MM-DD)", bookingEnd) {
                    bookingEnd = it // Updates the booking end field.
                }

                // US 02.04.05: owner chooses how many entrants can win.
                DemoHeading("Number of Winners")
                DemoInput("Winners", winnersText) {
                    winnersText = it // Updates the winners input.
                }

                // Shows the reason when the entered form values are incomplete.
                if (formError.isNotEmpty()) {
                    Text(formError) // A message is easier to understand than a silent failure.
                }

                // Creates a local lottery only when basic form checks pass.
                Button(
                    onClick = {
                        val winners = winnersText.toIntOrNull() // Converts the typed value to a number.
                        val dateFormat = Regex("\\d{4}-\\d{2}-\\d{2}") // Checks basic YYYY-MM-DD format.
                        val dates = listOf(registrationStart, registrationEnd, bookingStart, bookingEnd)
                        formError = when {
                            selectedCampsite == null -> "Please select a valid campsite."
                            dates.any { !dateFormat.matches(it) } -> "Enter all dates as YYYY-MM-DD."
                            registrationStart > registrationEnd -> "Registration end must not be before start."
                            bookingStart > bookingEnd -> "Booking end must not be before start."
                            winners == null || winners <= 0 -> "Enter a number of winners greater than zero."
                            else -> "" // Empty string means the basic checks passed.
                        }
                        if (formError.isEmpty() && selectedCampsite != null && winners != null) {
                            // Backend: replace this in-memory insertion with a Firestore create request.
                            lotteries.add(
                                DemoLottery(
                                    campsiteId = selectedCampsite.id,
                                    registrationStart = registrationStart,
                                    registrationEnd = registrationEnd,
                                    bookingStart = bookingStart,
                                    bookingEnd = bookingEnd,
                                    winners = winners
                                )
                            )
                            page = "created" // Shows the created lottery summary.
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Create Lottery") }
                DemoBackButton("Back to My Campsites") { page = "owner" }
            }

            "created" -> {
                // Confirmation page for the locally created lottery.
                DemoHeading("Lottery Created")
                Text("The lottery was created in the local demo only.")
                val latestLottery = lotteries.lastOrNull() // Shows the most recently created lottery.
                if (latestLottery != null) {
                    Text("Campsite ID: ${latestLottery.campsiteId}")
                    Text("Registration: ${latestLottery.registrationStart} to ${latestLottery.registrationEnd}")
                    Text("Booking: ${latestLottery.bookingStart} to ${latestLottery.bookingEnd}")
                    Text("Number of winners: ${latestLottery.winners}")
                }
                DemoBackButton("Back to My Campsites") { page = "owner" }
            }

            "adminCampsites" -> {
                // US 03.04.01: admin browses all registered campsites.
                DemoHeading("Admin - Browse Campsites")
                Text("Demo data only. Admin permissions are not connected yet.")
                if (campsites.isEmpty()) {
                    Text("No registered campsites.") // Empty state after removals.
                }
                campsites.forEach { campsite ->
                    DemoItemTitle(campsite.name) // Displays the campsite name.
                    Text("Location: ${campsite.location}") // Displays the campsite location.
                    Text("Campsite ID: ${campsite.id}") // Helps backend identify the record.
                    Text("Owner ID: ${campsite.ownerId}") // Shows ownership information.
                    Button(
                        onClick = {
                            // US 03.01.01: opens a confirmation before removing a campsite.
                            campsiteToDelete = campsite.id
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Remove Campsite") }
                    HorizontalDivider() // Separates items in the admin list.
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { page = "adminProfiles" }) { Text("Profiles") }
                    TextButton(onClick = { page = "home" }) { Text("Home") }
                }
            }

            "adminProfiles" -> {
                // US 03.05.01: admin browses registered user profiles.
                DemoHeading("Admin - Browse Profiles")
                Text("Demo data only. Admin permissions are not connected yet.")
                if (profiles.isEmpty()) {
                    Text("No profiles found.") // Empty state after removals.
                }
                profiles.forEach { profile ->
                    DemoItemTitle(profile.displayName) // Displays the profile name.
                    Text("Role: ${profile.role}") // Displays the user role.
                    Text("Profile ID: ${profile.id}") // Helps backend identify the record.
                    Button(
                        onClick = {
                            // US 03.02.01: opens a confirmation before removing a profile.
                            profileToDelete = profile.id
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Remove Profile") }
                    HorizontalDivider() // Separates items in the admin list.
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { page = "adminCampsites" }) { Text("Campsites") }
                    TextButton(onClick = { page = "home" }) { Text("Home") }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp)) // Leaves space at the bottom of the page.
    }
}

/** Draws a consistent bold title for a screen or form section. */
@Composable
private fun DemoHeading(title: String) {
    Text(
        text = title, // Text supplied by the page.
        fontWeight = FontWeight.Bold, // Makes the heading stand out.
        modifier = Modifier.fillMaxWidth(), // Uses the width of the page.
        textAlign = TextAlign.Center // Centers the heading.
    )
}

/** Draws a bold name for an item in a campsite or profile list. */
@Composable
private fun DemoItemTitle(title: String) {
    Text(text = title, fontWeight = FontWeight.Bold) // Displays the item's name.
}

/** Draws a text field and sends changed text back to its parent screen. */
@Composable
private fun DemoInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, // Current text held in Compose state.
        onValueChange = onValueChange, // Called whenever the user types.
        label = { Text(label) }, // Displays the field's label.
        singleLine = true, // Keeps dates and numbers on one line.
        modifier = Modifier.fillMaxWidth() // Makes forms easier to read on a phone.
    )
}

/** Draws a small button for returning to the previous page. */
@Composable
private fun DemoBackButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label) // Uses the destination label from the current page.
    }
}
