# ⚡ SuperFlash TCU (Anki SRS + Gemini AI)

Aplicativo Android nativo de alta performance para preparação de concursos de elite (Tribunal de Contas da União - TCU / Controle), estruturado na hierarquia rigorosa **L1 (Carreira) > L2 (Disciplina) > L3 (Tópico)** com algoritmo de repetição espaçada (SM-2 SRS), banco local Room SQLite offline-first e **Tutor Pedagógico Gemini AI**.

---

## 📱 Versão Atual: v1.4.1 (Code 6)

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
