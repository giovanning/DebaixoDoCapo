package com.projetos.filmei.debaixodocapo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projetos.filmei.debaixodocapo.R
import com.projetos.filmei.debaixodocapo.ui.viewmodel.AppListState
import com.projetos.filmei.debaixodocapo.ui.viewmodel.AppViewModel
import com.projetos.filmei.debaixodocapo.utils.AplicativoInstalado
import com.projetos.filmei.debaixodocapo.utils.AppFramework
import com.projetos.filmei.debaixodocapo.utils.DetalhesAppNativos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(viewModel: AppViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val mostrarAppsPessoais by viewModel.mostrarAppsPessoais.collectAsState()
    var indexTabSelecionada by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Debaixo do Capô") },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (mostrarAppsPessoais) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Switch(
                                checked = mostrarAppsPessoais,
                                onCheckedChange = { viewModel.switchMostrarAppsPessoais() },
                                thumbContent = null,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                )
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                TabRow(selectedTabIndex = indexTabSelecionada) {
                    Tab(
                        selected = indexTabSelecionada == 0,
                        onClick = { indexTabSelecionada = 0 },
                        text = { Text("Resumo") },
                        icon = { Icon(painterResource(R.drawable.pie_chart_24px), contentDescription = null) }
                    )
                    Tab(
                        selected = indexTabSelecionada == 1,
                        onClick = { indexTabSelecionada = 1 },
                        text = { Text("Lista") },
                        icon = { Icon(Icons.Default.List, contentDescription = null) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is AppListState.Loading -> {
                    CircularProgressIndicator()
                }
                is AppListState.Error -> {
                    Text(text = "Erro: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                is AppListState.Success -> {
                    if (indexTabSelecionada == 0) {
                        ResumoFrameworks(apps = state.apps)
                    } else {
                        ListaApp(apps = state.apps)
                    }
                }
            }
        }
    }
}

@Composable
fun ResumoFrameworks(apps: List<AplicativoInstalado>) {
    val contagem = apps.groupingBy { it.framework }.eachCount()
    val total = apps.size

    val nativeApps = apps.filter { it.framework == AppFramework.NATIVE }
    val onlyXml = nativeApps.count { it.detalhesNativos?.hasXml == true && it.detalhesNativos.hasCompose }
    val onlyCompose = nativeApps.count { it.detalhesNativos?.hasXml == false && it.detalhesNativos.hasCompose }
    val both = nativeApps.count { it.detalhesNativos?.hasXml == true && it.detalhesNativos.hasCompose }
    val stackNativaOculta = nativeApps.count { it.detalhesNativos?.hasCompose != true && it.detalhesNativos?.hasXml != true && it.detalhesNativos?.isKmp != true }

    val frameworks = AppFramework.entries.toTypedArray()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Total de Apps: $total",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        frameworks.toList().chunked(2).forEach { parDeFrameworks ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    parDeFrameworks.forEach { framework ->
                        Box(modifier = Modifier.weight(1f)) {
                            CardFramework(
                                framework = framework,
                                count = contagem[framework] ?: 0
                            )
                        }
                    }
                    if (parDeFrameworks.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "Detalhamento Nativo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow(label = "Apenas XML", count = onlyXml, color = Color(0xFFE65100))
                InfoRow(label = "Apenas Compose", count = onlyCompose, color = Color(0xFF2E7D32))
                InfoRow(label = "Híbrido (XML + Compose)", count = both, color = Color(0xFF6A1B9A))
                InfoRow(label = "Stack Nativa Oculta", count = stackNativaOculta, color = Color(0xFFF5F5F5))
            }
        }
    }
}

@Composable
fun InfoRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun CardFramework(framework: AppFramework, count: Int) {
    val (color, label) = when (framework) {
        AppFramework.NATIVE -> Color(0xFFEF6C00) to "Nativo"
        AppFramework.FLUTTER -> Color(0xFF2E7D32) to "Flutter"
        AppFramework.REACT_NATIVE -> Color(0xFF00838F) to "React Native"
        AppFramework.UNKNOWN -> Color(0xFF424242) to "Desconhecido"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ListaApp(apps: List<AplicativoInstalado>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(apps) { app ->
            AppItemCard(app)
        }
    }
}

@Composable
fun FrameworkBadge(framework: AppFramework) {
    val (backgroundColor, textColor, label) = when (framework) {
        AppFramework.FLUTTER -> Triple(Color(0xFFE1F5FE), Color(0xFF2E7D32), "Flutter")
        AppFramework.REACT_NATIVE -> Triple(Color(0xFFE0F7FA), Color(0xFF00838F), "React Native")
        AppFramework.NATIVE -> Triple(Color(0xFFE8F5E9), Color(0xFFEF6C00), "Nativo")
        AppFramework.UNKNOWN -> Triple(Color(0xFFEEEEEE), Color(0xFF424242), "Desconhecido")
    }

    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AppItemCard(app: AplicativoInstalado) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = app.nomePacote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FrameworkBadge(framework = app.framework)
            }

            if (app.framework == AppFramework.NATIVE && app.detalhesNativos != null) {
                Spacer(modifier = Modifier.height(12.dp))
                NativeDetailsRow(detalhes = app.detalhesNativos)
            }
        }
    }
}

@Composable
fun NativeDetailsRow(detalhes: DetalhesAppNativos) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (detalhes.hasCompose) {
            SmallBadge(text = "Compose", backgroundColor = Color(0xFFE8EAF6), textColor = Color(0xFF3F51B5))
        }
        if (detalhes.hasXml) {
            SmallBadge(text = "XML", backgroundColor = Color(0xFFFFF3E0), textColor = Color(0xFFE65100))
        }
        if (detalhes.isKmp) {
            SmallBadge(text = "KMP", backgroundColor = Color(0xFFF3E5F5), textColor = Color(0xFF6A1B9A))
        }

        if (!detalhes.hasCompose && !detalhes.hasXml && !detalhes.isKmp) {
            SmallBadge(text = "Stack Nativa Oculta", backgroundColor = Color(0xFFF5F5F5), textColor = Color(0xFF757575))
        }
    }
}

@Composable
fun SmallBadge(text: String, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
