package com.example.auroracampsites.ui.screens

/*
 * User Story:
 * US 1.4.1 - View available booking dates
 * after receiving a winning lottery status.
 *
 * Description:
 * Displays the user's lottery result and
 * available booking dates for a campsite.
 * Reads personal lottery results and dates from Firebase.
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import com.example.auroracampsites.data.session.UserSession
import com.example.auroracampsites.data.repository.WaitListRepository
import com.example.auroracampsites.data.repository.CampsiteRepository
import kotlinx.coroutines.CancellationException
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private enum class LotteryResult {
    WIN,
    LOSE,
    WAITING,
    UNKNOWN
}

private data class LotteryStatusData(
    val number: String,
    val lotteryName: String,
    val campsiteId: String,
    val availableDates: List<String>,
    val result: LotteryResult,
    val showDetails: Boolean = false,
    val note: String? = null
)

@Composable
fun AvailableBookingDatesScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { navController.popBackStack() },
    onSeeDetails: (String) -> Unit = { id ->
        navController.navigate("campsite_details/${Uri.encode(id)}")
    }
) {
    val context = LocalContext.current
    val userId = remember(context) { UserSession.getCurrentUserId(context) }
    val waitListRepository = remember { WaitListRepository() }
    val campsiteRepository = remember { CampsiteRepository() }
    var lotteries by remember(userId) { mutableStateOf<List<LotteryStatusData>>(emptyList()) }
    var isLoading by remember(userId) { mutableStateOf(true) }
    var error by remember(userId) { mutableStateOf("") }
    var refresh by remember { mutableStateOf(0) }

    LaunchedEffect(userId, refresh) {
        isLoading = true
        error = ""
        try {
            val entries = waitListRepository.getUserWaitListEntries(userId)
            lotteries = entries.sortedBy { it.first.registrationStart }.mapIndexed { index, (lottery, entry) ->
                val id = lottery.campsiteId
                val campsite = campsiteRepository.getCampsiteById(id)
                val result = when (entry.status) {
                    "Won" -> LotteryResult.WIN
                    "Lost" -> LotteryResult.LOSE
                    "Waiting" -> LotteryResult.WAITING
                    else -> LotteryResult.UNKNOWN
                }
                LotteryStatusData(
                    number = "${index + 1}.",
                    lotteryName = (campsite?.name ?: "Campsite no longer available") + " (${lottery.registrationStart})",
                    campsiteId = id,
                    availableDates = if (result == LotteryResult.WIN && campsite != null && campsite.activeLotteryId == lottery.id)
                        campsite.availableDates.filterNot { it in campsite.bookedDates }
                            .distinct().sorted() else emptyList(),
                    result = result,
                    showDetails = result == LotteryResult.WIN && campsite != null && campsite.activeLotteryId == lottery.id,
                    note = if (campsite == null) "Campsite not found." else null
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("LotteryDebug", "Unable to load lottery records", e)
            error = "Unable to load lottery records. Please try again."
        } finally {
            isLoading = false
        }
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

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .height(34.dp),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(
                    horizontal = 20.dp,
                    vertical = 0.dp
                ),
                border = BorderStroke(2.dp, Color.Black)
            ) {
                Text(
                    text = "Back",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Lottery Status",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(14.dp))

        OutlinedButton(onClick = { refresh++ }, enabled = !isLoading) { Text("Refresh") }
        if (isLoading) {
            CircularProgressIndicator()
        } else if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
            Button(onClick = { refresh++ }) { Text("Retry") }
        } else if (lotteries.isEmpty()) {
            Text("No lottery records yet.")
        } else Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color.Gray),
            color = Color(0xFFF7F7F7)
        ) {
            Column {
                lotteries.forEachIndexed { index, lottery ->
                    LotteryStatusItem(
                        lottery = lottery,
                        onSeeDetails = {
                            onSeeDetails(lottery.campsiteId)
                        }
                    )

                    if (index < lotteries.lastIndex) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LotteryStatusItem(
    lottery: LotteryStatusData,
    onSeeDetails: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 245.dp)
            .padding(16.dp)
    ) {
        Text(
            text = lottery.number,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = "Lottery Name : ${lottery.lotteryName}",
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 12.dp)
        )

        Spacer(Modifier.height(18.dp))

        if (lottery.result == LotteryResult.WIN) {
            Text(
                text = "Available Booking Dates: " +
                        lottery.availableDates.ifEmpty { listOf("None currently available") }.joinToString(", "),
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(Modifier.height(18.dp))
        }

        val resultText = buildAnnotatedString {
            append("Lottery Result : ")

            when (lottery.result) {
                LotteryResult.WIN -> {
                    withStyle(
                        SpanStyle(
                            color = Color(0xFF20C553),
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("WIN")
                    }
                }

                LotteryResult.LOSE -> {
                    withStyle(
                        SpanStyle(
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("LOSE")
                    }
                }

                LotteryResult.WAITING -> {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("WAITING")
                    }
                }
                LotteryResult.UNKNOWN -> append("Unknown status")
            }
        }

        Text(
            text = resultText,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 12.dp)
        )

        Spacer(Modifier.weight(1f, fill = false))
        Spacer(Modifier.height(35.dp))

        if (lottery.showDetails) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onSeeDetails,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(2.dp, Color.Black),
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 4.dp
                    )
                ) {
                    Text(
                        text = "See details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        lottery.note?.let { note ->
            Text(
                text = note,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }
}
