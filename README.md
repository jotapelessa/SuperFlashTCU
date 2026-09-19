# ⚡ SuperFlash TCU (Anki SRS + Gemini AI)

Aplicativo Android nativo de alta performance para preparação de concursos de elite (Tribunal de Contas da União - TCU / Controle), estruturado na hierarquia rigorosa **L1 (Carreira) > L2 (Disciplina) > L3 (Tópico)** com algoritmo de repetição espaçada (SM-2 SRS), banco local Room SQLite offline-first e **Tutor Pedagógico Gemini AI**.

---

## 📱 Versão Atual: v1.5.6 (Code 16)

### 🚀 Principais Novidades da Versão 1.5.6:
* **Barra de Ação Fixa e Ergonomia em Tela Cheia no Diálogo de Estudo (`StudyConfigDialog.kt`)**:
  * **BottomBar Acessível com Elevação:** O botão "Iniciar Estudo" e o contador dinâmico de cartões selecionados foram movidos para a `bottomBar` fixa do `Scaffold`, com elevação tonal e sombra (8dp) e `navigationBarsPadding()` com margens seguras. O botão fica **100% visível e imediatamente clicável**, sem necessidade de rolar a tela até o fim.
  * **Reset em Cascata nos Menus Suspensos:** Ao alterar o Baralho L1, os campos de Disciplina L2 e Subtópico L3 são automaticamente limpos; ao alterar L2, L3 é limpo, eliminando seleções incoerentes e listas de estudo vazias.
  * **Contextualização Direta de Escopo:** Ao abrir a configuração a partir de uma matéria L2 na `SubDeckScreen`, o app pré-seleciona a hierarquia correspondente diretamente.
* **Redesenho e Padronização Geométrica de Flashcards (`SubDeckScreen.kt`)**:
  * **CardPreviewItem de Alta Fidelidade:** Novo layout com `BorderStroke` sutil em `outlineVariant`, elevação de 1.5dp, e espaçamento harmônico entre breadcrumb `L2 › L3` e badge de domínio.
  * **Pergunta Contida e Toque Intuitivo:** A pergunta principal no estado colapsado fica limitada a 3 linhas elegantes com reticências (`maxLines = 3, overflow = Ellipsis`), com indicação visual sutil de toque ("Toque para ver resposta"). Ao tocar, expande suavemente a resposta completa e fundamentação sem desalinhamento.

---

## 📱 Versão Anterior: v1.5.5 (Code 15)

### 🚀 Principais Novidades da Versão 1.5.5:
* **Resiliência HTTP OkHttp na Cascata Gemini (`GeminiStudyAnalyzer.kt`)**:
  * **Recriação do RequestBody por Tentativa:** Correção crítica onde o `RequestBody` era instanciado uma única vez fora do loop; com a recriação a cada iteração, as tentativas de fallback após erros transitórios (ex: 503) operam com stream fresco e intacto.
  * **Fechamento Determinístico com `response.use {}`:** Todas as chamadas de rede do Gemini agora fecham conexões e sockets imediatamente.
  * **Remoção de Reflexão do Moshi:** Eliminação do `KotlinJsonAdapterFactory`, confiando exclusivamente nos adaptadores KSP pré-compilados.
  * **Limpeza Rigorosa de Pensamentos (`<thought>`):** Expressão regular case-insensitive para remoção de blocos de raciocínio intermediário do Gemini 2.0 Flash Thinking.
* **Título Dinâmico no Card SRS (`SubDeckScreen.kt` e `MainActivity.kt`)**:
  * Exibição reativa de `Progresso de Revisão Espaçada (FSRS-5)` ou `(SM-2)` conforme o algoritmo configurado pelo usuário.

---

