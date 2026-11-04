
package com.example.auroracampsites

//US 02.01.01 - As a campsite owner, I want to register my campsite with the app.
//US 02.01.02 - As a campsite owner, I want to provide campsite details
//             including name, location, description, maximum occupancy,
//             map location, optional image, and tags.
//US 02.01.03 - Update existing campsite details.
//
//Shared campsite form for registration and editing.
//Includes campsite information, optional image,
//tags, and input validation.

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CampsiteFormData(
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val occupancy: String = "",
    val mapLocation: String = "",
    val tags: String = ""
)

@Composable
fun RegisterCampsiteScreen(
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    existingCampsite: CampsiteFormData = CampsiteFormData(),
    onSaved: (CampsiteFormData) -> Unit = {}
) {
    var campsiteName by remember(isEditMode, existingCampsite) {
        mutableStateOf(existingCampsite.name)
    }
    var location by remember(isEditMode, existingCampsite) {
        mutableStateOf(existingCampsite.location)
    }
    var description by remember(isEditMode, existingCampsite) {
        mutableStateOf(existingCampsite.description)
    }
    var occupancy by remember(isEditMode, existingCampsite) {
        mutableStateOf(existingCampsite.occupancy)
    }
    var mapLocation by remember(isEditMode, existingCampsite) {
        mutableStateOf(existingCampsite.mapLocation)
    }
    var tags by remember(isEditMode, existingCampsite) {
        mutableStateOf(existingCampsite.tags)
    }

    var selectedImage by remember {
        mutableStateOf<Uri?>(null)
    }
    var success by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImage = uri
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AURORA CAMPSITES",
            fontSize = 25.sp
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 18.dp)
        )

        if (success) {
            Spacer(Modifier.height(150.dp))

            Text(
                text = if (isEditMode)
                    "Changes Saved!"
                else
                    "Registration Successful!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = {
                    success = false
                }
            ) {
                Text(
                    if (isEditMode) "Back to Edit"
                    else "Back to Form"
                )
            }
        } else {
            Text(
                text = if (isEditMode)
                    "Edit Campsite"
                else
                    "Register Campsite",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(20.dp))

            CampsiteFormField(
                "Campsite Name",
                campsiteName,
                { campsiteName = it }
            )

            CampsiteFormField(
                "Location",
                location,
                { location = it }
            )

            CampsiteFormField(
                "Description / Amenities",
                description,
                { description = it },
                minLines = 3
            )

            CampsiteFormField(
                "Maximum Occupancy",
                occupancy,
                { occupancy = it },
                keyboardType = KeyboardType.Number
            )

            CampsiteFormField(
                "Map Location",
                mapLocation,
                { mapLocation = it }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Optional Image", fontSize = 14.sp)

                    OutlinedButton(
                        onClick = {
                            imagePicker.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (selectedImage == null)
                                "Choose Image"
                            else
                                "Image Selected",
                            fontSize = 12.sp
                        )
                    }
                }

                Column(Modifier.weight(1f)) {
                    Text("Tags", fontSize = 14.sp)

                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            if (showError) {
                Spacer(Modifier.height(12.dp))

                Text(
                    "Please complete all required fields and enter a valid occupancy.",
                    color = Color.Red,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val valid =
                        campsiteName.isNotBlank() &&
                                location.isNotBlank() &&
                                description.isNotBlank() &&
                                (occupancy.toIntOrNull() ?: 0) > 0 &&
                                mapLocation.isNotBlank()

                    if (valid) {
                        showError = false

                        val data = CampsiteFormData(
                            name = campsiteName,
                            location = location,
                            description = description,
                            occupancy = occupancy,
                            mapLocation = mapLocation,
                            tags = tags
                        )

                        onSaved(data)
                        success = true
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE5E5E8),
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Text(
                    if (isEditMode)
                        "Save Changes"
                    else
                        "Register Campsite",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CampsiteFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Text(label, fontSize = 14.sp)

        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            singleLine = minLines == 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5)
            )
        )
    }
}