# SuperFlash TCU — Diretrizes do Agente & Segundo Cérebro (Gemini)

Este projeto integra o ecossistema de memória de longo prazo do desenvolvedor estruturado no **Segundo Cérebro (Obsidian)** em `/Users/jotapelessa/segundo-cerebro/`.

## 🧠 Protocolo Obrigatório de Execução

### 1. Antes de Iniciar Qualquer Tarefa:
1. Consulte o índice mestre em `/Users/jotapelessa/segundo-cerebro/00-indice-mestre.md`.
2. Para qualquer tarefa envolvendo a **IA Gemini**, leia e respeite as seguintes notas:
   - `01-padroes/gemini-ai-resilience-pattern.md` (Padrão de Resiliência: aliases flutuantes e cascata de fallback).
   - `02-decisoes/superflash-gemini-decisoes.md` (Decisões de Engenharia e segurança de chaves).
   - `04-projetos/superflash-tcu.md` (Visão geral e hierarquia L1/L2/L3).
   - `05-stack/superflash-tcu-stack.md` (Stack Android, Kotlin Coroutines, Room, Java 17 no macOS).
   - `06-aprendizados/gemini-api-e-google-ai-studio.md` (Erros 503, 404 e quotas do Google AI Studio).
3. **Prova de Consulta**: Indique explicitamente na resposta inicial a nota consultada:
   `> 🧠 **Segundo Cérebro:** [0X-categoria/nome-da-nota.md]`

### 2. Regras de Ouro da Integração Gemini:
- **Nunca use versões fixas obsoletas** (como `gemini-1.5-flash` ou `gemini-2.5-flash`). Use sempre `gemini-flash-latest` e garanta o fallback para `gemini-flash-lite-latest`.
- **Zero Vazamento de Chaves**: Jamais commite `.env`, `local.properties` ou `debug.keystore`. Mantenha o `.gitignore` estritamente limpo.
- **Testes Unitários com Mocks**: Nunca faça requisições reais de rede para a API Gemini em testes unitários. Use interceptores do OkHttp.
- **Compatibilidade macOS**: Sempre configure o build com `JAVA_HOME=/opt/homebrew/opt/openjdk@17` e testes Robolectric com `@Config(sdk = [34])`.

### 3. Depois de Concluir a Tarefa:
- Registre novas decisões arquiteturais, padrões comprovados ou lições aprendidas diretamente na pasta correspondente em `/Users/jotapelessa/segundo-cerebro/`, sem duplicar conteúdo.
