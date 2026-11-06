package com.example.auroracampsites.ui.screens

/*
 *
 * User Stories:
 * US 01.05.01 - Confirm a campsite reservation.
 * US 01.05.02 - Send a reservation notification
 * or download the reservation as CSV.
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController

private val AccentCyan = Color(0xFF00BECB)
private val LightGray = Color(0xFFE6E6E9)

@Composable
fun BookingConfirmationScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onSendNotification: () -> Unit = {},
    onDownloadCsv: () -> Unit = {},
//    onViewReservations: () -> Unit = {} -- does what?
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            val screenHeight = maxHeight

            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.5.dp,
                    Color(0xFFD7D7D7)
                ),
                color = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(22.dp))

                        Text(
                            text = "AURORA CAMPSITES",
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(top = 14.dp),
                            color = Color.LightGray
                        )

                        Spacer(Modifier.height(70.dp))

                        Text(
                            text = "Campsite Reserved!",
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(76.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(0.88f),
                            shape = RoundedCornerShape(5.dp),
                            border = BorderStroke(
                                1.5.dp,
                                Color.Black
                            ),
                            color = LightGray
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = 20.dp,
                                    vertical = 16.dp
                                ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ConfirmationActionButton(
                                    text = "Send a Notification",
                                    onClick = onSendNotification
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "or",
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(8.dp))

                                ConfirmationActionButton(
                                    text = "Download as CSV",
                                    onClick = onDownloadCsv
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { navController.navigate("active_reservations") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = 20.dp,
                                bottom = screenHeight * 0.10f
                            ),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(
                            1.5.dp,
                            Color.Black
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFF5F5F5),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "View Reservations",
                            fontSize = 13.sp
                        )

                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationActionButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(
            1.dp,
            Color.Black
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFFF8F8F8),
            contentColor = AccentCyan
        )
    ) {
        Text(
            text = text,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