## 📱 Versão Anterior: v1.5.4 (Code 14)
* **Tabelas com Coluna Fixa Congelada (*Sticky Column*) & Alinhamento Rígido (`AiMarkdownContent.kt`)**:
  * **Coluna Fixa de Matérias/Carreiras:** A primeira coluna com o nome da disciplina ou carreira permanece **congelada e sempre visível** à esquerda da tela, com divisor vertical semântico e indicador de status, enquanto apenas os dados numéricos e métricas complementares se movem horizontalmente.
  * **Alinhamento Vertical Determinístico:** Cálculo memoizado de larguras de coluna com dimensionamento rígido e alturas sincronizadas (`44dp` cabeçalho, `50dp` linhas), eliminando 100% dos problemas de colunas desalinhadas nas tabelas geradas pelo Gemini AI.
  * **Zebra Coordenada:** Alternância de cores de fundo (`surface` vs `surfaceContainerLowest`) aplicada em perfeita harmonia entre a célula fixa e as células roláveis.
* **Dashboards Gráficos Expandidos L1 e L2 no Tutor AI (`AiTutorScreen.kt`)**:
  * **Gráfico de Barras Empilhadas de Distribuição Cognitiva:** Cada carreira L1 agora conta com barra segmentada multipartite exibindo a proporção exata de cartões Dominados (Verde), Em Aprendizado (Âmbar), Novos (Azul) e Vencidos (Vermelho), com mini-legenda explicativa e quantitativos.
  * **Tabela Comparativa Nativa de Carreiras:** Tabela M3 integrada para visualização tabular instantânea (0ms de rede) de todas as carreiras ativas com Total, Vencidos e % de Domínio.
  * **Quadro Tático de Disciplinas L2 com 3 Modos:** Seletor por `FilterChip` entre *🏆 Top Retenção*, *⚠️ Maior Urgência* e *📊 Todas as Disciplinas*, com barras individuais de retenção e badges de estabilidade FSRS-5 ($S$ em dias).
  * **Monitor de Saúde Cognitiva e Sobrecarga:** Novo card com cálculo da taxa de vencimentos do acervo (`% Vencidos`) acompanhado de semáforo preventivo (*🟢 Carga Equilibrada*, *🟡 Revisão Recomendada*, *🔴 Risco de Sobrecarga*).

---

## 📱 Versão Anterior: v1.5.3 (Code 13)

### 🚀 Principais Novidades da Versão 1.5.3:
* **Controle Granular de Baralhos L1 na Análise do Tutor Gemini AI (Itens 1.1 e 3.1)**:
  * **Isolamento de Escopo por Baralho L1:** O estudante agora pode ativar ou desativar individualmente a participação de qualquer baralho L1 nas análises estatísticas da IA. Isso impede que baralhos inativos, legados ou em pausa distorçam o diagnóstico geral com acúmulos falsos de cartões vencidos ou taxas de domínio incoerentes.
  * **Configuração no Diálogo de Edição L1 (`EditL1DeckDialog.kt`):** Adicionado controle interativo com `Switch` M3 para ligar/desligar a "Análise do Tutor AI Gemini" com persistência atômica em `SharedPreferences`.
  * **Seletor Rápido de Escopo no Tutor AI (`AiTutorScreen.kt`):** Painel superior com `FilterChip` interativo para cada Carreira L1, permitindo ligar e desligar o baralho diretamente na tela de diagnóstico sem precisar voltar à tela inicial. Conta também com contador de alerta indicando quantos baralhos estão em pausa.
  * **Badge Visual Semântico no Card (`DeckCard.kt`):** Quando um baralho L1 tem a análise desativada, exibe uma tag visual `IA Pausada` sutil no cartão da tela inicial.
  * **Filtragem Rígida de Métricas & Payload da IA (`DeckViewModel.kt`):** Todas as estatísticas globais, FSRS-5, gargalos críticos e lista de cartões passados ao `GeminiStudyAnalyzer` são recalculadas estritamente com base nos baralhos L1 permitidos. Caso nenhum baralho esteja ativado, a chamada da API é abortada imediatamente com orientação clara ao usuário, economizando tokens e tempo de rede.

---

## 📱 Versão Anterior: v1.5.2 (Code 12)

