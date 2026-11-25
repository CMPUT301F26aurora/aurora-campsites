package com.example.auroracampsites.ui.navigation


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.auroracampsites.data.models.Campsite
import com.example.auroracampsites.data.repository.CampsiteRepository
import kotlinx.coroutines.CancellationException
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.auroracampsites.ReservationManagementScreen
import com.example.auroracampsites.ui.screens.*


@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {


        composable("home") {
            HomeScreen(navController = navController)
        }

        // --- PART 1: ENTRANT FLOW ---
        //  Search & Browse Campsites (US 01.01.01 - 01.01.04, US 03.04.01)
        composable("campsite_search") {
            CampsiteSearchScreen(navController = navController)
        }


        //  View Campsite Details -- user (US 02.01.02 - 02.01.04, US 02.02.01)
        composable(
            route = "campsite_details/{campsiteId}",
            arguments = listOf(navArgument("campsiteId") { type = NavType.StringType })
        ) { entry ->
            val campsiteId = entry.arguments?.getString("campsiteId").orEmpty()
            val repository = remember { CampsiteRepository() }
            var campsite by remember(campsiteId) { mutableStateOf<Campsite?>(null) }
            var isLoading by remember(campsiteId) { mutableStateOf(true) }
            var error by remember(campsiteId) { mutableStateOf("") }
            var retry by remember(campsiteId) { mutableStateOf(0) }

            LaunchedEffect(campsiteId, retry) {
                isLoading = true
                error = ""
                campsite = null
                try {
                    campsite = repository.getCampsiteById(campsiteId)
                    if (campsite == null) error = "Campsite not found."
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    error = "Unable to load campsite."
                } finally {
                    isLoading = false
                }
            }

            val loadedCampsite = campsite
            if (loadedCampsite != null && !isLoading) {
                CampsiteDetailsScreen(
                    navController = navController,
                    campsite = loadedCampsite
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text(error)
                        Button(onClick = { retry++ }) { Text("Retry") }
                    }
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back to Search")
                    }
                }
            }
        }

        // 2. Select Booking Dates (US 01.04.01)
        composable("available_booking_dates") {
            AvailableBookingDatesScreen(navController = navController)
        }

        //  Confirm Booking & View Win/Lose Notifications (US 01.03.01, US 01.05.01)
        composable("booking_confirmation/{reservationId}",
            arguments = listOf(navArgument("reservationId") { type = NavType.StringType })
        ) { entry ->
            BookingConfirmationScreen(navController = navController, reservationId = entry.arguments?.getString("reservationId").orEmpty())
        }

        //  View Active Reservations & Cancel (US 01.06.01, US 01.07.01)
        composable("active_reservations") {
            ReservationManagementScreen(navController = navController)
        }

        //  Leave Review (US 01.13.01)
        composable(route = "leave_review/{reservationId}",
            arguments = listOf(navArgument("reservationId") { type = NavType.StringType })
        ) { entry ->
            ReservationReviewScreen(navController, entry.arguments?.getString("reservationId").orEmpty())
        }

        // --- PART 2: OWNER & ADMIN FLOW ---
        composable("owner_menu") {
            OwnerMenuScreen(navController)
        }
        composable("owner_lotteries") {
            HalfwayCheckpointScreen(navController = navController, mode = "owner")
        }
        composable("admin_menu") {
            HalfwayCheckpointScreen(navController = navController, mode = "admin")
        }

        //  View My Campsites & Manage Lotteries (US 02.03.01, US 02.04.01 - 02.04.05)
        composable("campsite_owner") {
            CampsiteOwnerScreen(navController = navController)
        }

        //  Register New Campsite (US 02.01.01)
        composable("register_campsite") {
            RegisterCampsiteScreen(navController = navController)
        }



        // --- PART 3: PROFILE & SYSTEM SETTINGS ---
        //  Profile Settings & Admin Remove Profiles (US 01.07.03, US 01.08.01, US 03.02.01, US 03.05.01)
        composable("profile_settings") {
            ProfileManagementScreen(navController = navController)
        }

        //  App Terms & Policies (US 01.11.01)
        composable("app_policy") {
            AppPolicyScreen(navController = navController)
        }

        // Demo Owner and Admin
        composable("halfway_checkpoint") {
            HalfwayCheckpointScreen(navController = navController)
        }
    }
}

@Preview (showBackground = true)
@Composable
fun AppNavPreview(){
    AppNav()
}
