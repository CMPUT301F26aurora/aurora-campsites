
package com.example.auroracampsites

/*
 * User Story:
 * US 1.4.1 - View available booking dates
 * after receiving a winning lottery status.
 *
 * Description:
 * Displays the user's lottery result and
 * available booking dates for a campsite.
 * (just for navigation: lines with // needs to be replace with real data)
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class LotteryResult {
    WIN,
    LOSE,
    NOT_ENTERED
}

private data class LotteryStatusData(
    val number: String,
    val lotteryName: String,
    val winners: Int,
    val reserveTimeline: String,
    val result: LotteryResult,
    val showDetails: Boolean = false,
    val note: String? = null
)

@Composable
fun AvailableBookingDatesScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSeeDetails: (String) -> Unit = {}
) {
    // hardcode here, sample data for UI test only
    val lotteries = listOf(
        LotteryStatusData(
            number = "1a.",
            lotteryName = "Pine Creek Lottery",
            winners = 5,
            reserveTimeline = "2026-11-08 to 2026-11-15",
            result = LotteryResult.WIN,
            showDetails = true
        ),
        LotteryStatusData(
            number = "1b.",
            lotteryName = "Pine Creek Lottery",
            winners = 5,
            reserveTimeline = "N/A",
            result = LotteryResult.LOSE,
            note = "*Results per loss or non entrance"
        ),
        LotteryStatusData(
            number = "2.",
            lotteryName = "Multiple Campsites Lottery",
            winners = 5,
            reserveTimeline = "N/A",
            result = LotteryResult.WIN,
            showDetails = true
        )
    )

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

        Surface(
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
                            onSeeDetails(lottery.lotteryName)
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

        Text(
            text = "Winners : ${lottery.winners}",
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 12.dp)
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Reserve Timeline : ${lottery.reserveTimeline}",
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 12.dp)
        )

        Spacer(Modifier.height(18.dp))

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
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Not Entered / ")
                    }

                    withStyle(
                        SpanStyle(
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("LOSE")
                    }
                }

                LotteryResult.NOT_ENTERED -> {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Not Entered")
                    }
                }
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