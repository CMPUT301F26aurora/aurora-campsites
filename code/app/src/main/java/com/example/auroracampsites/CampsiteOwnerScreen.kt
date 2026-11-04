
/*
 * Related User Stories:
 * US 02.03.01 - View my campsites
 * US 02.04.03 - View campsite details
 * US 02.01.03 - Edit campsite information
 * US 02.02.01 - View campsite QR code
 *
 * This file contains the campsite owner frontend screens.
 * The screens currently use sample data for testing.
 * Firestore integration and QR code generation are not implemented yet.
 */

package com.example.auroracampsites

// Layout components used to arrange the screens.
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

// Allows the page to scroll when the content is too long.
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// Material components used for buttons, text fields, and text.
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

// Compose state is used to update the screen when data changes.
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// Controls the layout, text formatting, and spacing.
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Stores the campsite information displayed in the owner screens.
 *
 * This is a temporary frontend model using sample data.
 * The backend team can later connect these fields to the
 * existing Campsite model and CampsiteRepository.
 *
 * @property name Name of the campsite.
 * @property location General location of the campsite.
 * @property description Description and available amenities.
 * @property occupancy Maximum number of people allowed.
 * @property mapLocation Location displayed in the map field.
 * @property tags Tags describing the campsite.
 */
private data class OwnerCampsite(
    val name: String,         // Campsite name
    val location: String,     // Campsite location
    val description: String,  // Campsite description and amenities
    val occupancy: String,    // Maximum occupancy
    val mapLocation: String,  // Map location
    val tags: String          // Campsite tags
)

/**
 * Main frontend screen for campsite owners.
 *
 * Handles the campsite list, details, editing, save confirmation,
 * and QR code placeholder screens.
 *
 * Navigation is handled locally using currentPage.
 * The campsite information is stored in Compose state for now.
 *
 * @param modifier Modifier provided by the parent layout.
 */
