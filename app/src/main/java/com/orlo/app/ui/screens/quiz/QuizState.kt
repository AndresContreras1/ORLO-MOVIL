package com.orlo.app.ui.screens.quiz

import androidx.annotation.StringRes
import com.orlo.app.data.model.CuestionarioUi
import com.orlo.app.data.model.PreguntaUi

data class QuizState(
    val cuestionario: CuestionarioUi = CuestionarioUi(),
    val indice: Int = 0,
    // Las opciones elegidas en cada pregunta, por el id de la pregunta
    val respuestas: Map<Long, Set<Long>> = emptyMap(),
    val isLoading: Boolean = true,
    @StringRes val errorCargaRes: Int? = null,
    @StringRes val errorMessageRes: Int? = null,
    val enviando: Boolean = false,
    val navigate: Boolean = false,
) {
    val total: Int get() = cuestionario.preguntas.size

    val preguntaActual: PreguntaUi? get() = cuestionario.preguntas.getOrNull(indice)

    val seleccionActual: Set<Long>
        get() = preguntaActual?.let { respuestas[it.id] } ?: emptySet()

    val esUltima: Boolean get() = indice == total - 1

    // De 0 a 1 para la barra de progreso: cuenta la pregunta en la que esta
    val progreso: Float get() = if (total == 0) 0F else (indice + 1).toFloat() / total
}
