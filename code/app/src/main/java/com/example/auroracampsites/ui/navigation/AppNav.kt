package com.example.auroracampsites.ui.navigation


import androidx.compose.runtime.Composable
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
        startDestination = "campsite_search"
    ) {


        // --- PART 1: ENTRANT FLOW ---
        //  Search & Browse Campsites (US 01.01.01 - 01.01.04, US 03.04.01)
        composable("campsite_search") {
            CampsiteSearchScreen(navController = navController)
        }


        //  View Campsite Details -- user (US 02.01.02 - 02.01.04, US 02.02.01)
        composable("campsite_details") {
            CampsiteDetailsScreen(navController = navController)
        }

        // 2. Select Booking Dates (US 01.04.01)
        composable("available_booking_dates") {
            AvailableBookingDatesScreen(navController = navController)
        }

        //  Confirm Booking & View Win/Lose Notifications (US 01.03.01, US 01.05.01)
        composable("booking_confirmation") {
            BookingConfirmationScreen(navController = navController)
        }

        //  View Active Reservations & Cancel (US 01.06.01, US 01.07.01)
        composable("active_reservations") {
            ReservationManagementScreen(navController = navController)
        }

        //  Leave Review (US 01.13.01)
        composable(route = "leave_review") {
            LeaveReviewScreen(navController = navController)
        }

        // --- PART 2: OWNER & ADMIN FLOW ---
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