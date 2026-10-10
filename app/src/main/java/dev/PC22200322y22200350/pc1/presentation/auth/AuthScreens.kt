package dev.PC22200322y22200350.pc1.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import dev.PC22200322y22200350.pc1.data.remote.AuthRepository
import dev.PC22200322y22200350.pc1.presentation.AppPage
import dev.PC22200322y22200350.pc1.presentation.BrandHeader
import dev.PC22200322y22200350.pc1.presentation.Eyebrow
import dev.PC22200322y22200350.pc1.presentation.Forest
import dev.PC22200322y22200350.pc1.presentation.MutedText
import dev.PC22200322y22200350.pc1.presentation.ScreenTitle

@Composable
fun LoginScreen(onCreateAccount: () -> Unit) {
    AuthForm(register = false, onSwitch = onCreateAccount)
}

@Composable
fun RegisterScreen(onGoToLogin: () -> Unit) {
    AuthForm(register = true, onSwitch = onGoToLogin)
}

@Composable
private fun AuthForm(register: Boolean, onSwitch: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val title = if (register) "Crea tu cuenta" else "Juega con tu suerte"

    AppPage {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 26.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            BrandHeader()
            Spacer(Modifier.height(46.dp))
            Eyebrow(if (register) "Únete a la jugada" else "Bienvenido")
            Spacer(Modifier.height(10.dp))
            ScreenTitle(title, if (register) "Regístrate para seguir el sorteo en vivo." else "Sigue cada bolilla, en tiempo real.")
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim(); error = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            if (register) {
                Spacer(Modifier.height(8.dp))
                Text("Usa al menos 6 caracteres.", color = MutedText, style = MaterialTheme.typography.bodySmall)
            }
            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    error = when {
                        email.isBlank() -> "Ingresa tu correo electrónico."
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Revisa el formato del correo."
                        password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
                        else -> null
                    }
                    if (error == null) {
                        loading = true
                        val task = if (register) AuthRepository.register(email, password) else AuthRepository.signIn(email, password)
                        task.addOnCompleteListener { result ->
                            loading = false
                            if (!result.isSuccessful) error = friendlyAuthError(result.exception)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = Forest)
            ) {
                Text(if (loading) "Un momento…" else if (register) "Crear cuenta" else "Iniciar sesión", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onSwitch, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(if (register) "¿Ya tienes cuenta? Inicia sesión" else "¿Aún no tienes cuenta? Crear cuenta", color = Forest)
            }
        }
    }
}

private fun friendlyAuthError(exception: Exception?): String {
    val code = (exception as? com.google.firebase.auth.FirebaseAuthException)?.errorCode
    return when (code) {
        "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "Ese correo ya tiene una cuenta. Inicia sesión."
        "ERROR_WEAK_PASSWORD" -> "Elige una contraseña de al menos 6 caracteres."
        "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Correo o contraseña incorrectos."
        "ERROR_TOO_MANY_REQUESTS" -> "Hubo demasiados intentos. Espera un momento y vuelve a probar."
        else -> exception?.localizedMessage ?: "No se pudo completar la autenticación. Revisa tu conexión e inténtalo de nuevo."
    }
}
