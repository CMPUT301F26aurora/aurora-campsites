package com.example.auroracampsites.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.auroracampsites.ui.qr.ScanCampsiteQrButton

/** Main navigation entry for existing entrant, owner and profile screens. */
@Composable
fun HomeScreen(navController: NavController, scanQr: (suspend () -> String?)? = null) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Aurora Campsites", style = MaterialTheme.typography.headlineMedium)
            Text("Explore and manage your camping trips.")
            HorizontalDivider()
            Text("Camping", style = MaterialTheme.typography.titleLarge)
            HomeLink("Search Campsites", "campsite_search", navController)
            ScanCampsiteQrButton(scan = scanQr) { campsiteId ->
                navController.navigate("campsite_details/${Uri.encode(campsiteId)}") {
                    launchSingleTop = true
                }
            }
            HomeLink("Available Booking Dates", "available_booking_dates", navController)
            HomeLink("My Reservations", "active_reservations", navController)
            HorizontalDivider()
            Text("Settings", style = MaterialTheme.typography.titleLarge)
            HomeLink("Profile Settings", "profile_settings", navController)
            HomeLink("App Policy", "app_policy", navController)
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            HomeLink("Campsite Owner", "owner_menu", navController)
            HomeLink("Admin", "admin_menu", navController)
        }
    }
}

@Composable
private fun HomeLink(label: String, route: String, navController: NavController) {
    Button(
        onClick = { navController.navigate(route) { launchSingleTop = true } },
        modifier = Modifier.fillMaxWidth()
    ) { Text(label) }
}

/** Owner navigation menu. Existing screens handle campsite and lottery operations. */
@Composable
fun OwnerMenuScreen(navController: NavController) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Campsite Owner", style = MaterialTheme.typography.headlineMedium)
            HorizontalDivider()
            HomeLink("My Campsites", "campsite_owner", navController)
            HomeLink("Register Campsite", "register_campsite", navController)
            HomeLink("Manage Lotteries", "owner_lotteries", navController)
            TextButton(onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()) { Text("Back to Home") }
        }
    }
}
