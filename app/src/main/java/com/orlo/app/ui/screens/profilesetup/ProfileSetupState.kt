package com.orlo.app.ui.screens.profilesetup

import androidx.annotation.StringRes
import com.orlo.app.data.model.PerfilUi

data class ProfileSetupState(
    val perfil: PerfilUi = PerfilUi(),
    val isLoading: Boolean = true,
    @StringRes val errorCargaRes: Int? = null,
    @StringRes val errorMessageRes: Int? = null,
    val guardando: Boolean = false,
    val navigate: Boolean = false,
)
