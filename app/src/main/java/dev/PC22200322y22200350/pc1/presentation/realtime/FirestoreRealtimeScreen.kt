package dev.PC22200322y22200350.pc1.presentation.realtime

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.ListenerRegistration
import dev.PC22200322y22200350.pc1.data.model.DrawBall
import dev.PC22200322y22200350.pc1.data.model.LotteryDraw
import dev.PC22200322y22200350.pc1.data.remote.LotteryRepository
import dev.PC22200322y22200350.pc1.presentation.AppPage
import dev.PC22200322y22200350.pc1.presentation.DeepForest
import dev.PC22200322y22200350.pc1.presentation.Eyebrow
import dev.PC22200322y22200350.pc1.presentation.Forest
import dev.PC22200322y22200350.pc1.presentation.Lime
import dev.PC22200322y22200350.pc1.presentation.MutedText
import dev.PC22200322y22200350.pc1.presentation.NumberBall
import dev.PC22200322y22200350.pc1.presentation.StatusCard
import dev.PC22200322y22200350.pc1.presentation.SuccessGreen

@Composable
fun FirestoreRealtimeScreen(
    email: String,
    uid: String,
    myNumbers: List<Int>,
    onSignOut: () -> Unit
) {
    var draw by remember { mutableStateOf<LotteryDraw?>(null) }
    var balls by remember { mutableStateOf(emptyList<DrawBall>()) }
    var drawError by remember { mutableStateOf<String?>(null) }
    var ballsError by remember { mutableStateOf<String?>(null) }
    var drawLoaded by remember { mutableStateOf(false) }

    DisposableEffect(uid) {
        val registrations = mutableListOf<ListenerRegistration>()
        registrations += LotteryRepository.listenDraw { updated, error ->
            draw = updated
            drawError = error
            drawLoaded = true
        }
        registrations += LotteryRepository.listenBalls { updated, error ->
            balls = updated.sortedBy { it.order }.take(6)
            ballsError = error
        }
        onDispose { registrations.forEach(ListenerRegistration::remove) }
    }

    val hitNumbers = myNumbers.toSet().intersect(balls.map { it.number }.toSet())
    val finalResult = draw?.isFinished == true && balls.size == 6

    AppPage {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("${draw?.nombre ?: "Sorteo de prueba"} · ${draw?.fecha.orEmpty()}")
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onSignOut, shape = RoundedCornerShape(18.dp)) { Text("Salir") }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Sigue el sorteo", style = MaterialTheme.typography.headlineMedium, color = DeepForest, fontWeight = FontWeight.Bold)
                    Text("Sesión: $email", color = MutedText, style = MaterialTheme.typography.bodySmall)
                }
                if (draw?.isRunning == true) {
                    Surface(color = Color(0xFFE8F3D2), shape = CircleShape) {
                        Text("●  EN VIVO", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Surface(color = Forest, shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("BOLILLAS PUBLICADAS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${balls.size.coerceAtMost(6)}", color = Lime, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold)
                        Text(" / 6", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(bottom = 7.dp))
                        Spacer(Modifier.weight(1f))
                        Text(
                            when {
                                !drawLoaded -> "CONECTANDO"
                                draw?.isOpen == true -> "ABIERTO"
                                draw?.isRunning == true -> "EN CURSO"
                                draw?.isFinished == true -> "FINALIZADO"
                                else -> "SIN ESTADO"
                            },
                            color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 9.dp)
                        )
                    }
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { balls.size.coerceAtMost(6) / 6f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Lime,
                        trackColor = Color.White.copy(alpha = .22f)
                    )
                }
            }

            Text("Bolillas sorteadas", fontWeight = FontWeight.Bold, color = DeepForest)
            if (balls.isEmpty()) {
                Text("Aún no se han publicado bolillas.", color = MutedText)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    balls.chunked(6).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                            row.forEach { ball ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    NumberBall(ball.number, selected = true, modifier = Modifier.size(44.dp))
                                    Text("${ball.order}°", color = MutedText, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Tu jugada", fontWeight = FontWeight.Bold, color = DeepForest)
                Spacer(Modifier.weight(1f))
                Text("${hitNumbers.size} aciertos", color = if (finalResult && hitNumbers.size == 6) SuccessGreen else MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                myNumbers.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)) {
                        row.forEach { number ->
                            NumberBall(number, selected = number in hitNumbers, modifier = Modifier.size(48.dp))
                        }
                    }
                }
            }

            when {
                !drawLoaded -> StatusCard("Conectando", "Esperando los datos del sorteo en Firestore…")
                drawError != null -> StatusCard("Error del sorteo", drawError.orEmpty())
                ballsError != null -> StatusCard("Error de bolillas", ballsError.orEmpty())
                draw == null -> StatusCard("Sorteo no encontrado", "No existe /sorteos/${LotteryRepository.TEST_DRAW_ID} en Firestore.")
                finalResult && hitNumbers.size == 6 -> StatusCard("¡GANADOR!", "Acertaste los seis números.", icon = "✓", tone = Color(0xFFE7F2D4))
                finalResult -> StatusCard("SIGUE INTENTANDO", "Tu resultado final es ${hitNumbers.size} de 6 aciertos.", icon = "i")
                draw?.isFinished == true -> StatusCard("Esperando resultados", "El sorteo finalizó; esperando las seis bolillas.")
                draw?.isOpen == true -> StatusCard("Sorteo abierto", "Espera a que comience el sorteo para ver las bolillas.")
                draw?.isRunning == true -> StatusCard("Sorteo en curso", "Las bolillas y tus aciertos se actualizan automáticamente.", icon = "●")
                else -> StatusCard("Estado no disponible", "Usa abierto, en curso o finalizado en Firestore.")
            }
            Text("Actualización automática desde Firestore", color = MutedText, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
        }
    }
}
