package dev.PC22200322y22200350.pc1.data.model

data class LotteryDraw(
    val id: String,
    val nombre: String,
    val fecha: String,
    val estado: String
) {
    val isOpen: Boolean get() = estado.equals(STATE_OPEN, ignoreCase = true)
    val isRunning: Boolean get() = estado.equals(STATE_RUNNING, ignoreCase = true)
    val isFinished: Boolean get() = estado.equals(STATE_FINISHED, ignoreCase = true)

    companion object {
        const val STATE_OPEN = "abierto"
        const val STATE_RUNNING = "en curso"
        const val STATE_FINISHED = "finalizado"
    }
}

data class DrawBall(val number: Int, val order: Int)
