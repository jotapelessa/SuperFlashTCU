# SuperFlash — Manual de Identidade Visual & Diretrizes de Marca (Brand Guidelines)

> **Versão:** 1.0.0 (Oficial)  
> **Conceito Aprovado:** The Gestalt Shutter (O Obturador de Gestalt)  
> **Metáfora Central:** *"O Momento da Recuperação Ativa da Memória"*

---

## 1. O Símbolo: The Gestalt Shutter

O símbolo do **SuperFlash** é construído a partir de quatro formas geométricas poligonais rígidas em ângulo de 45° dispostas sobre um grid modular de 512x512 unidades.

* **Primeira Leitura:** Estrutura arquitetônica moderna, robusta e tecnológica.
* **Segunda Leitura (Gestalt):** O espaço negativo central não é vazio — ele força o cérebro do observador a completar ativamente o monograma **'S'** e uma **centelha de raio** (flash de memória).

---

## 2. Paleta de Cores Oficial

| Aplicação | Nome | Código HEX | RGB | Função |
|---|---|---|---|---|
| **Primária** | **Cobalt Tech** | `#2563EB` | rgb(37, 99, 235) | Ação, foco, confiança institucional |
| **Destaque** | **Electric Cyan** | `#38BDF8` | rgb(56, 189, 248) | Centelha, claridade, inteligência |
| **Fundo Dark** | **Obsidian Deep** | `#0B0F19` | rgb(11, 15, 25) | Superfícies, fundo do app icon |
| **Neutro Claro** | **Pure White** | `#FFFFFF` | rgb(255, 255, 255) | Silhueta em modo reverso e contraste |
| **Neutro Escuro**| **Solid Black** | `#000000` | rgb(0, 0, 0) | Master de produção e impressão 1-cor |

---

## 3. Área de Proteção e Proporções

* **Área de Não-Interferência:** A distância mínima entre o símbolo e qualquer texto ou borda gráfica deve ser igual à largura da aleta central (*X = 72px* na proporção 512px).
* **Alinhamento Óptico:** O centro visual do símbolo está posicionado 2% acima do centro geométrico para compensar o peso visual da base.

---

## 4. Tamanhos Mínimos Recomendados

* **Favicon / Status Bar:** 16 x 16 px (O corte chanfrado e a fenda de 45° foram desenhados especificamente para zero empastamento em 16px).
* **Ícone de Menu / Action Bar:** 24 x 24 dp.
* **Launcher Icon (Android):** 108 x 108 dp (com o símbolo ocupando a zona segura central de 72 dp).
* **Impressão / Mídia Física:** Mínimo de 10 mm de largura.

---

## 5. Arquivos de Entrega e Estrutura de Pastas

```
design/logo/
├── symbol_exploration/
│   ├── concept_01_gestalt_shutter.svg          # Símbolo Master (Preto puro)
│   ├── concept_01_gestalt_shutter_reversed.svg # Versão Reversa (Branco sobre Obsidian)
│   ├── concept_01_gestalt_shutter_mono.svg     # Versão Monocromática de Marca (Cobalt)
│   ├── memory_recall_concept_sheet.png         # Prancha oficial de conceitos
│   └── memory_recall_presentation.html         # Apresentação interativa com Squint Test
├── dist_gestalt_shutter/
│   ├── superflash_gestalt_shutter-black.svg
│   ├── superflash_gestalt_shutter-white.svg
│   ├── superflash_gestalt_shutter-mono-2563eb.svg
│   ├── superflash_gestalt_shutter-app-icon.svg
│   ├── superflash_gestalt_shutter-favicon.svg
│   ├── favicon.ico
│   ├── apple-touch-icon.png
│   ├── site.webmanifest
│   └── PNGs de 32px até 1024px
```

---

## 6. Uso em Código (Jetpack Compose)

Para utilizar o símbolo de forma nativa e sem alocação em qualquer tela do app:

```kotlin
import com.example.ui.components.SuperFlashSymbol
import com.example.ui.components.SuperFlashLockup

// Apenas o símbolo:
SuperFlashSymbol(
    size = 32.dp,
    tint = MaterialTheme.colorScheme.primary
)

// Símbolo + Nome:
SuperFlashLockup(
    iconSize = 28.dp,
    accentColor = MaterialTheme.colorScheme.primary
)
```

---

## 7. Proibições de Uso

1. **Nunca** distorça ou altere as proporções 1:1 do símbolo.
2. **Nunca** use efeitos de relevo degradê 3D obsoletos sobre o master monocromático.
3. **Nunca** preencha o espaço negativo central (a letra 'S' e o raio devem sempre ser formados por ausência de matéria / espaço negativo).
4. **Nunca** aplique sombras difusas que borrem a fenda central de 45°.
