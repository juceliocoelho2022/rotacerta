package com.jucelio.rotacerta.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SplashUiState(
    val isReady: Boolean = false
)

/**
 * ViewModel da inicialização do aplicativo.
 *
 * Hoje a Splash aguarda apenas a animação mínima de apresentação. Este ponto
 * fica preparado para, nas próximas sprints, validar sessão, configurações
 * locais e disponibilidade de serviços antes de decidir a tela de destino.
 */
@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SplashUiState())
    val state: StateFlow<SplashUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            delay(SPLASH_DURATION_MS)
            _state.update { it.copy(isReady = true) }
        }
    }

    private companion object {
        const val SPLASH_DURATION_MS = 1_300L
    }
}
