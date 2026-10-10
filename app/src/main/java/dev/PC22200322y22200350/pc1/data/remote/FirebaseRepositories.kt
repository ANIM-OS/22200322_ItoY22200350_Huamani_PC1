package dev.PC22200322y22200350.pc1.data.remote

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import dev.PC22200322y22200350.pc1.data.model.DrawBall
import dev.PC22200322y22200350.pc1.data.model.LotteryDraw
import dev.PC22200322y22200350.pc1.data.model.Play

object AuthRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun currentUser() = auth.currentUser
    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) = auth.addAuthStateListener(listener)
    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) = auth.removeAuthStateListener(listener)
    fun signIn(email: String, password: String) = auth.signInWithEmailAndPassword(email, password)
    fun register(email: String, password: String) = auth.createUserWithEmailAndPassword(email, password)
    fun signOut() = auth.signOut()
}

object LotteryRepository {
    const val TEST_DRAW_ID = "pc01_loteria_2026"
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val drawRef get() = firestore.collection("sorteos").document(TEST_DRAW_ID)

    fun userFacingError(exception: Throwable?): String {
        var current = exception
        while (current != null) {
            when ((current as? com.google.firebase.firestore.FirebaseFirestoreException)?.code) {
                com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    return "Firestore rechazó el acceso. Comprueba tu sesión y las reglas de acceso."
                com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE,
                com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                    return "No hay conexión con Firestore. Comprueba internet y vuelve a intentarlo."
                else -> Unit
            }
            val message = current.message
            val expectedMessage = listOf("El sorteo", "Ya tienes", "La jugada", "No existe el sorteo")
                .any { prefix -> message?.startsWith(prefix) == true }
            if (expectedMessage) {
                return message.orEmpty()
            }
            current = current.cause
        }
        return "No se pudo completar la operación. Revisa tu conexión e inténtalo de nuevo."
    }

    fun listenDraw(onChange: (LotteryDraw?, String?) -> Unit): ListenerRegistration =
        drawRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onChange(null, userFacingError(error))
            } else {
                onChange(snapshot?.toLotteryDraw(), null)
            }
        }

    fun listenBalls(onChange: (List<DrawBall>, String?) -> Unit): ListenerRegistration =
        drawRef.collection("bolillas")
            .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onChange(emptyList(), userFacingError(error))
            } else {
                val documents = snapshot?.documents.orEmpty()
                val decoded = documents.mapNotNull { doc ->
                    val number = doc.getLong("numero")?.toInt()
                    val order = doc.getLong("orden")?.toInt()
                    if (number != null && order != null && number in 1..45 && order in 1..6) {
                        DrawBall(number, order)
                    } else null
                }.sortedBy { it.order }
                val validationError = when {
                    decoded.size != documents.size -> "Hay bolillas con datos inválidos en Firestore."
                    decoded.map { it.order }.distinct().size != decoded.size -> "Hay órdenes de bolilla repetidos."
                    decoded.map { it.number }.distinct().size != decoded.size -> "El sorteo contiene números repetidos."
                    decoded.map { it.order } != (1..decoded.size).toList() -> "El orden de las bolillas debe comenzar en 1 y ser consecutivo."
                    else -> null
                }
                onChange(decoded, validationError)
            }
        }

    fun getPlay(uid: String): Task<DocumentSnapshot> = drawRef.collection("jugadas").document(uid).get()

    fun savePlay(uid: String, numbers: List<Int>): Task<Boolean> {
        val playRef = drawRef.collection("jugadas").document(uid)
        val payload = mapOf(
            "sorteoId" to TEST_DRAW_ID,
            "uid" to uid,
            "numeros" to numbers,
            "fechaRegistro" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        return firestore.runTransaction { transaction ->
            val draw = transaction.get(drawRef)
            val play = transaction.get(playRef)
            check(draw.exists()) { "No existe el sorteo configurado en Firestore." }
            check(draw.getString("estado").equals(LotteryDraw.STATE_OPEN, ignoreCase = true)) {
                "El sorteo ya comenzó. No puedes registrar ni modificar tu jugada."
            }
            check(!play.exists()) { "Ya tienes una jugada registrada para este sorteo." }
            check(numbers.size == 6 && numbers.distinct().size == 6 && numbers.all { it in 1..45 }) {
                "La jugada debe tener seis números distintos entre 1 y 45."
            }
            transaction.set(playRef, payload, SetOptions.merge())
            true
        }
    }

    fun DocumentSnapshot.toLotteryDraw(): LotteryDraw? {
        if (!exists()) return null
        return LotteryDraw(
            id = id,
            nombre = getString("nombre").orEmpty(),
            fecha = getString("fecha").orEmpty(),
            estado = getString("estado").orEmpty()
        )
    }

    fun DocumentSnapshot.toPlay(): Play? {
        if (!exists()) return null
        val rawNumbers = get("numeros") as? List<*> ?: return null
        val numbers = rawNumbers.mapNotNull { (it as? Number)?.toInt() }
        if (numbers.size != 6 || numbers.distinct().size != 6 || numbers.any { it !in 1..45 }) return null
        return Play(
            drawId = getString("sorteoId") ?: TEST_DRAW_ID,
            uid = getString("uid") ?: id,
            numbers = numbers
        )
    }
}