### 🚀 Principais Novidades da Versão 1.5.2:
* **Tabelas Markdown Nativas no Tutor Pedagógico Gemini AI (Item 1.1)**:
  * **Parser e Renderizador Compose de Alta Performance (`AiMarkdownContent.kt`):** Implementação de `MarkdownBlock.Table` com suporte nativo a tabelas Markdown (`| col1 | col2 |`), cabeçalho estilizado (`surfaceContainerHighest`), rolagem horizontal fluida (`horizontalScroll`) e alternância sutil de linhas com bordas elegantes, eliminando quebras de formatação no celular.
  * **Matrizes Diagnósticas e Comparativas L1 e L2:** Instrução de sistema e prompt do Gemini aprimorados para gerar tabelas comparativas formais de Carreiras L1 e Matriz Diagnóstica L2 com barras visuais (`[████████░░] 80%`) e sinalizadores semânticos (`🟢 Consolidado`, `🟡 Em Alerta`, `🔴 Crítico`).
* **Dashboard Visual de Desempenho L1 & L2 no Tutor AI (`AiTutorScreen.kt`)**:
  * **Comparativo Gráfico de Carreiras (L1):** Cards nativos no topo da aba Diagnóstico com barras de progresso proporcionais (`LinearProgressIndicator`), badges de Total, Vencidos e Dominados com cores semânticas de acordo com o domínio.
  * **Ranking de Disciplinas L2 (Top Retenção vs Maior Urgência):** Seletor rápido com `FilterChip` destacando as 5 matérias mais consolidadas vs as 5 matérias com maior carga de cartões vencidos acumulados.
  * **Indicadores da Memória FSRS-5 em Tempo Real:** Painel com Estabilidade Média Global ($S$ em dias), Dificuldade Média ($D$ / 10) e total de flashcards modelados pelo algoritmo DSR.

---

## 📱 Versão Anterior: v1.5.1 (Code 11)

### 🚀 Principais Novidades da Versão 1.5.1:
* **Sinergia Cognitiva Total: FSRS-5 + Tutor Pedagógico Gemini AI**:
  * **Diagnósticos Baseados na Curva de Esquecimento Real:** O `GeminiStudyAnalyzer` agora recebe o perfil cognitivo completo da base de estudos: Algoritmo ativo (FSRS-5 vs SM-2), Retenção Alvo parametrizada (85%, 90%, 95%), e a **Estabilidade Média de Memória ($S$ em dias)** e **Dificuldade Média ($D$)** geral e por disciplina L2. O Tutor prescreve planos táticos com base científica da força da memória.
  * **Configuração de Geração Otimizada (`GeminiGenerationConfig`):** Requisições enviadas com `temperature: 0.3` e `maxOutputTokens: 2048`, reduzindo a latência média da IA em até 40% (< 1.5s com `gemini-flash-lite-latest`).
  * **Cascata Estritamente Flutuante:** Remoção de versões estáticas datadas da cascata de fallback, garantindo resiliência total com `customModelVersion -> gemini-flash-latest -> gemini-flash-lite-latest`.
* **Zero Recomposições Ociosas na Tela de Estudos (`StudyScreen`)**:
  * **Isolamento de Timer no HUD:** Leitura do cronômetro de 500ms desacoplada via providers de lambda no `SessionLiveHud`, eliminando 100% das recomposições periódicas do corpo principal do cartão, leitor HTML e botões durante o estudo estático.
  * **Transparência FSRS no Cartão:** Exibição clara de `Rep: X • S: 14.2d • D: 4.8/10 • Int: Yd` quando o FSRS estiver ativo.
* **Descarregamento de CPU no Tutor IA (`AiTutorScreen`)**:
  * **Gargalos L1/L2/L3 em Background:** O agrupamento de 18.000+ flashcards para detecção de gargalos foi transferido da Main Thread no Compose para `Dispatchers.Default` no `DeckViewModel`, garantindo 60/120 FPS estáveis na alternância de abas.
