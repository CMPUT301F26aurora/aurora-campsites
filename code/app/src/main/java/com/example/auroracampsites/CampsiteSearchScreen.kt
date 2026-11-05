package com.example.aurora_campsites

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

/**
 * Role: UI Screen for searching and filtering campsites (US 01.01.02).
 *
 * What it does:
 * - Lets the user type keywords into a search bar.
 * - Displays a filter button that opens a dialog with mini-search bars (location, date, guests).
 * - Displays matching campsite items in a scrollable list.
 *
 * Connections:
 * - Uses Campsite model and NavController to handle back button navigation.
 * - Teammate will hook up Firestore data queries in the future.
 * - Includes testTag modifiers for UI testing.
 */
@Composable
fun CampsiteSearchScreen(navController: NavController) {

    // Store search bar text
    var searchText by remember { mutableStateOf("") }

    // Store mini-filter dialog inputs
    var locationInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("") }
    var guestsInput by remember { mutableStateOf("") }

    // Show or hide the filter popup dialog
    var showDialog by remember { mutableStateOf(false) }

    // Lists to hold all campsites and filtered campsites
    val allCampsites = remember { mutableStateListOf<Campsite>() }
    val filteredList = remember { mutableStateListOf<Campsite>() }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Standard Top Header Bar (No @OptIn required)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
