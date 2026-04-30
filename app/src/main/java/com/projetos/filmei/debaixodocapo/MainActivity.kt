package com.projetos.filmei.debaixodocapo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.projetos.filmei.debaixodocapo.ui.screen.AppScreen
import com.projetos.filmei.debaixodocapo.ui.theme.DebaixoDoCapoTheme
import com.projetos.filmei.debaixodocapo.ui.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppViewModel(packageManager) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DebaixoDoCapoTheme {
                AppScreen(viewModel)
            }
        }
    }
}