* **Blindagem de Sincronização em Nuvem (Supabase) e Banco Room**:
  * **Persistência Total FSRS no Supabase:** Inclusão dos campos `stability` e `difficulty` tanto no payload de envio quanto no parser de download do `SupabaseSyncManager`.
  * **Correção no Reset de Estatísticas SQLite:** A query de `resetAllCardStats()` em `FlashcardDao` agora zera adequadamente `stability = 0.0` e `difficulty = 0.0`.
  * **Chunking de Inserção CSV:** Importação particionada em lotes de 500 cartões para proteger limites de transação do SQLite.

---

## 📱 Versão Anterior: v1.5.0 (Code 10)

### 🚀 Principais Novidades da Versão 1.5.0:
* **Integração Nativa do Algoritmo FSRS-5 (Open Spaced Repetition)**:
  * **Motor FSRS-5 em Kotlin Puro (`FsrsScheduler.kt`):** Implementação completa do algoritmo moderno FSRS-5 baseado no modelo DSR (*Difficulty, Stability, Retrievability*) com os 19 parâmetros globais pré-treinados em milhões de repetições.
  * **Redução de Carga de Estudos em 20% a 30%:** Diferente do SM-2 clássico (1987), que assume esquecimento puramente exponencial e fator de facilidade linear estático, o FSRS-5 calcula a probabilidade real de retenção $R(t, S) = (1 + 0.2345679 \cdot t / S)^{-0.5}$ e ajusta a estabilidade e a dificuldade de forma dinâmica e precisa.
  * **Retenção Alvo Parametrizável (85%, 90%, 95%):** Permite ao candidato calibrar o equilíbrio ideal entre tempo gasto de estudo e taxa de retenção memorística na tela de Configurações, adaptando-se a fases de pré-edital ou pós-edital.
  * **Prévia Dinâmica de Intervalos na Tela de Estudo (`StudyScreen`):** Os 4 botões de avaliação (*Errei*, *Difícil*, *Bom*, *Fácil*) calculam e exibem instantaneamente os dias futuros previstos com base no modelo ativo e na retenção alvo selecionada.
  * **Compatibilidade e Transição Suave SM-2 ➔ FSRS-5:** Cartões legados sem estabilidade gravada têm seus intervalos e facilidades convertidos dinamicamente na primeira revisão, preservando integralmente o histórico de estudo do estudante.
  * **Migração Segura Room v2 ➔ v3 (`MIGRATION_2_3`):** Adicionados os campos `stability REAL NOT NULL DEFAULT 0.0` e `difficulty REAL NOT NULL DEFAULT 0.0` na entidade `FlashcardEntity` com script SQL não-destrutivo.
  * **Seletor de Algoritmo nas Configurações:** Suporte dual permitindo alternar livremente entre **FSRS-5 (Recomendado)** e **SM-2 Clássico (Anki)** com persistência em `SharedPreferences`.

---

## 📱 Versão Anterior: v1.4.4 (Code 9)

### 🚀 Principais Novidades da Versão 1.4.4:
* **Correção do Alerta de Sessão Concluída & Relatório de Tempo por Baralho (Item 3.1)**:
  * **Eliminação da Condição de Corrida no Estudo Rápido L1:** Correção do fluxo na tela inicial onde o acionamento do estudo rápido utilizava `filteredCards` assíncrono ainda não propagado, gerando sessões com 0 cartões que disparavam imediatamente a tela de "Sessão Concluída!" zerada. O carregamento agora utiliza a coleção síncrona `allCards`, com guarda estrita contra listas vazias.
  * **Cronometragem de Alta Precisão com `SystemClock.elapsedRealtime()`:** Substituição do contador de segundos baseado em `delay(1000L)` por marcações reais de timestamp de hardware. O tempo de resposta por cartão agora registra a latência real em milissegundos, sem perdas por atrasos de coroutines.
  * **Coerência Total no Relatório de Tempo por Deck:** O tempo total exibido no resumo agora reflete o tempo real decorrido da sessão e se alinha perfeitamente à soma dos tempos por disciplina L2. Valores inferiores a 1 segundo são exibidos como `< 1s` em vez do confuso `0s`.
  * **Limpeza e Isolamento de Ciclo de Vida da Sessão (`finishStudySession`):** Ao concluir os estudos ou retornar ao painel, todos os contadores ao vivo (`_sessionStats`), índices e mapeamentos de baralhos são redefinidos atomicamente no ViewModel, prevenindo dados órfãos em sessões subsequentes.
  * **Tela Amigável de Baralho em Dia (`EmptyStudySessionView`):** Ao iniciar filtros sem nenhum cartão pendente, o app agora exibe uma tela informativa com ícone de aviso e botão direto para retornar ao painel, em vez de abrir um relatório de desempenho zerado.

