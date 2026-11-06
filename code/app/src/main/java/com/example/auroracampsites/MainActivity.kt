package com.example.auroracampsites

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.auroracampsites.ui.navigation.AppNav
import com.example.auroracampsites.ui.theme.AuroraCampsitesTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {

    val db = Firebase.firestore //implementation of database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuroraCampsitesTheme {
                AppNav()
            }
        }
    }
}
                
