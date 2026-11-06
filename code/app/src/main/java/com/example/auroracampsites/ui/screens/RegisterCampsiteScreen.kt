package com.example.auroracampsites.ui.screens

//US 02.01.01 - As a campsite owner, I want to register my campsite with the app.
//US 02.01.02 - As a campsite owner, I want to provide campsite details
//             including name, location, description, maximum occupancy,
//             map location, optional image, and tags.

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
import androidx.navigation.NavController
import com.example.auroracampsites.data.models.Campsite
import com.example.auroracampsites.data.repository.CampsiteRepository
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun RegisterCampsiteScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    repository: CampsiteRepository = remember { CampsiteRepository() }
) {
    var campsiteName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var occupancy by remember { mutableStateOf("") }
    var mapLocation by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var availableDate by remember { mutableStateOf("2026-06-01") }
    var cancellationPolicy by remember { mutableStateOf("") }
    var rules by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var success by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImage = uri
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "AURORA CAMPSITES", fontSize = 25.sp)

        HorizontalDivider(modifier = Modifier.padding(vertical = 18.dp))

        if (success) {
            Spacer(Modifier.height(150.dp))

            Text(
                text = "Registration Successful!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = {
                    success = false
                    navController.navigate("campsite_owner") {
                        popUpTo("campsite_owner") { inclusive = true }
                    }
                }
            ) {
                Text("Back to My Campsites")
            }
        } else {
            Text(
                text = "Register Campsite",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(20.dp))

            CampsiteFormField("Campsite Name", campsiteName) { campsiteName = it }
            CampsiteFormField("Location / Map Location", mapLocation.ifEmpty { location }) {
                location = it
                mapLocation = it
            }
            CampsiteFormField("Description / Amenities", description, minLines = 3) { description = it }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    CampsiteFormField("Max Occupancy", occupancy, keyboardType = KeyboardType.Number) { occupancy = it }
                }
                Box(Modifier.weight(1f)) {
                    CampsiteFormField("Price ($ / Night)", price, keyboardType = KeyboardType.Number) { price = it }
                }
            }

            CampsiteFormField("Available Date (YYYY-MM-DD)", availableDate) { availableDate = it }
            CampsiteFormField("Cancellation Policy", cancellationPolicy, minLines = 2) { cancellationPolicy = it }
            CampsiteFormField("Campsite Rules", rules, minLines = 2) { rules = it }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Optional Image", fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text(if (selectedImage == null) "Choose Image" else "Image Selected", fontSize = 12.sp)
                    }
                }

                Column(Modifier.weight(1f)) {
                    CampsiteFormField("Tags (comma-separated)", tags) { tags = it }
                }
            }

            if (showError) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Please complete all required fields and enter valid occupancy/price numbers.",
                    color = Color.Red,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val parsedOccupancy = occupancy.toIntOrNull() ?: 0
                    val parsedPrice = price.toDoubleOrNull() ?: 0.0
                    val valid = campsiteName.isNotBlank() &&
                            location.isNotBlank() &&
                            description.isNotBlank() &&
                            parsedOccupancy > 0

                    if (valid) {
                        showError = false
                        coroutineScope.launch {
                            val newCampsite = Campsite(
                                id = UUID.randomUUID().toString(),
                                name = campsiteName,
                                location = location,
                                maxOccupancy = parsedOccupancy,
                                price = parsedPrice,
                                description = description,
                                availableDates = availableDate.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                cancellationPolicy = cancellationPolicy,
                                rules = rules,
                                amenities = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            )
                            repository.addCampsite(newCampsite)
                            success = true
                        }
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
                Text("Register Campsite", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CampsiteFormField(
    label: String,
    value: String,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
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
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5)
            )
        )
    }
}