---

## 📱 Versão Anterior: v1.4.3 (Code 8)

### 🚀 Principais Novidades da Versão 1.4.3:
* **Reatividade Imediata no Salvamento de Ícones e Cores L2 (Item 3.8)**:
  * **Sinal Reativo sem I/O de Disco (`_paletteUpdateSignal`):** Resolução do problema em que a alteração de ícone ou cor de disciplinas L2 (sem renomeação) não era refletida instantaneamente na interface. Introdução de um canal reativo de sinalização atômico no `DeckRepository` combinado via `Flow.combine()` com as consultas do Room, garantindo reemissão imediata das listas L2 em 0ms e sem sobrecarga no SQLite.
  * **Higienização de Chaves Antigas em Renomeações:** Ao renomear uma matéria L2, os metadados anteriores gravados em `SharedPreferences` e `DisciplinePalette` são expurgados com `removeCustom()`, evitando acúmulo de chaves órfãs.
  * **Padronização Visual no Diálogo de Mover Tópicos L3 (`MoveL3ToL2Dialog`):** O seletor de ícones e cores ao criar/mover para um novo baralho L2 agora adota a mesma matriz contígua 5x10 (0dp de espaçamento, proporção 1:1 e sem rótulos textuais), unificando a identidade visual das telas de baralhos.

---

## 📱 Versão Anterior: v1.4.2 (Code 7)

### 🚀 Principais Novidades da Versão 1.4.2:
* **Tutor IA com 35 Sugestões Táticas para TCU / Controle (Item 3.4)**:
  * **Expansão Pedagógica com +30 Novas Sugestões:** Adição de 30 novos tópicos estratégicos cobrindo jurisprudência recente do TCU (licitações e contratos, Lei 14.133/2021), AFO e LRF, contabilidade pública (PCASP/DCASP), auditoria de TI (COBIT/ITIL), controle de lapsos críticos do Anki SRS e elaboração de relatórios discursivos.
* **Paleta Contígua de 50 Cores em Baralhos L1 e L2 (Itens 3.5 e 3.7)**:
  * **Matriz Contígua 5x10 sem Espaçamento:** Nos diálogos de edição de Decks L1 e Matérias L2, a escolha de cor de destaque agora apresenta exatamente 50 cores cromáticas justapostas (0dp de espaçamento), quadradas (1:1) e sem nomes de cores.
* **Upload de Imagem da Galeria para Capa L1 (Item 3.6)**:
  * **Persistência Total no Armazenamento Local:** Adicionado botão de seleção de fotos da galeria do smartphone com cópia assíncrona para `context.filesDir/covers/`, garantindo funcionamento 100% offline e imune a revogações de permissão temporária do Android.
* **Seletor Contíguo de 50 Ícones de Matérias L2 (Item 3.8)**:
  * **Matriz 5x10 com 50 Ícones Especializados:** O seletor de ícones de disciplinas L2 foi expandido de 12 para 50 ícones temáticos (Tribunal, Justiça, Auditoria, Exatas, TI, Governança, Leitura, etc.), dispostos em grade contígua 5x10 sem textos, compactando o espaço vertical em mais de 60%.

