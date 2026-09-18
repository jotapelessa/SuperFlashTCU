package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BottleneckItem
import com.example.data.model.FlashcardEntity
import com.example.data.model.L1DeckSummary
import com.example.data.model.StudyProgressReport
import com.example.ui.components.AiMarkdownContent

enum class TutorTab(val title: String) {
    DIAGNOSTIC("Diagnóstico Geral"),
    CONSULTATION("Consultoria & Perguntas"),
    BOTTLENECKS("Gargalos L1/L2/L3")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiTutorScreen(
    isLoading: Boolean,
    analysisResult: String?,
    errorMessage: String?,
    activeModelVersion: String,
    l1Decks: List<L1DeckSummary>,
    allCards: List<FlashcardEntity>,
    progressReport: StudyProgressReport,
    criticalBottlenecks: List<BottleneckItem> = emptyList(),
    onAnalyze: (customQuestion: String?) -> Unit,
    onClearAnalysis: () -> Unit,
    onStudyCriticalCards: ((List<FlashcardEntity>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var customQuery by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickPrompts = remember {
        listOf(
            "📊 Diagnóstico Geral L1/L2/L3",
            "⚡ O que estudar hoje com urgência?",
            "⚠️ Identifique meus gargalos de memória",
            "🎯 Plano para bater minha meta diária",
            "📈 Estratégia de retenção para concursos do TCU",
            "⚖️ Jurisprudência recente do TCU sobre licitações e contratos",
            "🏛️ Princípios de Controle Externo e competências constitucionais",
            "📑 Como memorizar os prazos da Lei 14.133/2021",
            "🔍 Auditoria Operacional vs Auditoria de Conformidade",
            "💰 Regras de Ouro da LRF e limites de despesa com pessoal",
            "📉 Curva de esquecimento: quais cards revisar antes do simulado?",
            "🔄 Estratégia de recuperação para cartões com muitos lapsos",
            "🧩 Mnemônicos infalíveis para Direito Administrativo",
            "💡 Diferenças fundamentais entre Restos a Pagar e DEA",
            "📊 Análise de Pareto: 20% das matérias que geram 80% das questões",
            "📝 Roteiro para elaboração de Relatório de Auditoria do TCU",
            "🎯 Como transformar erros repetidos em cards de gatilho ativo",
            "📚 Plano de revisão rápida de Direito Constitucional (Arts. 70 a 75)",
            "⏳ Cronograma de revisões espaçadas para reta final (30 dias)",
            "🧠 Como evitar a ilusão de competência ao avaliar facilidade no Anki",
            "🔬 Contabilidade Pública: lançamentos típicos do PCASP",
            "🏦 Demonstrações Contábeis Aplicadas ao Setor Público (DCASP)",
            "🧮 Auditoria de TI: COBIT, ITIL e governança pública",
            "🛡️ Lei Anticorrupção e Acordos de Leniência no âmbito do TCU",
            "⚡ Técnicas de memorização para fórmulas de Análise das Demonstrações",
            "📌 Tomada de Contas Especial (TCE): pressupostos e rito processual",
            "⚖️ Súmulas vinculantes e teses de repercussão geral para auditor",
            "🚨 Alerta de sobrecarga: estou estudando cards demais por dia?",
            "📋 Como formular perguntas discursivas a partir do meu acervo",
            "🎓 Ajuste fino do Ease Factor: quando diminuir ou aumentar?",
            "🎯 Plano de estudo intercalado (Interleaving) entre exatas e direito",
            "🔎 Detecção de 'Leaches' (cards sanguessugas) que travam o progresso",
            "📖 Estrutura de Parecer Técnico do Ministério Público junto ao TCU",
            "🌐 Governança e Gestão de Riscos (COSO ERM) no setor público",
            "🏆 Simulação de banca: arguição rápida sobre Controle da Administração"
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tutor IA Gemini",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF10B981), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = activeModelVersion,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (analysisResult != null) {
                        IconButton(
                            onClick = onClearAnalysis,
                            modifier = Modifier.testTag("btn_clear_tutor_analysis")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar Diagnóstico",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = { onAnalyze(null) },
                        enabled = !isLoading,
                        modifier = Modifier.testTag("btn_refresh_tutor_analysis")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Atualizar Diagnóstico",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Abas de Navegação Interna da Tela do Tutor
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                TutorTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            when (TutorTab.entries[selectedTabIndex]) {
                TutorTab.DIAGNOSTIC -> {
                    // Visualização Ampla e Fluida do Relatório de Diagnóstico
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("tutor_diagnostic_view"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card de Ação Rápida / Status Superior
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Diagnóstico Pedagógico de Alto Nível",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Avaliação contínua das disciplinas L1/L2/L3 com metodologia de Repetição Espaçada.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { onAnalyze(null) },
                                        enabled = !isLoading,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Analisar")
                                    }
                                }
                            }
                        }

                        when {
                            isLoading -> {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(44.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "O Gemini está processando as estatísticas...",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Avaliando curva de esquecimento, taxas de retenção e gargalos L1/L2/L3.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            errorMessage != null -> {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(40.dp)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "Não foi possível carregar a análise",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = errorMessage,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            OutlinedButton(
                                                onClick = { onAnalyze(null) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Tentar Novamente")
                                            }
                                        }
                                    }
                                }
                            }

                            analysisResult != null -> {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Relatório Pedagógico Ativo",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Diagnóstico Gemini SuperFlash", analysisResult)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Diagnóstico copiado!", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Copiar Relatório", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                        )
                                    ) {
                                        Box(modifier = Modifier.padding(16.dp)) {
                                            AiMarkdownContent(
                                                markdownText = analysisResult,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }

                            else -> {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(60.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Nenhum relatório gerado ainda",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Toque no botão abaixo para gerar uma análise profunda do seu progresso em todas as carreiras e disciplinas.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Button(
                                                onClick = { onAnalyze(null) },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Gerar Primeiro Diagnóstico")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                TutorTab.CONSULTATION -> {
                    // Área Dedicada de Perguntas e Respostas Livres com Dropdown Retrátil
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        var expandedPromptMenu by remember { mutableStateOf(false) }

                        Text(
                            text = "Sugestões de Análise Rápida:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        androidx.compose.material3.ExposedDropdownMenuBox(
                            expanded = expandedPromptMenu,
                            onExpandedChange = { expandedPromptMenu = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material3.OutlinedCard(
                                onClick = { expandedPromptMenu = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true)
                                    .testTag("dropdown_quick_prompts"),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Escolher análise rápida pronta...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPromptMenu)
                                }
                            }

                            ExposedDropdownMenu(
                                expanded = expandedPromptMenu,
                                onDismissRequest = { expandedPromptMenu = false }
                            ) {
                                quickPrompts.forEach { prompt ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Psychology,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = prompt,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        },
                                        onClick = {
                                            customQuery = prompt
                                            expandedPromptMenu = false
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Pergunta Personalizada ao Tutor:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customQuery,
                            onValueChange = { customQuery = it },
                            placeholder = { Text("Ex: Como melhorar em Direito Administrativo nos próximos 5 dias?") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("tutor_custom_query_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val queryToSend = customQuery.trim().ifBlank { null }
                                onAnalyze(queryToSend)
                                selectedTabIndex = 0 // Alterna para a visualização do relatório
                            },
                            enabled = !isLoading && customQuery.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_submit_tutor_query"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Consultar Tutor Gemini", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "💡 Dica de Utilização",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "O Tutor Gemini avalia automaticamente o seu banco de dados Room (cards vencidos, taxas SM-2, disciplinas com maior dificuldade). Você pode pedir planos de contingência, resumos de prioridade ou explicação conceitual das matérias.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                TutorTab.BOTTLENECKS -> {
                    // Visão Instantânea dos Gargalos de Disciplinas e Tópicos
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("tutor_bottlenecks_view"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Matérias com Alta Dificuldade ou Cards Vencidos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Detectado automaticamente a partir dos intervalos do algoritmo de repetição espaçada.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (criticalBottlenecks.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Nenhum gargalo crítico!",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Suas matérias estão com retenção acima de 50% e sem cards em atraso acumulado.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(criticalBottlenecks, key = { it.discipline }) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.discipline,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Vencidos: ${item.dueCards}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (item.dueCards > 0) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "Domínio: ${String.format("%.0f", item.masteryRate)}%",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (item.masteryRate < 50f) Color(0xFFF59E0B) else Color(0xFF10B981)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "Total: ${item.totalCards}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (item.avgStability > 0f) {
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = "S: ${String.format("%.1f", item.avgStability)}d",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }

                                        if (onStudyCriticalCards != null && item.dueCards > 0) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = {
                                                    val dueList = item.cards.filter { it.dueTimestamp <= System.currentTimeMillis() }
                                                    onStudyCriticalCards(if (dueList.isNotEmpty()) dueList else item.cards)
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Revisar", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
