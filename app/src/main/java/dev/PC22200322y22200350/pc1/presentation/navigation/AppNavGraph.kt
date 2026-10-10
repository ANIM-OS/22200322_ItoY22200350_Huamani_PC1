package dev.PC22200322y22200350.pc1.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dev.PC22200322y22200350.pc1.data.model.Play
import dev.PC22200322y22200350.pc1.data.remote.AuthRepository
import dev.PC22200322y22200350.pc1.data.remote.LotteryRepository
import dev.PC22200322y22200350.pc1.data.remote.LotteryRepository.toPlay
import dev.PC22200322y22200350.pc1.presentation.AppPage
import dev.PC22200322y22200350.pc1.presentation.Forest
import dev.PC22200322y22200350.pc1.presentation.LoadingMessage
import dev.PC22200322y22200350.pc1.presentation.auth.LoginScreen
import dev.PC22200322y22200350.pc1.presentation.auth.RegisterScreen
import dev.PC22200322y22200350.pc1.presentation.home.HomeScreen
import dev.PC22200322y22200350.pc1.presentation.realtime.FirestoreRealtimeScreen

private enum class AuthPage { LOGIN, REGISTER }

@Composable
fun AppNavGraph() {
    var user by remember { mutableStateOf(AuthRepository.currentUser()) }
    var authPage by remember { mutableStateOf(AuthPage.LOGIN) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            user = auth.currentUser
            if (auth.currentUser == null) authPage = AuthPage.LOGIN
        }
        AuthRepository.addAuthStateListener(listener)
        onDispose { AuthRepository.removeAuthStateListener(listener) }
    }

    val signedInUser = user
    if (signedInUser == null) {
        when (authPage) {
            AuthPage.LOGIN -> LoginScreen(onCreateAccount = { authPage = AuthPage.REGISTER })
            AuthPage.REGISTER -> RegisterScreen(onGoToLogin = { authPage = AuthPage.LOGIN })
        }
    } else {
        AuthenticatedFlow(user = signedInUser, onSignOut = { AuthRepository.signOut() })
    }
}

@Composable
private fun AuthenticatedFlow(user: FirebaseUser, onSignOut: () -> Unit) {
    var play by remember(user.uid) { mutableStateOf<Play?>(null) }
    var loading by remember(user.uid) { mutableStateOf(true) }
    var error by remember(user.uid) { mutableStateOf<String?>(null) }
    var ready by remember(user.uid) { mutableStateOf(false) }

    fun loadPlay() {
        loading = true
        error = null
        LotteryRepository.getPlay(user.uid)
            .addOnSuccessListener { snapshot ->
                val savedPlay = snapshot.toPlay()
                if (snapshot.exists() && savedPlay == null) {
                    error = "Tu jugada existe en Firestore, pero sus datos no cumplen el formato esperado."
                    ready = false
                } else {
                    play = savedPlay
                    ready = true
                }
                loading = false
            }
            .addOnFailureListener { exception ->
                error = exception.localizedMessage ?: "No se pudo recuperar tu jugada."
                ready = false
                loading = false
            }
    }

    androidx.compose.runtime.LaunchedEffect(user.uid) { loadPlay() }

    when {
        loading -> AppPage {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
                LoadingMessage("Recuperando tu jugada…")
            }
        }
        error != null -> AppPage {
            Column(
                Modifier.fillMaxSize().padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No se pudo conectar con Firestore", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(error.orEmpty())
                Spacer(Modifier.height(18.dp))
                Button(onClick = { loadPlay() }, colors = ButtonDefaults.buttonColors(containerColor = Forest)) { Text("Reintentar") }
                Button(onClick = onSignOut) { Text("Cerrar sesión") }
            }
        }
        ready && play != null -> FirestoreRealtimeScreen(
            email = user.email.orEmpty(),
            uid = user.uid,
            myNumbers = play!!.numbers,
            onSignOut = onSignOut
        )
        ready -> HomeScreen(
            email = user.email.orEmpty(),
            uid = user.uid,
            onSignOut = onSignOut,
            onPlaySaved = { numbers ->
                play = Play(LotteryRepository.TEST_DRAW_ID, user.uid, numbers)
                ready = true
            }
        )
    }
}