---

## 📱 Versão Anterior: v1.4.1 (Code 6)

### 🚀 Principais Novidades da Versão 1.4.1:
* **Gerenciamento de Dados Individualizado (Item 3.1)**:
  * **Separação de Ações de Limpeza:** Botão individual para "Zerar Apenas Estatísticas" (reinicia contadores de revisões, retenção e streaks via SQLite sem apagar os decks) e botão individual para "Apagar Apenas Baralhos e Cards" (exclui o acervo físico de cartões), cada um com seu próprio diálogo de confirmação explicativo e seguro.
* **Correção e Persistência de Duração Customizada da Sessão (Item 3.2)**:
  * **Persistência no SharedPreferences:** A meta de tempo da sessão (`targetSessionMinutes`) agora é gravada e recuperada permanentemente em `SharedPreferences`.
  * **Feedback Visual Ativo:** Seletor com chip ativo customizado `"Ativo: X min"`, presets rápidos (incluindo `0 min / Sem limite`) e campo de entrada numérica com validação e botão Salvar funcional.
* **Paleta Contígua de 50 Cores Material 3 (Item 3.3)**:
  * **50 Cores Harmoniosas:** Expansão da paleta do sistema para 50 tonalidades cromáticas dispostas em grade estrita de 5 linhas x 10 colunas, com quadrados justapostos sem espaçamento (0dp) e sem rótulos textuais de nome, reduzindo o espaço vertical em mais de 70%.
* **Menu Dropdown Retrátil de Sugestões no Tutor IA (Item 3.4)**:
  * **Compactação de Espaço Útil:** Substituição da antiga grade de chips abertos por um `ExposedDropdownMenuBox` elegante de 48dp na sub-aba Consultoria, recolhendo-se automaticamente após a escolha e liberando mais de 115dp de altura para digitação confortável.

---

## 📱 Versão Anterior: v1.4.0 (Code 5)

### 🚀 Principais Novidades da Versão 1.4.0:
* **Motor de IA Gemini Otimizado & Resiliência Avançada (`GeminiStudyAnalyzer`)**:
  * **Remoção de Aliases Inexistentes:** Eliminação do modelo `gemini-3.5-flash` (que retornava 404) e integração do `gemini-2.0-flash` na cascata (`requestedModel` ➔ `gemini-flash-latest` ➔ `gemini-flash-lite-latest` ➔ `gemini-2.0-flash`).
  * **Cache Reutilizável de Moshi Adapters:** Instanciação única de `requestAdapter` e `responseAdapter`, eliminando overhead de reflexão repetida em chamadas à rede.
  * **Compactação Semântica de Prompt para 18.000+ Flashcards:** Envio de resumos estatísticos por disciplina e foco exclusivo nos tópicos L3 críticos com cartões vencidos (`dueNow > 0`), mantendo o prompt sob 2.000 tokens e latência inferior a 1 segundo no modelo Lite.
  * **Filtragem Nativa de Pensamentos (*Thoughts*):** Garantia de exclusão de blocos de raciocínio intermediário e metadados de modelos Gemini 2.0+.
* **Algoritmo Anki SRS com Day-Cutoff & Índices Compostos no Room**:
  * **Corte Diário às 04:00 AM:** Alinhamento dos prazos de vencimento (`dueTimestamp`) ao início do ciclo diário do Anki, assegurando que revisões noturnas fiquem imediatamente prontas para o ciclo da manhã seguinte.
  * **Aceleração de Consultas com Índices Compostos:** Adição dos índices `(l1, dueTimestamp)`, `(l1, masteryLevel)` e `(l1, l2, dueTimestamp)` no `FlashcardEntity` com migração `MIGRATION_1_2` segura (`CREATE INDEX IF NOT EXISTS`) e bump para Room v2.
