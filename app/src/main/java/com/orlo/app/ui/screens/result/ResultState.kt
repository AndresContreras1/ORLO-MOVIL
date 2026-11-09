package com.orlo.app.ui.screens.result

import androidx.annotation.StringRes
import com.orlo.app.data.model.ResultadoUi

data class ResultState(
    val resultado: ResultadoUi? = null,
    val isLoading: Boolean = true,
    @StringRes val errorMessageRes: Int? = null,
)
