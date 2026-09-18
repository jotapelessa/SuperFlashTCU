package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.importer.CsvParseResult
import com.example.data.importer.SampleData
import com.example.data.local.AppDatabase
import com.example.data.local.HierarchyFolderTuple
import com.example.data.model.BottleneckItem
import com.example.data.model.DeckTimeSpent
import com.example.data.model.DomainStats
import com.example.data.model.FlashcardEntity
import com.example.data.model.L1DeckSummary
import com.example.data.model.L2DisciplineSummary
import com.example.data.model.ReviewStats
import com.example.data.model.SessionLiveStats
import com.example.data.model.SessionTimeReport
import com.example.data.model.StudyFilterMode
import com.example.data.model.StudyProgressReport
import com.example.data.model.StudyTimerConfig
import com.example.data.model.PerCardTimeLimit
import com.example.data.repository.DeckRepository
import com.example.ui.theme.AccentColorOption
import com.example.ui.theme.AppThemeMode
import com.example.ui.components.DisciplinePalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class DeckViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DeckRepository

    private val prefs = application.getSharedPreferences("anki_user_prefs", Context.MODE_PRIVATE)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DeckRepository(db.flashcardDao())

        // Load custom deck colors and icons from prefs
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("custom_color_") && value is String) {
                val deckName = key.removePrefix("custom_color_")
                DisciplinePalette.setCustomColor(deckName, value)
            } else if (key.startsWith("custom_icon_") && value is String) {
                val deckName = key.removePrefix("custom_icon_")
                DisciplinePalette.setCustomIcon(deckName, value)
            }
        }

        // Prepopulate with sample realistic Anki data if empty
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    // Daily Study Goal Feature
    private val _dailyStudyGoal = MutableStateFlow(prefs.getInt("daily_study_goal", 20))
    val dailyStudyGoal: StateFlow<Int> = _dailyStudyGoal.asStateFlow()

    fun setDailyStudyGoal(goal: Int) {
        val safe = goal.coerceIn(5, 500)
        _dailyStudyGoal.value = safe
        prefs.edit().putInt("daily_study_goal", safe).apply()
    }

    // Smart Shuffle Toggle (prioritizes overdue cards & lower mastery)
    private val _smartShuffleEnabled = MutableStateFlow(prefs.getBoolean("smart_shuffle_enabled", true))
    val smartShuffleEnabled: StateFlow<Boolean> = _smartShuffleEnabled.asStateFlow()

    fun setSmartShuffle(enabled: Boolean) {
        _smartShuffleEnabled.value = enabled
        prefs.edit().putBoolean("smart_shuffle_enabled", enabled).apply()
    }

    // SRS Algorithm: "FSRS" (FSRS-5 Moderno) or "SM2" (SM-2 Clássico)
    private val _srsAlgorithm = MutableStateFlow(prefs.getString("srs_algorithm", "FSRS") ?: "FSRS")
    val srsAlgorithm: StateFlow<String> = _srsAlgorithm.asStateFlow()

    fun setSrsAlgorithm(algorithm: String) {
        val safe = if (algorithm.equals("SM2", ignoreCase = true)) "SM2" else "FSRS"
        _srsAlgorithm.value = safe
        prefs.edit().putString("srs_algorithm", safe).apply()
    }

    // FSRS Target Retention: 0.85f (85%), 0.90f (90%), 0.95f (95%)
    private val _targetRetention = MutableStateFlow(prefs.getFloat("fsrs_target_retention", 0.90f))
    val targetRetention: StateFlow<Float> = _targetRetention.asStateFlow()

    fun setTargetRetention(retention: Float) {
        val safe = retention.coerceIn(0.70f, 0.98f)
        _targetRetention.value = safe
        prefs.edit().putFloat("fsrs_target_retention", safe).apply()
    }

    // Today's Reviewed Cards Count (cards reviewed today from 00:00)
    val todayReviewedCount: StateFlow<Int> = repository.allCards.map { cards ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val dayStart = cal.timeInMillis
        cards.count { it.lastReviewedTimestamp >= dayStart }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0
    )

    // All Tags (Cross-domain tagging system)
    val allDistinctTags: StateFlow<List<String>> = repository.allCards.map { cards ->
        repository.extractAllTags(cards)
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )
    val allTags: StateFlow<List<String>> = allDistinctTags

    private val _selectedTagFilter = MutableStateFlow<String?>(null)
    val selectedTagFilter: StateFlow<String?> = _selectedTagFilter.asStateFlow()

    fun setTagFilter(tag: String?) {
        _selectedTagFilter.value = tag
    }

    fun filterByTag(tag: String?) {
        _selectedTagFilter.value = tag
    }

    // 1. Primary Deck List: Strictly surfaces ONLY L1 Decks
    val allCards: StateFlow<List<FlashcardEntity>> = repository.allCards
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val criticalBottlenecks: StateFlow<List<BottleneckItem>> = repository.allCards.map { cards ->
        val now = System.currentTimeMillis()
        cards.groupBy { "${it.l1} > ${it.l2}" }
            .map { (key, disciplineCards) ->
                val dueCount = disciplineCards.count { it.dueTimestamp <= now }
                val masteredCount = disciplineCards.count { it.masteryLevel >= 2 }
                val masteryRate = if (disciplineCards.isNotEmpty()) (masteredCount.toFloat() / disciplineCards.size) * 100f else 0f
                val cardsWithS = disciplineCards.filter { it.stability > 0f }
                val avgS = if (cardsWithS.isNotEmpty()) cardsWithS.map { it.stability.toDouble() }.average().toFloat() else 0f
                val avgD = if (cardsWithS.isNotEmpty()) cardsWithS.map { it.difficulty.toDouble() }.average().toFloat() else 0f
                BottleneckItem(
                    discipline = key,
                    totalCards = disciplineCards.size,
                    dueCards = dueCount,
                    masteryRate = masteryRate,
                    avgStability = avgS,
                    avgDifficulty = avgD,
                    cards = disciplineCards
                )
            }
            .filter { it.dueCards > 0 || it.masteryRate < 50f || (it.avgStability in 0.1f..7.0f) }
            .sortedWith(compareByDescending<BottleneckItem> { it.dueCards }.thenBy { it.masteryRate })
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val _customizationsTrigger = MutableStateFlow(System.currentTimeMillis())

    private data class L1Customization(
        val color: String?,
        val course: String?,
        val cover: String?,
        val isAiEnabled: Boolean
    )

    private fun getCustomizationForL1(l1: String): L1Customization {
        val color = prefs.getString("l1_color_$l1", null)
        val course = prefs.getString("l1_course_$l1", null)
        val cover = prefs.getString("l1_cover_$l1", null)
        val isAiEnabled = prefs.getBoolean("l1_ai_enabled_$l1", true)
        return L1Customization(color, course, cover, isAiEnabled)
    }

    val l1Decks: StateFlow<List<L1DeckSummary>> = combine(repository.l1DeckSummaries, _customizationsTrigger) { summaries, _ ->
        summaries.map { summary ->
            val custom = getCustomizationForL1(summary.l1)
            summary.copy(
                cardColorHex = custom.color,
                courseName = custom.course,
                coverUrl = custom.cover,
                isAiAnalysisEnabled = custom.isAiEnabled
            )
        }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    fun updateL1Customization(
        oldL1: String,
        newName: String,
        cardColorHex: String?,
        courseName: String?,
        coverUrl: String?,
        isAiEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            var targetL1 = oldL1.trim()
            val cleanNewName = newName.trim()
            if (cleanNewName.isNotBlank() && cleanNewName != oldL1) {
                repository.renameL1Deck(oldL1, cleanNewName)
                
                prefs.edit().apply {
                    remove("l1_color_$oldL1")
                    remove("l1_course_$oldL1")
                    remove("l1_cover_$oldL1")
                    remove("l1_ai_enabled_$oldL1")
                }.apply()
                targetL1 = cleanNewName
            }

            prefs.edit().apply {
                if (!cardColorHex.isNullOrBlank()) putString("l1_color_$targetL1", cardColorHex) else remove("l1_color_$targetL1")
                if (!courseName.isNullOrBlank()) putString("l1_course_$targetL1", courseName) else remove("l1_course_$targetL1")
                if (!coverUrl.isNullOrBlank()) putString("l1_cover_$targetL1", coverUrl) else remove("l1_cover_$targetL1")
                putBoolean("l1_ai_enabled_$targetL1", isAiEnabled)
            }.apply()

            if (_selectedL1.value == oldL1) {
                _selectedL1.value = targetL1
            }

            _customizationsTrigger.value = System.currentTimeMillis()
        }
    }

    fun toggleL1AiAnalysis(l1: String, enabled: Boolean) {
        viewModelScope.launch {
            prefs.edit().putBoolean("l1_ai_enabled_$l1", enabled).apply()
            _customizationsTrigger.value = System.currentTimeMillis()
        }
    }

    fun deleteL2Discipline(l1: String, l2: String) {
        viewModelScope.launch {
            repository.deleteL2Discipline(l1, l2)
        }
    }

    // Current selected L1 for dedicated sub-deck navigation
    private val _selectedL1 = MutableStateFlow<String?>(null)
    val selectedL1: StateFlow<String?> = _selectedL1.asStateFlow()

    fun selectL1(l1: String?) {
        _selectedL1.value = l1
        // Reset sub-filters
        _selectedL2Filter.value = null
        _selectedL3Filter.value = null
    }

    fun loadAnalytics(l1: String? = null) {
        _selectedL1.value = l1
    }

    // 2. Dedicated Sub-decks: L2 disciplines and L3 topics for the selected L1
    @OptIn(ExperimentalCoroutinesApi::class)
    val l2Disciplines: StateFlow<List<L2DisciplineSummary>> = _selectedL1
        .flatMapLatest { l1 ->
            if (l1 != null) {
                repository.getL2DisciplinesForL1(l1)
            } else {
                flowOf(emptyList())
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Sub-filters for Dominio and Revisao tabs
    private val _selectedL2Filter = MutableStateFlow<String?>(null)
    val selectedL2Filter: StateFlow<String?> = _selectedL2Filter.asStateFlow()

    private val _selectedL3Filter = MutableStateFlow<String?>(null)
    val selectedL3Filter: StateFlow<String?> = _selectedL3Filter.asStateFlow()

    fun setL2Filter(l2: String?) {
        _selectedL2Filter.value = l2
        _selectedL3Filter.value = null
    }

    fun setL3Filter(l3: String?) {
        _selectedL3Filter.value = l3
    }

    // Cards matching current L1 and optional sub-filters
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredCards: StateFlow<List<FlashcardEntity>> = combine(
        _selectedL1,
        _selectedL2Filter,
        _selectedL3Filter
    ) { l1, l2, l3 -> Triple(l1, l2, l3) }
        .flatMapLatest { (l1, l2, l3) ->
            if (l1 != null) {
                repository.getCards(l1, l2, l3)
            } else {
                repository.allCards
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // 3. Domain (Domínio) Stats: Correctly filtered and aggregated by hierarchy
    val domainStats: StateFlow<DomainStats> = filteredCards
        .map { cards ->
            repository.calculateDomainStats(cards)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DomainStats()
        )

    // 4. Review (Revisão) Stats: Correctly filtered and aggregated by hierarchy
    val reviewStats: StateFlow<ReviewStats> = filteredCards
        .map { cards ->
            repository.calculateReviewStats(cards)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ReviewStats()
        )

    // 5. Progress Analytics & Visualization State
    val progressReport: StateFlow<StudyProgressReport> = repository.allCards
        .combine(_selectedL1) { allCards, selectedL1 ->
            val scopedCards = if (selectedL1 != null) {
                allCards.filter { it.l1 == selectedL1 }
            } else {
                allCards
            }
            repository.calculateProgressReport(scopedCards)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = StudyProgressReport()
        )

    // 6. Study / Review Session State
    private val _studyCards = MutableStateFlow<List<FlashcardEntity>>(emptyList())
    val studyCards: StateFlow<List<FlashcardEntity>> = _studyCards.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isAnswerRevealed = MutableStateFlow(false)
    val isAnswerRevealed: StateFlow<Boolean> = _isAnswerRevealed.asStateFlow()

    private val _studyCompleted = MutableStateFlow(false)
    val studyCompleted: StateFlow<Boolean> = _studyCompleted.asStateFlow()

    private val _sessionStats = MutableStateFlow(SessionLiveStats())
    val sessionStats: StateFlow<SessionLiveStats> = _sessionStats.asStateFlow()

    private val _sessionTimerConfig = MutableStateFlow(
        StudyTimerConfig(
            perCardLimit = try {
                PerCardTimeLimit.valueOf(
                    prefs.getString("timer_per_card_limit", PerCardTimeLimit.UNLIMITED.name) ?: PerCardTimeLimit.UNLIMITED.name
                )
            } catch (e: Exception) {
                PerCardTimeLimit.UNLIMITED
            },
            targetSessionMinutes = prefs.getInt("timer_target_session_minutes", 0)
        )
    )
    val sessionTimerConfig: StateFlow<StudyTimerConfig> = _sessionTimerConfig.asStateFlow()

    fun setSessionTimerConfig(config: StudyTimerConfig) {
        _sessionTimerConfig.value = config
        prefs.edit()
            .putString("timer_per_card_limit", config.perCardLimit.name)
            .putInt("timer_target_session_minutes", config.targetSessionMinutes)
            .apply()
    }

    private val sessionDeckTimes = mutableMapOf<String, Long>()
    private val sessionDeckCounts = mutableMapOf<String, Int>()
    private val sessionDeckL1 = mutableMapOf<String, String>()

    fun startStudySession(
        cards: List<FlashcardEntity>,
        timerConfig: StudyTimerConfig = _sessionTimerConfig.value,
        applySmartShuffle: Boolean = _smartShuffleEnabled.value
    ) {
        val preparedCards = if (applySmartShuffle) repository.applySmartShuffle(cards) else cards
        _studyCards.value = preparedCards
        _currentCardIndex.value = 0
        _isAnswerRevealed.value = false
        _studyCompleted.value = false
        _sessionTimerConfig.value = timerConfig
        sessionDeckTimes.clear()
        sessionDeckCounts.clear()
        sessionDeckL1.clear()
        _sessionStats.value = SessionLiveStats(
            totalSessionCards = preparedCards.size,
            timeReport = SessionTimeReport()
        )
    }

    fun finishStudySession() {
        _studyCompleted.value = false
        _studyCards.value = emptyList()
        _currentCardIndex.value = 0
        _isAnswerRevealed.value = false
        sessionDeckTimes.clear()
        sessionDeckCounts.clear()
        sessionDeckL1.clear()
        _sessionStats.value = SessionLiveStats()
    }

    fun startConfiguredStudySession(
        allCardsList: List<FlashcardEntity>,
        l1: String?,
        l2: String?,
        l3: String?,
        tag: String? = null,
        mode: StudyFilterMode = StudyFilterMode.DUE_ONLY,
        limit: Int = 20,
        smartShuffle: Boolean = _smartShuffleEnabled.value,
        timerConfig: StudyTimerConfig = _sessionTimerConfig.value
    ) {
        val selectedCards = repository.filterCardsForStudySession(
            cards = allCardsList,
            l1 = l1,
            l2 = l2,
            l3 = l3,
            tag = tag,
            mode = mode,
            limit = limit,
            smartShuffle = smartShuffle
        )
        startStudySession(selectedCards, timerConfig = timerConfig, applySmartShuffle = false)
    }

    fun revealAnswer() {
        _isAnswerRevealed.value = true
    }

    fun rateCurrentCard(rating: Int, timeSpentMillis: Long = 0L) {
        val cards = _studyCards.value
        val index = _currentCardIndex.value
        if (index in cards.indices) {
            val card = cards[index]
            val algorithm = _srsAlgorithm.value
            val retention = _targetRetention.value.toDouble()
            viewModelScope.launch {
                repository.recordReview(
                    card = card,
                    rating = rating,
                    algorithm = algorithm,
                    targetRetention = retention
                )
            }

            // Track time spent per deck/discipline
            val deckName = when {
                card.l2.isNotBlank() -> card.l2
                card.l1.isNotBlank() -> card.l1
                else -> "Geral"
            }
            sessionDeckL1[deckName] = card.l1
            val prevTime = sessionDeckTimes.getOrDefault(deckName, 0L)
            sessionDeckTimes[deckName] = prevTime + timeSpentMillis
            sessionDeckCounts[deckName] = sessionDeckCounts.getOrDefault(deckName, 0) + 1

            // Update live session statistics
            val prev = _sessionStats.value
            val isGraduated = card.masteryLevel < 2 && (rating >= 3 && (card.intervalDays >= 5 || card.reps >= 2))
            val newCompleted = prev.completedCards + 1

            val totalTime = sessionDeckTimes.values.sum()
            val deckBreakdown = sessionDeckTimes.map { (deck, millis) ->
                val deckL1 = sessionDeckL1[deck] ?: card.l1
                DeckTimeSpent(
                    deckName = deck,
                    l1 = deckL1,
                    l2 = if (deck != deckL1) deck else "",
                    timeMillis = millis,
                    cardsCount = sessionDeckCounts.getOrDefault(deck, 1),
                    colorHex = DisciplinePalette.getColorForDiscipline(deck)
                )
            }.sortedByDescending { it.timeMillis }

            val avgSec = if (newCompleted > 0) (totalTime / 1000f) / newCompleted else 0f
            val timeReport = SessionTimeReport(
                totalTimeMillis = totalTime,
                deckBreakdown = deckBreakdown,
                averageSecondsPerCard = avgSec
            )

            _sessionStats.value = prev.copy(
                completedCards = newCompleted,
                againCount = if (rating == 1) prev.againCount + 1 else prev.againCount,
                hardCount = if (rating == 2) prev.hardCount + 1 else prev.hardCount,
                goodCount = if (rating == 3) prev.goodCount + 1 else prev.goodCount,
                easyCount = if (rating == 4) prev.easyCount + 1 else prev.easyCount,
                graduatedToMasteredCount = if (isGraduated) prev.graduatedToMasteredCount + 1 else prev.graduatedToMasteredCount,
                timeReport = timeReport
            )

            if (index + 1 < cards.size) {
                _currentCardIndex.value = index + 1
                _isAnswerRevealed.value = false
            } else {
                _studyCompleted.value = true
            }
        }
    }

    // 7. Manual Flashcard Editor State (Create / Update individual flashcards & hierarchy)
    private val _isCardEditorOpen = MutableStateFlow(false)
    val isCardEditorOpen: StateFlow<Boolean> = _isCardEditorOpen.asStateFlow()

    private val _cardBeingEdited = MutableStateFlow<FlashcardEntity?>(null)
    val cardBeingEdited: StateFlow<FlashcardEntity?> = _cardBeingEdited.asStateFlow()

    private val _allFolders = MutableStateFlow<List<HierarchyFolderTuple>>(emptyList())
    val allFolders: StateFlow<List<HierarchyFolderTuple>> = _allFolders.asStateFlow()

    fun openCreateCard(defaultL1: String? = null, defaultL2: String? = null, defaultL3: String? = null) {
        viewModelScope.launch {
            _allFolders.value = repository.getAllHierarchyFolders()
        }
        _cardBeingEdited.value = FlashcardEntity(
            id = 0L,
            deckRaw = "",
            l1 = defaultL1 ?: _selectedL1.value ?: "",
            l2 = defaultL2 ?: _selectedL2Filter.value ?: "",
            l3 = defaultL3 ?: _selectedL3Filter.value ?: "",
            frontHtml = "",
            backHtml = "",
            tags = "",
            contentHash = ""
        )
        _isCardEditorOpen.value = true
    }

    fun openEditCard(card: FlashcardEntity) {
        viewModelScope.launch {
            _allFolders.value = repository.getAllHierarchyFolders()
        }
        _cardBeingEdited.value = card
        _isCardEditorOpen.value = true
    }

    fun closeCardEditor() {
        _isCardEditorOpen.value = false
        _cardBeingEdited.value = null
    }

    fun saveCard(card: FlashcardEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val saved = repository.saveCard(card)
            // If we are currently in study mode and editing the active card, update it in-place
            val currentCards = _studyCards.value.toMutableList()
            val index = currentCards.indexOfFirst { it.id == saved.id }
            if (index != -1) {
                currentCards[index] = saved
                _studyCards.value = currentCards
            }
            closeCardEditor()
            onComplete?.invoke()
        }
    }

    fun deleteCard(card: FlashcardEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteCard(card)
            val currentCards = _studyCards.value.toMutableList()
            currentCards.removeAll { it.id == card.id }
            _studyCards.value = currentCards
            if (_currentCardIndex.value >= currentCards.size && currentCards.isNotEmpty()) {
                _currentCardIndex.value = currentCards.size - 1
            }
            closeCardEditor()
            onComplete?.invoke()
        }
    }

    // 8. CSV Importer State
    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importResult = MutableStateFlow<CsvParseResult?>(null)
    val importResult: StateFlow<CsvParseResult?> = _importResult.asStateFlow()

    fun importCsv(content: String) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val result = repository.importAnkiCsv(content)
                _importResult.value = result
            } catch (e: Exception) {
                _importResult.value = CsvParseResult(
                    totalRows = 0,
                    validCards = emptyList(),
                    skippedDuplicates = 0,
                    errors = listOf(
                        com.example.data.importer.RowError(
                            rowNumber = 0,
                            rawPreview = "",
                            reason = "Erro durante processamento: ${e.localizedMessage}"
                        )
                    )
                )
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun loadSampleDeck() {
        importCsv(SampleData.sampleCsv)
    }

    fun dismissImportResult() {
        _importResult.value = null
    }

    fun moveL3TopicsToNewL2(
        l1: String,
        topicsToMove: List<Pair<String, String>>,
        newL2Name: String,
        iconKey: String,
        colorHex: String,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val cleanName = newL2Name.trim().ifBlank { "Novo Baralho L2" }
            DisciplinePalette.setCustomColor(cleanName, colorHex)
            DisciplinePalette.setCustomIcon(cleanName, iconKey)

            prefs.edit()
                .putString("custom_color_$cleanName", colorHex)
                .putString("custom_icon_$cleanName", iconKey)
                .apply()

            repository.moveL3TopicsToNewL2(l1, topicsToMove, cleanName)
            repository.notifyPaletteChanged()
            onComplete?.invoke()
        }
    }

    fun updateL2Discipline(
        l1: String,
        oldL2Name: String,
        newL2Name: String,
        iconKey: String,
        colorHex: String,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val cleanName = newL2Name.trim().ifBlank { oldL2Name }
            DisciplinePalette.setCustomColor(cleanName, colorHex)
            DisciplinePalette.setCustomIcon(cleanName, iconKey)

            prefs.edit()
                .putString("custom_color_$cleanName", colorHex)
                .putString("custom_icon_$cleanName", iconKey)
                .apply()

            if (cleanName != oldL2Name) {
                DisciplinePalette.removeCustom(oldL2Name)
                prefs.edit()
                    .remove("custom_color_$oldL2Name")
                    .remove("custom_icon_$oldL2Name")
                    .apply()

                repository.renameL2Discipline(l1, oldL2Name, cleanName)
                if (_selectedL2Filter.value == oldL2Name) {
                    _selectedL2Filter.value = cleanName
                }
            }
            repository.notifyPaletteChanged()
            onComplete?.invoke()
        }
    }

    // Move multiple L2 decks across L1s to a new or existing target L1 deck
    fun moveMultipleL2ToL1(
        itemsToMove: List<Pair<String, String>>, // list of (sourceL1, l2)
        newL1Name: String,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val cleanL1Name = newL1Name.trim().ifBlank { "Novo Baralho L1" }
            repository.moveMultipleL2ToL1(itemsToMove, cleanL1Name)
            onComplete?.invoke()
        }
    }

    // CSV Backup Export
    fun exportL1ToCsv(l1Target: String? = null, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val csv = repository.exportL1ToCsv(l1Target)
            onResult(csv)
        }
    }

    // Supabase Sync State & Controls
    private val supabaseSyncManager = com.example.data.remote.SupabaseSyncManager()

    private val _supabaseUrl = MutableStateFlow(prefs.getString("supabase_url", "") ?: "")
    val supabaseUrl: StateFlow<String> = _supabaseUrl.asStateFlow()

    fun setSupabaseUrl(url: String) {
        _supabaseUrl.value = url.trim()
        prefs.edit().putString("supabase_url", url.trim()).apply()
    }

    private val _supabaseKey = MutableStateFlow(prefs.getString("supabase_key", "") ?: "")
    val supabaseKey: StateFlow<String> = _supabaseKey.asStateFlow()

    fun setSupabaseKey(key: String) {
        _supabaseKey.value = key.trim()
        prefs.edit().putString("supabase_key", key.trim()).apply()
    }

    private val _supabaseAutoSync = MutableStateFlow(prefs.getBoolean("supabase_auto_sync", false))
    val supabaseAutoSync: StateFlow<Boolean> = _supabaseAutoSync.asStateFlow()

    fun setSupabaseAutoSync(enabled: Boolean) {
        _supabaseAutoSync.value = enabled
        prefs.edit().putBoolean("supabase_auto_sync", enabled).apply()
    }

    private val _supabaseSyncState = MutableStateFlow<String?>("Pronto para sincronizar")
    val supabaseSyncState: StateFlow<String?> = _supabaseSyncState.asStateFlow()

    private val _supabaseIsLoading = MutableStateFlow(false)
    val supabaseIsLoading: StateFlow<Boolean> = _supabaseIsLoading.asStateFlow()

    fun testSupabaseConnection() {
        viewModelScope.launch {
            _supabaseIsLoading.value = true
            _supabaseSyncState.value = "Testando conexão..."
            val result = supabaseSyncManager.testConnection(_supabaseUrl.value, _supabaseKey.value)
            when (result) {
                is com.example.data.remote.SupabaseSyncResult.Success -> {
                    _supabaseSyncState.value = "🟢 ${result.message}"
                }
                is com.example.data.remote.SupabaseSyncResult.Error -> {
                    _supabaseSyncState.value = "🔴 ${result.errorMessage}"
                }
            }
            _supabaseIsLoading.value = false
        }
    }

    fun syncToSupabase() {
        viewModelScope.launch {
            _supabaseIsLoading.value = true
            _supabaseSyncState.value = "Enviando dados para o Supabase..."
            val cards = allCards.value
            val progress = progressReport.value
            val settingsJson = org.json.JSONObject().apply {
                put("dailyGoal", dailyStudyGoal.value)
                put("smartShuffle", smartShuffleEnabled.value)
                put("themeMode", themeMode.value.name)
                put("accentColor", accentColor.value.name)
            }.toString()

            val result = supabaseSyncManager.uploadFullData(
                supabaseUrl = _supabaseUrl.value,
                supabaseKey = _supabaseKey.value,
                cards = cards,
                progressReport = progress,
                settingsJson = settingsJson
            )

            when (result) {
                is com.example.data.remote.SupabaseSyncResult.Success -> {
                    _supabaseSyncState.value = "🟢 ${result.message}"
                }
                is com.example.data.remote.SupabaseSyncResult.Error -> {
                    _supabaseSyncState.value = "🔴 ${result.errorMessage}"
                }
            }
            _supabaseIsLoading.value = false
        }
    }

    fun downloadFromSupabase() {
        viewModelScope.launch {
            _supabaseIsLoading.value = true
            _supabaseSyncState.value = "Baixando e restaurando dados do Supabase..."
            val resultPair = supabaseSyncManager.downloadFullData(
                supabaseUrl = _supabaseUrl.value,
                supabaseKey = _supabaseKey.value
            )
            val downloadedCards = resultPair.first
            val error = resultPair.second

            if (downloadedCards != null && downloadedCards.isNotEmpty()) {
                repository.restoreCardsFromCloud(downloadedCards)
                _supabaseSyncState.value = "🟢 Sucesso! ${downloadedCards.size} flashcards restaurados da nuvem."
            } else {
                _supabaseSyncState.value = "🔴 ${error ?: "Nenhum dado encontrado no Supabase para restaurar."}"
            }
            _supabaseIsLoading.value = false
        }
    }

    fun deleteDeck(l1: String) {
        viewModelScope.launch {
            repository.deleteDeck(l1)
            if (_selectedL1.value == l1) {
                _selectedL1.value = null
            }
        }
    }

    // Theme Preferences
    private val _themeMode = MutableStateFlow(
        AppThemeMode.valueOf(prefs.getString("app_theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("app_theme_mode", mode.name).apply()
    }

    private val _accentColor = MutableStateFlow(
        AccentColorOption.valueOf(prefs.getString("app_accent_color", AccentColorOption.BLUE.name) ?: AccentColorOption.BLUE.name)
    )
    val accentColor: StateFlow<AccentColorOption> = _accentColor.asStateFlow()

    fun setAccentColor(option: AccentColorOption) {
        _accentColor.value = option
        prefs.edit().putString("app_accent_color", option.name).apply()
    }

    // Custom Gemini API Key & Model Version Preferences
    private val _geminiApiKey = MutableStateFlow(prefs.getString("gemini_api_key", "") ?: "")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    fun setGeminiApiKey(key: String) {
        _geminiApiKey.value = key
        prefs.edit().putString("gemini_api_key", key).apply()
    }

    private val _geminiModelVersion = MutableStateFlow(
        prefs.getString("gemini_model_version", "gemini-flash-latest")?.takeIf { it != "gemini-1.5-flash" } ?: "gemini-flash-latest"
    )
    val geminiModelVersion: StateFlow<String> = _geminiModelVersion.asStateFlow()

    fun setGeminiModelVersion(version: String) {
        _geminiModelVersion.value = version
        prefs.edit().putString("gemini_model_version", version).apply()
    }

    // Gemini AI Analysis & Telemetry
    private val aiAnalyzer = com.example.data.ai.GeminiStudyAnalyzer()

    data class GeminiTelemetryState(
        val requestsToday: Int = 0,
        val tokensToday: Int = 0,
        val lastPromptTokens: Int = 0,
        val lastResponseTokens: Int = 0,
        val lastLatencyMs: Long = 0L,
        val lastModelUsed: String = "",
        val isTestingKey: Boolean = false,
        val testResult: String? = null
    )

    private val _geminiTelemetry = MutableStateFlow(loadInitialGeminiTelemetry())
    val geminiTelemetry: StateFlow<GeminiTelemetryState> = _geminiTelemetry.asStateFlow()

    private fun loadInitialGeminiTelemetry(): GeminiTelemetryState {
        val lastResetDay = prefs.getInt("gemini_telemetry_last_day", -1)
        val currentDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)

        val requests = if (lastResetDay == currentDay) prefs.getInt("gemini_requests_today", 0) else 0
        val tokens = if (lastResetDay == currentDay) prefs.getInt("gemini_tokens_today", 0) else 0

        if (lastResetDay != currentDay) {
            prefs.edit()
                .putInt("gemini_telemetry_last_day", currentDay)
                .putInt("gemini_requests_today", 0)
                .putInt("gemini_tokens_today", 0)
                .apply()
        }

        return GeminiTelemetryState(
            requestsToday = requests,
            tokensToday = tokens,
            lastPromptTokens = prefs.getInt("gemini_last_prompt_tokens", 0),
            lastResponseTokens = prefs.getInt("gemini_last_response_tokens", 0),
            lastLatencyMs = prefs.getLong("gemini_last_latency_ms", 0L),
            lastModelUsed = prefs.getString("gemini_last_model_used", "") ?: ""
        )
    }

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiAnalysisResult = MutableStateFlow<String?>(null)
    val aiAnalysisResult: StateFlow<String?> = _aiAnalysisResult.asStateFlow()

    private val _aiErrorMessage = MutableStateFlow<String?>(null)
    val aiErrorMessage: StateFlow<String?> = _aiErrorMessage.asStateFlow()

    fun runAiAnalysis(customQuestion: String? = null) {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiErrorMessage.value = null

            val permittedL1Decks = l1Decks.value.filter { it.isAiAnalysisEnabled }
            if (permittedL1Decks.isEmpty()) {
                _aiErrorMessage.value = "Nenhum baralho L1 habilitado para análise da IA. Ative a IA em ao menos um baralho nas configurações do deck."
                _aiLoading.value = false
                return@launch
            }

            val permittedL1Names = permittedL1Decks.map { it.l1 }.toSet()
            val permittedCards = allCards.value.filter { it.l1 in permittedL1Names }

            // Recalcula o progresso contextualizado estritamente para os baralhos permitidos
            val now = System.currentTimeMillis()
            val total = permittedCards.size
            val due = permittedCards.count { it.dueTimestamp <= now }
            val mastered = permittedCards.count { it.masteryLevel >= 2 }
            val learning = permittedCards.count { it.masteryLevel == 1 }
            val newCards = permittedCards.count { it.masteryLevel == 0 && it.reps == 0 }
            val totalRevs = permittedCards.sumOf { it.reps }
            val masteryPct = if (total > 0) ((mastered * 1.0f + learning * 0.4f) / total) * 100f else 0f

            val baseReport = progressReport.value
            val contextualizedReport = baseReport.copy(
                totalCards = total,
                totalReviews = totalRevs,
                overallMasteryPercentage = masteryPct,
                dueNowCount = due,
                learningCount = learning,
                masteredCount = mastered,
                newCount = newCards,
                domainMasteryList = baseReport.domainMasteryList.filter { it.domainName in permittedL1Names }
            )

            val result = aiAnalyzer.analyzeStudyDataWithMetadata(
                l1Decks = permittedL1Decks,
                allCards = permittedCards,
                progressReport = contextualizedReport,
                todayReviewed = todayReviewedCount.value,
                dailyGoal = dailyStudyGoal.value,
                customQuestion = customQuestion,
                customApiKey = geminiApiKey.value,
                customModelVersion = geminiModelVersion.value,
                srsAlgorithm = _srsAlgorithm.value,
                targetRetention = _targetRetention.value
            )
            result.onSuccess { meta ->
                _aiAnalysisResult.value = meta.text

                // Atualiza Telemetria
                val currentDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
                val newRequests = _geminiTelemetry.value.requestsToday + 1
                val newTokens = _geminiTelemetry.value.tokensToday + meta.totalTokens

                _geminiTelemetry.value = _geminiTelemetry.value.copy(
                    requestsToday = newRequests,
                    tokensToday = newTokens,
                    lastPromptTokens = meta.promptTokens,
                    lastResponseTokens = meta.candidatesTokens,
                    lastLatencyMs = meta.latencyMs,
                    lastModelUsed = meta.modelUsed
                )

                prefs.edit()
                    .putInt("gemini_telemetry_last_day", currentDay)
                    .putInt("gemini_requests_today", newRequests)
                    .putInt("gemini_tokens_today", newTokens)
                    .putInt("gemini_last_prompt_tokens", meta.promptTokens)
                    .putInt("gemini_last_response_tokens", meta.candidatesTokens)
                    .putLong("gemini_last_latency_ms", meta.latencyMs)
                    .putString("gemini_last_model_used", meta.modelUsed)
                    .apply()
            }.onFailure { err ->
                _aiErrorMessage.value = err.localizedMessage ?: "Erro ao gerar análise com Gemini."
            }
            _aiLoading.value = false
        }
    }

    fun testGeminiApiKey() {
        viewModelScope.launch {
            _geminiTelemetry.value = _geminiTelemetry.value.copy(
                isTestingKey = true,
                testResult = null
            )
            val result = aiAnalyzer.testApiKeyConnection(
                customApiKey = geminiApiKey.value,
                modelVersion = geminiModelVersion.value
            )
            result.onSuccess { (latency, modelUsed) ->
                _geminiTelemetry.value = _geminiTelemetry.value.copy(
                    isTestingKey = false,
                    testResult = "✅ Conexão ativa! Latência: ${latency}ms ($modelUsed)"
                )
            }.onFailure { err ->
                _geminiTelemetry.value = _geminiTelemetry.value.copy(
                    isTestingKey = false,
                    testResult = "❌ Falha na conexão: ${err.localizedMessage ?: "Erro desconhecido"}"
                )
            }
        }
    }

    fun resetGeminiDailyStats() {
        val currentDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        _geminiTelemetry.value = _geminiTelemetry.value.copy(
            requestsToday = 0,
            tokensToday = 0,
            testResult = null
        )
        prefs.edit()
            .putInt("gemini_telemetry_last_day", currentDay)
            .putInt("gemini_requests_today", 0)
            .putInt("gemini_tokens_today", 0)
            .apply()
    }

    fun clearAiAnalysis() {
        _aiAnalysisResult.value = null
        _aiErrorMessage.value = null
    }

    fun resetToDefault() {
        viewModelScope.launch {
            repository.clearAll()
            repository.checkAndSeedInitialData()
            _selectedL1.value = null
        }
    }

    fun resetAllStudyStats() {
        viewModelScope.launch {
            repository.resetAllCardStats()
            sessionDeckTimes.clear()
            sessionDeckCounts.clear()
            _sessionStats.value = SessionLiveStats()
            _aiAnalysisResult.value = null
            _aiErrorMessage.value = null
        }
    }

    fun deleteAllDecksAndCards() {
        viewModelScope.launch {
            repository.clearAll()
            _selectedL1.value = null
            _selectedL2Filter.value = null
            _selectedL3Filter.value = null
            _studyCards.value = emptyList()
            sessionDeckTimes.clear()
            sessionDeckCounts.clear()
            _sessionStats.value = SessionLiveStats()
            _aiAnalysisResult.value = null
            _aiErrorMessage.value = null
        }
    }

    fun clearAllDataAndResetZero() {
        viewModelScope.launch {
            repository.clearAll()
            _selectedL1.value = null
            _selectedL2Filter.value = null
            _selectedL3Filter.value = null
            _studyCards.value = emptyList()
            sessionDeckTimes.clear()
            sessionDeckCounts.clear()
            _sessionStats.value = SessionLiveStats()
            _aiAnalysisResult.value = null
            _aiErrorMessage.value = null
        }
    }
}