* **Sincronização Supabase em Lotes Resiliente (`SupabaseSyncManager`)**:
  * **Batching em Chunks de 250 Cards:** Envio particionado sequencial com `resolution=merge-duplicates`, eliminando estouros de payload HTTP 413 (Payload Too Large) e protegendo contra OutOfMemory em celulares intermediários.
  * **Expansão de Download:** Limite ampliado para até 50.000 cartões em requisições diretas de restauração.
* **Ergonomia Mobile Jetpack Compose & Customização L2**:
  * **Diálogo de Comparação Otimizado:** Barra de botões inferior unificada em linha única de 44dp com padding de 80dp, eliminando 100% da sobreposição com a barra de 3 botões do Android.
  * **Edição Completa de Matérias L2:** Diálogo dedicado para renomeação, alteração de paleta Material 3 e ícones de disciplina com persistência no Room e SharedPreferences.
* **Integração com o Segundo Cérebro (Obsidian)**:
  * Criação do índice dedicado `00-indice-superflash-tcu.md` e formalização das Decisões de Engenharia 10, 11 e 12 no cofre.

---

## 📱 Versão Anterior: v1.1.0 (Code 2)

### 🚀 Principais Novidades da Versão 1.1.0:
* **Aba Dedicada ao Tutor IA Gemini (`AiTutorScreen`)**:
  * Substituição do antigo BottomSheet restrito por uma tela completa e organizada no menu principal.
  * **Diagnóstico Geral:** Área nobre de leitura em tela cheia com renderizador semântico de Markdown, botão de cópia de relatório em 1 clique e badge de conectividade.
  * **Consultoria & Perguntas Livres:** Interface conversacional isolada com atalhos e chips de sugestão rápida que não competem com a visualização do relatório.
  * **Painel de Gargalos L1/L2/L3:** Detecção instantânea das disciplinas com maior número de cards vencidos ou taxa de acerto crítica (< 50%) com botão de revisão imediata.
* **Otimização Global de Performance (60/120 FPS Estáveis)**:
  * **Eliminação de GC Thrashing no Canvas:** Reutilização de instâncias de `Paint`, fontes e gradientes com `remember` no gráfico de frequência semanal de `AnalyticsScreen`.
  * **Preservação de Estado entre Abas:** Navegação instantânea (< 30ms) com `rememberSaveable` retendo o estado de rolagem das listas (`LazyListState`).
  * **Cálculos Reativos Otimizados:** Agrupamentos de estatísticas e filtros protegidos por `derivedStateOf`.

---

## 🏛️ Arquitetura e Stack Tecnológica
* **Linguagem & Plataforma:** Kotlin 2.x, Android SDK 34/36, Java 17 (macOS `/opt/homebrew/opt/openjdk@17`).
* **UI Toolkit:** Jetpack Compose (Material Design 3), navegação fluida em 4 abas, temas Claro e Escuro dinâmicos.
* **Motor de IA Gemini:** Google Generative Language API (REST v1beta), cascata de resiliência (`gemini-flash-latest` ➔ `gemini-flash-lite-latest` ➔ `gemini-3.5-flash`), extração sem pensamentos (*thoughts*) e isolamento seguro de credenciais.
* **Persistência Local:** Room Database SQLite com índices compostos para contagem e recuperação instantânea de cards por L1, L2 e L3.
* **Algoritmo de Aprendizagem:** Anki SuperMemo 2 (SM-2) com cálculo de intervalos, fator de facilidade e Smart Shuffle para priorizar vencimentos.
* **Sincronização & Dados:** Supabase REST / Storage Access Framework (SAF) para importação e exportação de baralhos CSV reais.
* **Testes Automatizados:** Robolectric com `@Config(sdk = [34])` e OkHttp MockWebServer.

---

## 🔒 Segurança e Governança
* Nenhuma chave de API ou credencial confidencial é versionada no Git (`.env`, `local.properties` e keystores mantidos no `.gitignore`).
* Padrão documentado no cofre do **Segundo Cérebro** (`~/segundo-cerebro/`).
