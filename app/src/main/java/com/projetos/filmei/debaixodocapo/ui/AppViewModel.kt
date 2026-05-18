package com.projetos.filmei.debaixodocapo.ui

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projetos.filmei.debaixodocapo.utils.AplicativoInstalado
import com.projetos.filmei.debaixodocapo.utils.AppAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AppListState {
    object Loading : AppListState()
    data class Success(val apps: List<AplicativoInstalado>) : AppListState()
    data class Error(val message: String) : AppListState()
}

class AppViewModel(packageManager: PackageManager) : ViewModel() {

    private val analyzer = AppAnalyzer(packageManager)

    private val _todosApps = MutableStateFlow<List<AplicativoInstalado>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _mostrarAppsPessoais = MutableStateFlow(true)
    val mostrarAppsPessoais: StateFlow<Boolean> = _mostrarAppsPessoais

    val uiState: StateFlow<AppListState> = combine(
        _todosApps,
        _isLoading,
        _errorMessage,
        _mostrarAppsPessoais
    ) { apps, loading, error, mostrarAppsPessoais ->
        when {
            error != null -> AppListState.Error(error)
            loading -> AppListState.Loading
            else -> {
                val filteredApps = if (mostrarAppsPessoais) {
                    apps
                } else {
                    apps.filter { !it.nomePacote.startsWith("com.projetos.filmei") }
                }
                AppListState.Success(filteredApps)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppListState.Loading
    )

    init {
        carregarApps()
    }

    fun carregarApps() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val apps = analyzer.analisarAplicativosInstalados()
                _todosApps.value = apps
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Erro desconhecido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun switchMostrarAppsPessoais() {
        _mostrarAppsPessoais.value = !_mostrarAppsPessoais.value
    }
}
