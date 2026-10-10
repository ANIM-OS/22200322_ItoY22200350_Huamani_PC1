package dev.PC22200322y22200350.pc1.data.remote

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
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

    fun listenDraw(onChange: (LotteryDraw?, String?) -> Unit): ListenerRegistration =
        drawRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onChange(null, error.localizedMessage ?: "No se pudo escuchar el sorteo.")
            } else {
                onChange(snapshot?.toLotteryDraw(), null)
            }
        }

    fun listenBalls(onChange: (List<DrawBall>, String?) -> Unit): ListenerRegistration =
        drawRef.collection("bolillas")
            .orderBy("orden", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onChange(emptyList(), error.localizedMessage ?: "No se pudieron cargar las bolillas.")
                } else {
                    val balls = snapshot?.documents.orEmpty().mapNotNull { doc ->
                        val number = doc.getLong("numero")?.toInt()
                        val order = doc.getLong("orden")?.toInt()
                        if (number != null && order != null && number in 1..45 && order in 1..6) {
                            DrawBall(number, order)
                        } else null
                    }
                    onChange(balls, null)
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
