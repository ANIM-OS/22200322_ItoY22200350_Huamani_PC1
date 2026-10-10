package dev.PC22200322y22200350.pc1.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.ListenerRegistration
import dev.PC22200322y22200350.pc1.data.model.LotteryDraw
import dev.PC22200322y22200350.pc1.data.remote.LotteryRepository
import dev.PC22200322y22200350.pc1.presentation.AppPage
import dev.PC22200322y22200350.pc1.presentation.DeepForest
import dev.PC22200322y22200350.pc1.presentation.Eyebrow
import dev.PC22200322y22200350.pc1.presentation.Forest
import dev.PC22200322y22200350.pc1.presentation.Lime
import dev.PC22200322y22200350.pc1.presentation.MutedText
import dev.PC22200322y22200350.pc1.presentation.NumberBall
import dev.PC22200322y22200350.pc1.presentation.PaleGreen
import dev.PC22200322y22200350.pc1.presentation.StatusCard

@Composable
fun HomeScreen(
    email: String,
    uid: String,
    onSignOut: () -> Unit,
    onPlaySaved: (List<Int>) -> Unit
) {
    var draw by remember { mutableStateOf<LotteryDraw?>(null) }
    var drawLoaded by remember { mutableStateOf(false) }
    var listenerError by remember { mutableStateOf<String?>(null) }
    var selectedNumbersValue by rememberSaveable { mutableStateOf("") }
    val selectedNumbers = remember(selectedNumbersValue) {
        selectedNumbersValue.split(',').mapNotNull(String::toIntOrNull)
    }
    var saveError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    DisposableEffect(uid) {
        val registration: ListenerRegistration = LotteryRepository.listenDraw { updated, error ->
            draw = updated
            listenerError = error
            drawLoaded = true
        }
        onDispose { registration.remove() }
    }

    AppPage {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("Tu participación")
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onSignOut, shape = RoundedCornerShape(18.dp)) { Text("Salir") }
            }
            Text("Elige tus números", style = MaterialTheme.typography.headlineMedium, color = DeepForest, fontWeight = FontWeight.Bold)
            Text("Sesión: $email", color = MutedText, style = MaterialTheme.typography.bodySmall)

            ElevatedCard(shape = RoundedCornerShape(20.dp), colors = CardDefaults.elevatedCardColors(containerColor = Forest)) {
                Column(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(draw?.nombre?.ifBlank { "Sorteo de prueba" } ?: "Sorteo de prueba", color = Lime, fontWeight = FontWeight.Bold)
                    Text(draw?.fecha?.ifBlank { "Fecha por confirmar" } ?: "Conectando con Firestore…", color = androidx.compose.ui.graphics.Color.White)
                    Text("ID: ${LotteryRepository.TEST_DRAW_ID}", color = androidx.compose.ui.graphics.Color.White.copy(alpha = .78f), fontSize = 11.sp)
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Tus números", fontWeight = FontWeight.Bold, color = DeepForest)
                Spacer(Modifier.weight(1f))
                Text("${selectedNumbers.size} de 6", color = MutedText, fontSize = 13.sp)
            }

            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(6),
                modifier = Modifier.fillMaxWidth().height(358.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(45) { index ->
                    val number = index + 1
                    val isSelected = number in selectedNumbers
                    NumberBall(
                        number = number,
                        selected = isSelected,
                        modifier = Modifier.padding(1.dp),
                        onClick = {
                            val updatedSelection = when {
                                isSelected -> selectedNumbers - number
                                selectedNumbers.size < 6 -> selectedNumbers + number
                                else -> selectedNumbers
                            }
                            selectedNumbersValue = updatedSelection.joinToString(",")
                            saveError = null
                        }
                    )
                }
            }

            when {
                !drawLoaded -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Forest, strokeWidth = 2.dp)
                    Text("  Conectando con el sorteo…", color = MutedText)
                }
                listenerError != null -> StatusCard("No se pudo cargar el sorteo", listenerError.orEmpty())
                draw == null -> StatusCard("Sorteo no encontrado", "Crea el documento /sorteos/${LotteryRepository.TEST_DRAW_ID} en Firestore.")
                draw?.isOpen == true -> StatusCard("Sorteo abierto", "Podrás registrar tu jugada hasta que comience.", icon = "●")
                else -> StatusCard("Registro cerrado", "El sorteo ya comenzó y no se aceptan nuevas jugadas.")
            }

            if (saveError != null) Text(saveError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = {
                    if (selectedNumbers.size != 6) {
                        saveError = "Selecciona exactamente seis números."
                    } else {
                        saving = true
                        saveError = null
                        LotteryRepository.savePlay(uid, selectedNumbers).addOnCompleteListener { task ->
                            saving = false
                            if (task.isSuccessful) onPlaySaved(selectedNumbers)
                            else saveError = LotteryRepository.userFacingError(task.exception)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = !saving && draw?.isOpen == true && selectedNumbers.size == 6,
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (saving) "Guardando…" else "Guardar mi jugada", fontWeight = FontWeight.Bold)
            }
            Text("Tu jugada solo será visible para tu cuenta.", modifier = Modifier.align(Alignment.CenterHorizontally), color = MutedText, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
        }
    }
}
