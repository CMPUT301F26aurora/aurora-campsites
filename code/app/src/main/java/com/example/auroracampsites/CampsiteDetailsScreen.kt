
package com.example.auroracampsites

/*
 *
 * User Story:
 * US 02.01.04 - As a campsite owner, I want to
 * view details of my campsite.
 *
 * Read-only campsite details screen.
 * Includes campsite information, QR code entry,
 * and navigation to the editing screen.
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CampsiteDetailsScreen(
    modifier: Modifier = Modifier,
    campsiteName: String = "Pine Creek Campground",
    location: String = "Hinton, Alberta",
    description: String = "Cookhouse, tennis court",
    maximumOccupancy: String = "8",
    mapLocation: String = "Hinton, AB",
    tags: String = "Family, outdoor",
    onViewQRCode: () -> Unit = {},
    onEditCampsite: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AURORA CAMPSITES",
            fontSize = 24.sp
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = "Campsite Details",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(22.dp))

        DetailsField("Campsite Name", campsiteName)
        DetailsField("Location", location)
        DetailsField("Description / Amenities", description)
        DetailsField("Maximum Occupancy", maximumOccupancy)
        DetailsField("Map Location", mapLocation)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("QR Code", fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onViewQRCode,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text(
                        "View QR Code",
                        color = Color.Black,
                        fontSize = 12.sp
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                DetailsField("Tags", tags)
            }
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onEditCampsite,
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(50.dp),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Color.Black),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE5E5E8),
                contentColor = Color.Black
            )
        ) {
            Text("Edit Campsite", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailsField(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(6.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(5.dp),
            border = BorderStroke(1.dp, Color.Gray),
            color = Color(0xFFF5F5F5)
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(12.dp),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}
