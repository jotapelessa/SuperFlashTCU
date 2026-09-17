# ⚡ SuperFlash TCU (Anki SRS + Gemini AI)

Aplicativo Android nativo de alta performance para preparação de concursos de elite (Tribunal de Contas da União - TCU / Controle), estruturado na hierarquia rigorosa **L1 (Carreira) > L2 (Disciplina) > L3 (Tópico)** com algoritmo de repetição espaçada (SM-2 SRS), banco local Room SQLite offline-first e **Tutor Pedagógico Gemini AI**.

---

## 📱 Versão Atual: v1.1.0 (Code 2)

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
