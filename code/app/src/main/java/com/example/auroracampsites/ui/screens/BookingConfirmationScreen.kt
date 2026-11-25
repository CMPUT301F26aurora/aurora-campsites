package com.example.auroracampsites.ui.screens

/* US 01.05.01 - Confirm reservation.
 * US 01.05.02 - Share confirmation or export CSV.
 */
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.auroracampsites.data.models.Reservation
import com.example.auroracampsites.data.repository.ReservationRepo
import com.example.auroracampsites.data.session.UserSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BookingConfirmationScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onSendNotification: (() -> Unit)? = null,
    onDownloadCsv: (() -> Unit)? = null,
    reservationId: String = ""
) {
    val context = LocalContext.current
    val repository = remember { ReservationRepo() }
    val scope = rememberCoroutineScope()
    var reservation by remember(reservationId) { mutableStateOf<Reservation?>(null) }
    var loading by remember(reservationId) { mutableStateOf(true) }
    var error by remember(reservationId) { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var retry by remember { mutableStateOf(0) }
    var exporting by remember { mutableStateOf(false) }
    LaunchedEffect(reservationId, retry) {
        loading = true
        error = ""
        reservation = null
        try {
            reservation = repository.getReservationById(reservationId, UserSession.getCurrentUserId(context))
            if (reservation == null) error = "Reservation not found."
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { error = "Unable to load reservation." }
        finally { loading = false }
    }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        val item = reservation
        if (uri != null && item != null) {
            exporting = true
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        requireNotNull(context.contentResolver.openOutputStream(uri)).bufferedWriter(Charsets.UTF_8).use { it.write(reservationCsv(item)) }
                    }
                    message = "CSV saved."
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) { message = "Unable to save CSV." }
                finally { exporting = false }
            }
        }
    }
    Surface(modifier = modifier.fillMaxSize().padding(14.dp), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.5.dp, Color.LightGray)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("AURORA CAMPSITES", fontSize = 25.sp)
            HorizontalDivider()
            val item = reservation
            when {
                loading -> CircularProgressIndicator()
                item == null -> {
                    Text(error)
                    Button(onClick = { retry++ }) { Text("Retry") }
                }
                else -> {
                    Text(if (item.status == "ACTIVE") "Campsite Reserved!" else "Reservation: ${item.status}", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Text(item.campsiteName, fontWeight = FontWeight.Bold)
                    Text("Location: ${item.location}")
                    Text("Dates: ${item.startDate} to ${item.endDate}")
                    Text("Price: $${item.price}")
                    Text("Reservation ID: ${item.id}")
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(5.dp), color = Color(0xFFE6E6E9), border = BorderStroke(1.dp, Color.Gray)) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            OutlinedButton(onClick = {
                                if (onSendNotification != null) onSendNotification() else {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "Reservation ${item.id}: ${item.campsiteName}, ${item.startDate} to ${item.endDate}")
                                    }
                                    try { context.startActivity(Intent.createChooser(intent, "Share reservation")) }
                                    catch (e: Exception) { message = "Unable to share reservation." }
                                }
                            }, modifier = Modifier.fillMaxWidth()) { Text("Share Confirmation") }
                            Text("or")
                            OutlinedButton(onClick = {
                                if (onDownloadCsv != null) onDownloadCsv() else export.launch("reservation-${item.id}.csv")
                            }, enabled = !exporting, modifier = Modifier.fillMaxWidth()) { Text("Download as CSV") }
                        }
                    }
                    if (message.isNotBlank()) Text(message)
                }
            }
            OutlinedButton(onClick = { navController.navigate("active_reservations") }) { Text("View Reservations") }
            TextButton(onClick = { navController.popBackStack() }) { Text("Back") }
        }
    }
}

private fun reservationCsv(item: Reservation): String {
    fun cell(value: String): String {
        val safe = if (value.trimStart().firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r', '\n')) "'$value" else value
        return "\"${safe.replace("\"", "\"\"")}\""
    }
    return "reservationId,campsiteId,campsiteName,location,maximumOccupancy,price,startDate,endDate,status\r\n" +
            listOf(item.id, item.campsiteId, item.campsiteName, item.location, item.maximumOccupancy.toString(), item.price.toString(), item.startDate, item.endDate, item.status).joinToString(",") { cell(it) } + "\r\n"
}