@Composable
fun CampsiteOwnerScreen(modifier: Modifier = Modifier) {

    // Temporary campsite data used before Firestore integration.
    // Replace this list with data retrieved from the repository later.
    val campsites = remember {
        mutableStateListOf(

            // First sample campsite.
            OwnerCampsite(
                "Pine Creek Campground",  // Name
                "Hinton, Alberta",        // Location
                "Cookhouse, tennis court",// Description
                "8",                      // Maximum occupancy
                "Hinton, AB",             // Map location
                "Family, outdoor"         // Tags
            ),

            // Second sample campsite.
            OwnerCampsite(
                "Lakeview Campsite",      // Name
                "Banff, Alberta",         // Location
                "",                      // Description
                "6",                     // Maximum occupancy
                "Banff, AB",             // Map location
                "Lake, quiet"            // Tags
            )
        )
    }

    // Tracks which screen the user is currently viewing.
    // Possible values: list, details, edit, saved, and qr.
    var currentPage by remember { mutableStateOf("list") }

    // Stores the index of the campsite selected from the list.
    var selectedIndex by remember { mutableStateOf(0) }

    // Stores the current values entered in the edit form.
    // These values are separate from the campsite list until saved.
    var campsiteName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var occupancy by remember { mutableStateOf("") }
    var mapLocation by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    // Gets the currently selected campsite from the local list.
    val selectedCampsite = campsites[selectedIndex]

    // Main vertical layout shared by all campsite owner screens.
    Column(
        modifier = modifier
            .fillMaxSize()                       // Uses the available screen space.
            .verticalScroll(rememberScrollState()) // Allows vertical scrolling.
            .padding(16.dp)                      // Adds space around the content.
    ) {

        // App title shown at the top of each screen.
        Text(
            text = "AURORA CAMPSITES",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Divider below the app title.
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Displays the correct screen based on currentPage.
        when (currentPage) {

            // --------------------------------------------------
            // MY CAMPSITES SCREEN
            // US 02.03.01
            // Displays the campsites owned by the current user.
            // --------------------------------------------------
            "list" -> {

                // Page heading.
                OwnerTitle("My Campsites")

                // Adds space below the heading.
                Spacer(modifier = Modifier.height(16.dp))

                // Creates an entry for each campsite in the list.
                campsites.forEachIndexed { index, campsite ->

                    // Layout for one campsite entry.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        // Displays the campsite name.
                        Text(
                            text = campsite.name,
                            fontWeight = FontWeight.Bold
                        )

                        // Displays basic information about the campsite.
                        Text("Location: ${campsite.location}")
                        Text("Maximum Occupancy: ${campsite.occupancy}")
                        Text("Tags: ${campsite.tags}")

                        // Opens the details page for this campsite.
                        Button(
                            onClick = {

                                // Saves which campsite was selected.
                                selectedIndex = index

                                // Navigates to the campsite details screen.
                                currentPage = "details"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Details")
                        }
                    }
                }
            }

            // --------------------------------------------------
            // CAMPSITE DETAILS SCREEN
            // US 02.04.03
            // Shows all information for the selected campsite.
            // --------------------------------------------------
            "details" -> {

                // Page heading.
                OwnerTitle("Campsite Details")

                // Displays campsite information in read-only fields.
                OwnerInfo("Campsite Name", selectedCampsite.name)
                OwnerInfo("Location", selectedCampsite.location)

                // Displays the description and amenities.
                OwnerInfo(
                    "Description / Amenities",
                    selectedCampsite.description
                )

                // Displays the maximum occupancy.
                OwnerInfo(
                    "Maximum Occupancy",
                    selectedCampsite.occupancy
                )

                // Displays the map location and tags.
                OwnerInfo("Map Location", selectedCampsite.mapLocation)
                OwnerInfo("Tags", selectedCampsite.tags)

                // Opens the edit screen for the selected campsite.
                Button(
                    onClick = {

                        // Copies the selected campsite data into the edit fields.
                        campsiteName = selectedCampsite.name
                        location = selectedCampsite.location
                        description = selectedCampsite.description
                        occupancy = selectedCampsite.occupancy
                        mapLocation = selectedCampsite.mapLocation
                        tags = selectedCampsite.tags

                        // Navigates to the edit screen.
                        currentPage = "edit"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Campsite")
                }

                // Opens the QR code placeholder screen.
                TextButton(
                    onClick = {

                        // Navigates to the QR code screen.
                        currentPage = "qr"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View QR Code")
                }

                // Returns to the list of campsites.
                TextButton(
                    onClick = {

                        // Navigates back to My Campsites.
                        currentPage = "list"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to My Campsites")
                }
            }

            // --------------------------------------------------
            // EDIT CAMPSITE SCREEN
            // US 02.01.03
            // Allows the owner to update campsite information.
            // --------------------------------------------------
            "edit" -> {

                // Page heading.
                OwnerTitle("Edit Campsite")

                // Editable field for the campsite name.
                OwnerInput("Campsite Name", campsiteName) {

                    // Updates the name when the user types.
                    campsiteName = it
                }

                // Editable field for the campsite location.
                OwnerInput("Location", location) {

                    // Updates the location when the user types.
                    location = it
                }

                // Editable field for the campsite description.
                OwnerInput("Description / Amenities", description) {

                    // Updates the description when the user types.
                    description = it
                }

                // Editable field for maximum occupancy.
                OwnerInput("Maximum Occupancy", occupancy) {

                    // Updates the occupancy when the user types.
                    occupancy = it
                }

                // Editable field for the map location.
                OwnerInput("Map Location", mapLocation) {

                    // Updates the map location when the user types.
                    mapLocation = it
                }

                // Editable field for campsite tags.
                OwnerInput("Tags", tags) {

                    // Updates the tags when the user types.
                    tags = it
                }

                // Adds space before the save button.
                Spacer(modifier = Modifier.height(12.dp))

                // Saves the edited campsite information locally.
                Button(
                    onClick = {

                        // Replaces the selected campsite with the edited values.
                        // This updates the local Compose state only.
                        // A future version should save changes through
                        // CampsiteRepository and Firestore.
                        campsites[selectedIndex] = OwnerCampsite(
                            campsiteName,
                            location,
                            description,
                            occupancy,
                            mapLocation,
                            tags
                        )

                        // Navigates to the save confirmation screen.
                        currentPage = "saved"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Changes")
                }
            }

            // --------------------------------------------------
            // CHANGES SAVED SCREEN
            // US 02.01.03
            // Confirms that the local information was updated.
            // --------------------------------------------------
            "saved" -> {

                // Confirmation message displayed after saving.
                // This currently confirms local state changes only.
                OwnerTitle("Changes Saved Successfully!")

                // Shows the updated campsite information.
                OwnerInfo("Campsite Name", selectedCampsite.name)
                OwnerInfo("Location", selectedCampsite.location)

                // Shows the updated description.
                OwnerInfo(
                    "Description / Amenities",
                    selectedCampsite.description
                )

                // Shows the updated occupancy.
                OwnerInfo(
                    "Maximum Occupancy",
                    selectedCampsite.occupancy
                )

                // Shows the updated map location and tags.
                OwnerInfo("Map Location", selectedCampsite.mapLocation)
                OwnerInfo("Tags", selectedCampsite.tags)

                // Returns to the campsite details screen.
                Button(
                    onClick = {

                        // Navigates back to the selected campsite details.
                        currentPage = "details"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Campsite Details")
                }
            }

            // --------------------------------------------------
            // CAMPSITE QR CODE SCREEN
            // US 02.02.01
            // Placeholder for the future QR code feature.
            // --------------------------------------------------
            "qr" -> {

                // Page heading.
                OwnerTitle("Campsite QR Code")

                // Adds space above the QR code placeholder.
                Spacer(modifier = Modifier.height(32.dp))

                // Placeholder text where the QR code will be displayed.
                Text(
                    text = "QR Code",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Adds space below the placeholder.
                Spacer(modifier = Modifier.height(12.dp))

                // Explains that QR code generation is not implemented yet.
                // The backend integration will need to provide a valid
                // campsite link or identifier for QR code generation.
                Text(
                    text = "QR code generation is not available yet.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Adds space before the return button.
                Spacer(modifier = Modifier.height(32.dp))

                // Returns to the campsite details screen.
                Button(
                    onClick = {

                        // Navigates back to campsite details.
                        currentPage = "details"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Campsite Details")
                }
            }
        }
    }
}

/**
 * Displays the title of a campsite owner screen.
 *
 * This helper keeps the heading style consistent
 * across all the screens.
 *
 * @param title The heading text to display.
 */
@Composable
private fun OwnerTitle(title: String) {

    // Displays a centered, bold heading.
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
}

/**
 * Displays campsite information in a read-only text field.
 *
 * Used on the details and save confirmation screens.
 * Users can see the information but cannot edit it here.
 *
 * @param label The name of the field.
 * @param value The campsite information to display.
 */
@Composable
private fun OwnerInfo(label: String, value: String) {

    // Creates a text field that cannot be edited.
    OutlinedTextField(
        value = value,

        // No changes are accepted in this field.
        onValueChange = {},

        // Displays the field name above its value.
        label = { Text(label) },

        // Prevents users from editing the displayed information.
        readOnly = true,

        // Makes the field use the available width.
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

/**
 * Displays an editable text field for campsite information.
 *
 * Used on the Edit Campsite screen.
 * The entered value is passed back through onValueChange.
 *
 * @param label The name of the field.
 * @param value The current input value.
 * @param onValueChange Called whenever the user edits the text.
 */
@Composable
private fun OwnerInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {

    // Creates an editable field connected to the screen state.
    OutlinedTextField(
        value = value,

        // Sends the new text back to the parent screen.
        onValueChange = onValueChange,

        // Displays the field name.
        label = { Text(label) },

        // Makes the field fill the width with spacing below.
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}