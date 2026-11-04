
package com.example.auroracampsites

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.auroracampsites.ui.theme.AuroraCampsitesTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Main activity for the Aurora Campsites application.
 *
 * Displays the halfway checkpoint frontend screens.
 * Firebase is initialized, but the screens currently use sample data.
 */
class MainActivity : ComponentActivity() {

    // Existing Firestore database reference.
    // Backend integration will connect the screens to this database.
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allows the app to draw behind the system bars.
        enableEdgeToEdge()

        // Sets up the application's user interface.
        setContent {

            // Uses the existing Aurora Campsites theme.
            AuroraCampsitesTheme {

                // Main layout for the app.
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // Opens the nine halfway checkpoint frontend features.
                    HalfwayCheckpointScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Existing sample composable kept from the original project.
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

// Existing preview kept for the sample Greeting composable.
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AuroraCampsitesTheme {
        Greeting("Android")
    }
}
