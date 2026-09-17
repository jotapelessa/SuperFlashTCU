package com.example.data.repository

import com.example.data.importer.AnkiCsvImporter
import com.example.data.importer.CsvParseResult
import com.example.data.importer.PreImportReport
import com.example.data.importer.SampleData
import com.example.data.local.FlashcardDao
import com.example.data.local.HierarchyFolderTuple
import com.example.data.model.DomainStats
import com.example.data.model.FlashcardEntity
import com.example.data.model.L1DeckSummary
import com.example.data.model.L2DisciplineSummary
import com.example.data.model.L3TopicSummary
import com.example.data.model.ReviewStats
import com.example.data.model.DayStudyStat
import com.example.data.model.DomainMasteryStat
import com.example.data.model.StudyFilterMode
import com.example.data.model.StudyProgressReport
import com.example.ui.components.DisciplinePalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

class DeckRepository(private val dao: FlashcardDao) {

    val allCards: Flow<List<FlashcardEntity>> = dao.getAllFlashcards()

    /**
     * Strictly distinguish between L1, L2, and L3 levels during synchronization.
     * Only L1 decks are surfaced in the primary deck list.
     */
    val l1DeckSummaries: Flow<List<L1DeckSummary>> = allCards.map { cards ->
        val now = System.currentTimeMillis()
        cards.groupBy { it.l1 }
            .map { (l1, l1Cards) ->
                val l2Groups = l1Cards.groupBy { it.l2 }
                val l3Set = l1Cards.map { "${it.l2}::${it.l3}" }.toSet()
                val dueCount = l1Cards.count { it.reps > 0 && it.dueTimestamp <= now }
                val masteredCount = l1Cards.count { it.masteryLevel == 2 }
                val learningCount = l1Cards.count { it.masteryLevel == 1 }
                val newCount = l1Cards.count { it.reps == 0 && it.masteryLevel == 0 }

                val colors = l2Groups.keys.map { DisciplinePalette.getColorForDiscipline(it) }.distinct()

                L1DeckSummary(
                    l1 = l1,
                    totalCards = l1Cards.size,
                    dueCards = dueCount,
                    masteredCards = masteredCount,
                    learningCards = learningCount,
                    newCards = newCount,
                    l2Count = l2Groups.size,
                    l3Count = l3Set.size,
                    disciplineColors = colors
                )
            }
            .sortedBy { it.l1 }
    }.flowOn(Dispatchers.Default)

    /**
     * Dedicated L2 & L3 sub-deck hierarchy for a selected L1 deck.
     */
    fun getL2DisciplinesForL1(l1: String): Flow<List<L2DisciplineSummary>> =
        dao.getFlashcardsByL1(l1).map { cards ->
            val now = System.currentTimeMillis()
            cards.groupBy { it.l2 }
                .map { (l2, l2Cards) ->
                    val colorHex = DisciplinePalette.getColorForDiscipline(l2)
                    val iconKey = DisciplinePalette.getIconKeyForDiscipline(l2)
                    val l3Topics = l2Cards.groupBy { it.l3 }
                        .map { (l3, l3Cards) ->
                            L3TopicSummary(
                                l1 = l1,
                                l2 = l2,
                                l3 = l3,
                                totalCards = l3Cards.size,
                                dueCards = l3Cards.count { it.reps > 0 && it.dueTimestamp <= now },
                                masteredCards = l3Cards.count { it.masteryLevel == 2 },
                                learningCards = l3Cards.count { it.masteryLevel == 1 },
                                newCards = l3Cards.count { it.reps == 0 && it.masteryLevel == 0 },
                                colorHex = colorHex
                            )
                        }
                        .sortedBy { it.l3 }

                    L2DisciplineSummary(
                        l1 = l1,
                        l2 = l2,
                        totalCards = l2Cards.size,
                        dueCards = l2Cards.count { it.reps > 0 && it.dueTimestamp <= now },
                        masteredCards = l2Cards.count { it.masteryLevel == 2 },
                        learningCards = l2Cards.count { it.masteryLevel == 1 },
                        newCards = l2Cards.count { it.reps == 0 && it.masteryLevel == 0 },
                        l3Count = l3Topics.size,
                        colorHex = colorHex,
                        iconKey = iconKey,
                        topics = l3Topics
                    )
                }
                .sortedBy { it.l2 }
        }.flowOn(Dispatchers.Default)

    fun getCards(l1: String, l2: String? = null, l3: String? = null): Flow<List<FlashcardEntity>> {
        return when {
            l2 != null && l3 != null -> dao.getFlashcardsByL1L2L3(l1, l2, l3)
            l2 != null -> dao.getFlashcardsByL1AndL2(l1, l2)
            else -> dao.getFlashcardsByL1(l1)
        }
    }

    /**
     * Domain (Domínio) calculation logic strictly filtered and aggregated by hierarchy.
     */
    fun calculateDomainStats(cards: List<FlashcardEntity>): DomainStats {
        if (cards.isEmpty()) return DomainStats()
        val total = cards.size
        val mastered = cards.count { it.masteryLevel == 2 }
        val learning = cards.count { it.masteryLevel == 1 }
        val newCards = cards.count { it.masteryLevel == 0 && it.reps == 0 }
        val percentage = ((mastered * 1.0f + learning * 0.4f) / total) * 100f
        return DomainStats(
            total = total,
            mastered = mastered,
            learning = learning,
            newCards = newCards,
            masteryPercentage = percentage
        )
    }

    /**
     * Review (Revisão) calculation logic strictly filtered and aggregated by hierarchy.
     */
    fun calculateReviewStats(cards: List<FlashcardEntity>): ReviewStats {
        if (cards.isEmpty()) return ReviewStats()
        val now = System.currentTimeMillis()
        val oneDay = 24L * 3600L * 1000L
        val dueNow = cards.count { it.reps > 0 && it.dueTimestamp <= now }
        val dueToday = cards.count { it.reps > 0 && it.dueTimestamp <= (now + oneDay) }
        val reviewed = cards.filter { it.reps > 0 }
        val totalReviews = reviewed.sumOf { it.reps }
        val totalLapses = reviewed.sumOf { it.lapses }
        val retention = if (totalReviews > 0) {
            max(0f, (1f - (totalLapses.toFloat() / totalReviews)) * 100f)
        } else {
            100f
        }
        val avgInterval = if (reviewed.isNotEmpty()) {
            reviewed.map { it.intervalDays }.average().toFloat()
        } else 0f

        return ReviewStats(
            dueNow = dueNow,
            dueToday = dueToday,
            totalReviewed = reviewed.size,
            retentionRate = retention,
            averageIntervalDays = avgInterval
        )
    }

    /**
     * Pre-import check validating incoming records against the database.
     * Evaluates incoming CSV without committing database insertions.
     */
    suspend fun preImportCheck(csvContent: String): PreImportReport {
        val parseResult = AnkiCsvImporter.parseAnkiCsv(csvContent)
        val (_, report) = validateIncomingAgainstDatabase(
            incomingCards = parseResult.validCards,
            totalRows = parseResult.totalRows,
            initialSkippedDuplicates = parseResult.skippedDuplicates
        )
        return report
    }

    /**
     * Pre-import check validating a list of incoming flashcard entities against the database.
     */
    suspend fun preImportCheck(
        incomingCards: List<FlashcardEntity>,
        totalRows: Int = incomingCards.size,
        initialSkippedDuplicates: Int = 0
    ): PreImportReport {
        val (_, report) = validateIncomingAgainstDatabase(
            incomingCards = incomingCards,
            totalRows = totalRows,
            initialSkippedDuplicates = initialSkippedDuplicates
        )
        return report
    }

    /**
     * Imports an Anki CSV by running a strict pre-import check:
     * 1. Validates L1, L2, L3 folder hierarchy against existing DB folders to prevent duplicate folders
     *    caused by whitespace, letter-case, or format variations.
     * 2. Checks content hashes against the database to prevent redundant flashcards during subsequent imports.
     * 3. Only inserts newly verified cards, avoiding redundant writes.
     */
    suspend fun importAnkiCsv(csvContent: String): CsvParseResult {
        val parseResult = AnkiCsvImporter.parseAnkiCsv(csvContent)
        if (parseResult.validCards.isEmpty()) {
            val emptyReport = PreImportReport(
                totalIncomingRecords = parseResult.totalRows,
                validRecordsParsed = 0,
                redundantCardsSkipped = parseResult.skippedDuplicates,
                newCardsToInsert = 0,
                isFullyDuplicateDeck = false
            )
            return parseResult.copy(preImportReport = emptyReport)
        }

        val (validatedCardsToInsert, report) = validateIncomingAgainstDatabase(
            incomingCards = parseResult.validCards,
            totalRows = parseResult.totalRows,
            initialSkippedDuplicates = parseResult.skippedDuplicates
        )

        if (validatedCardsToInsert.isNotEmpty()) {
            dao.insertAll(validatedCardsToInsert)
        }

        return parseResult.copy(
            validCards = validatedCardsToInsert,
            skippedDuplicates = report.redundantCardsSkipped,
            preImportReport = report
        )
    }

    /**
     * Core validation algorithm checking incoming records against the database.
     */
    private suspend fun validateIncomingAgainstDatabase(
        incomingCards: List<FlashcardEntity>,
        totalRows: Int,
        initialSkippedDuplicates: Int
    ): Pair<List<FlashcardEntity>, PreImportReport> {
        if (incomingCards.isEmpty()) {
            val emptyReport = PreImportReport(
                totalIncomingRecords = totalRows,
                validRecordsParsed = 0,
                redundantCardsSkipped = initialSkippedDuplicates,
                newCardsToInsert = 0,
                isFullyDuplicateDeck = false
            )
            return Pair(emptyList(), emptyReport)
        }

        // 1. Fetch current database state
        val existingHashes = dao.getAllContentHashes().toSet()
        val existingFolders = dao.getAllDistinctFolders()

        // 2. Build canonical folder lookups from existing DB records
        val canonicalL1Map = existingFolders.map { it.l1 }.distinct().associateBy { normalizeFolderKey(it) }
        val canonicalL2Map = existingFolders.map { it.l1 to it.l2 }.distinct()
            .associate { (l1, l2) -> "${normalizeFolderKey(l1)}::${normalizeFolderKey(l2)}" to l2 }
        val canonicalL3Map = existingFolders.map { Triple(it.l1, it.l2, it.l3) }.distinct()
            .associate { (l1, l2, l3) -> "${normalizeFolderKey(l1)}::${normalizeFolderKey(l2)}::${normalizeFolderKey(l3)}" to l3 }

        val matchedL1 = mutableSetOf<String>()
        val newL1 = mutableSetOf<String>()
        val matchedL2 = mutableSetOf<String>()
        val newL2 = mutableSetOf<String>()
        val matchedL3 = mutableSetOf<String>()
        val newL3 = mutableSetOf<String>()

        var normalizationsCount = 0
        var redundantCount = initialSkippedDuplicates
        val batchHashes = mutableSetOf<String>()
        val validatedCardsToInsert = mutableListOf<FlashcardEntity>()

        for (card in incomingCards) {
            // Folder Normalization & Validation to prevent duplicate folders
            val normL1Key = normalizeFolderKey(card.l1)
            val canonicalL1 = if (canonicalL1Map.containsKey(normL1Key)) {
                val dbL1 = canonicalL1Map[normL1Key]!!
                if (dbL1 != card.l1) normalizationsCount++
                matchedL1.add(dbL1)
                dbL1
            } else {
                val cleanL1 = card.l1.trim()
                newL1.add(cleanL1)
                cleanL1
            }

            val normL2Key = "${normalizeFolderKey(canonicalL1)}::${normalizeFolderKey(card.l2)}"
            val canonicalL2 = if (canonicalL2Map.containsKey(normL2Key)) {
                val dbL2 = canonicalL2Map[normL2Key]!!
                if (dbL2 != card.l2) normalizationsCount++
                matchedL2.add(dbL2)
                dbL2
            } else {
                val cleanL2 = card.l2.trim()
                newL2.add(cleanL2)
                cleanL2
            }

            val normL3Key = "${normalizeFolderKey(canonicalL1)}::${normalizeFolderKey(canonicalL2)}::${normalizeFolderKey(card.l3)}"
            val canonicalL3 = if (canonicalL3Map.containsKey(normL3Key)) {
                val dbL3 = canonicalL3Map[normL3Key]!!
                if (dbL3 != card.l3) normalizationsCount++
                matchedL3.add(dbL3)
                dbL3
            } else {
                val cleanL3 = card.l3.trim()
                newL3.add(cleanL3)
                cleanL3
            }

            // Calculate canonical hash with normalized folders and trimmed HTML
            val canonicalHash = sha256("$canonicalL1::$canonicalL2::$canonicalL3|${card.frontHtml.trim()}|${card.backHtml.trim()}")

            // Redundant Flashcard Check: matches against existing DB or current batch
            if (existingHashes.contains(canonicalHash) ||
                existingHashes.contains(card.contentHash) ||
                batchHashes.contains(canonicalHash)
            ) {
                redundantCount++
            } else {
                batchHashes.add(canonicalHash)
                val canonicalDeckRaw = "$canonicalL1::$canonicalL2::$canonicalL3"
                validatedCardsToInsert.add(
                    card.copy(
                        l1 = canonicalL1,
                        l2 = canonicalL2,
                        l3 = canonicalL3,
                        deckRaw = canonicalDeckRaw,
                        contentHash = canonicalHash
                    )
                )
            }
        }

        val report = PreImportReport(
            totalIncomingRecords = totalRows,
            validRecordsParsed = incomingCards.size,
            redundantCardsSkipped = redundantCount,
            newCardsToInsert = validatedCardsToInsert.size,
            matchedExistingL1Folders = matchedL1.toList().sorted(),
            newL1FoldersToCreate = newL1.toList().sorted(),
            matchedExistingL2Folders = matchedL2.toList().sorted(),
            newL2FoldersToCreate = newL2.toList().sorted(),
            matchedExistingL3Folders = matchedL3.toList().sorted(),
            newL3FoldersToCreate = newL3.toList().sorted(),
            folderNormalizationsApplied = normalizationsCount,
            isFullyDuplicateDeck = validatedCardsToInsert.isEmpty() && incomingCards.isNotEmpty()
        )

        return Pair(validatedCardsToInsert, report)
    }

    private fun normalizeFolderKey(name: String): String {
        return name.trim().replace(Regex("\\s+"), " ").lowercase()
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun recordReview(card: FlashcardEntity, rating: Int) {
        // rating: 1 = Again (Errei), 2 = Hard (Difícil), 3 = Good (Bom), 4 = Easy (Fácil)
        val now = System.currentTimeMillis()
        val oneDay = 24L * 3600L * 1000L

        var newEase = card.easeFactor
        var newInterval = card.intervalDays
        var newLapses = card.lapses
        val newReps = card.reps + 1

        when (rating) {
            1 -> {
                // Again
                newInterval = 1
                newLapses++
                newEase = max(1.3f, newEase - 0.2f)
            }
            2 -> {
                // Hard
                newInterval = max(1, (newInterval * 1.2f).roundToInt())
                newEase = max(1.3f, newEase - 0.15f)
            }
            3 -> {
                // Good
                newInterval = if (card.reps == 0) 1 else if (card.reps == 1) 3 else max(1, (newInterval * newEase).roundToInt())
            }
            4 -> {
                // Easy
                newInterval = if (card.reps == 0) 3 else max(1, (newInterval * newEase * 1.3f).roundToInt())
                newEase += 0.15f
            }
        }

        val masteryLevel = when {
            newInterval >= 7 && newReps >= 3 -> 2 // Dominado
            newReps > 0 -> 1 // Em aprendizado
            else -> 0
        }

        val nextDue = calculateNextDueDate(newInterval, now)

        val updated = card.copy(
            intervalDays = newInterval,
            easeFactor = newEase,
            reps = newReps,
            lapses = newLapses,
            masteryLevel = masteryLevel,
            dueTimestamp = nextDue,
            lastReviewedTimestamp = now
        )

        dao.updateFlashcard(updated)
    }

    /**
     * Calculates next due timestamp aligning with Anki's standard 04:00 AM day cutoff.
     * Prevents cards reviewed late at night from being hidden on the subsequent review morning.
     */
    fun calculateNextDueDate(intervalDays: Int, now: Long = System.currentTimeMillis()): Long {
        if (intervalDays <= 0) return now
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            if (get(java.util.Calendar.HOUR_OF_DAY) < 4) {
                add(java.util.Calendar.DAY_OF_YEAR, -1)
            }
            set(java.util.Calendar.HOUR_OF_DAY, 4)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            add(java.util.Calendar.DAY_OF_YEAR, intervalDays)
        }
        return calendar.timeInMillis
    }

    suspend fun saveCard(card: FlashcardEntity): FlashcardEntity {
        val cleanL1 = card.l1.trim()
        val cleanL2 = card.l2.trim().ifBlank { "Geral" }
        val cleanL3 = card.l3.trim().ifBlank { "Tópico Geral" }
        val canonicalDeckRaw = "$cleanL1 :: $cleanL2 :: $cleanL3"
        val canonicalHash = sha256("$cleanL1::$cleanL2::$cleanL3|${card.frontHtml.trim()}|${card.backHtml.trim()}")

        val preparedCard = card.copy(
            l1 = cleanL1,
            l2 = cleanL2,
            l3 = cleanL3,
            deckRaw = canonicalDeckRaw,
            contentHash = canonicalHash
        )

        return if (preparedCard.id == 0L) {
            val insertedId = dao.insertFlashcard(preparedCard)
            preparedCard.copy(id = insertedId)
        } else {
            dao.updateFlashcard(preparedCard)
            preparedCard
        }
    }

    suspend fun deleteCard(card: FlashcardEntity) {
        dao.deleteFlashcard(card)
    }

    suspend fun getAllHierarchyFolders(): List<HierarchyFolderTuple> {
        return dao.getAllDistinctFolders()
    }

    fun calculateProgressReport(cards: List<FlashcardEntity>): StudyProgressReport {
        if (cards.isEmpty()) return StudyProgressReport()

        val now = System.currentTimeMillis()
        val totalCards = cards.size
        val totalReviews = cards.sumOf { it.reps }
        val mastered = cards.count { it.masteryLevel == 2 }
        val learning = cards.count { it.masteryLevel == 1 }
        val newCount = cards.count { it.masteryLevel == 0 }
        val dueNow = cards.count { it.reps > 0 && it.dueTimestamp <= now }
        val overallMastery = if (totalCards > 0) {
            ((mastered * 1.0f + learning * 0.4f) / totalCards) * 100f
        } else 0f

        // Calculate 7-day study frequency in chronological order (SEG, TER, QUA, QUI, SEX, SÁB, DOM)
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.firstDayOfWeek = Calendar.MONDAY

        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = when (currentDayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)

        val dailyStats = (0..6).map { dayIndex ->
            val dayCal = Calendar.getInstance()
            dayCal.timeInMillis = cal.timeInMillis
            dayCal.add(Calendar.DAY_OF_YEAR, dayIndex)

            dayCal.set(Calendar.HOUR_OF_DAY, 0)
            dayCal.set(Calendar.MINUTE, 0)
            dayCal.set(Calendar.SECOND, 0)
            dayCal.set(Calendar.MILLISECOND, 0)
            val dayStart = dayCal.timeInMillis

            dayCal.set(Calendar.HOUR_OF_DAY, 23)
            dayCal.set(Calendar.MINUTE, 59)
            dayCal.set(Calendar.SECOND, 59)
            dayCal.set(Calendar.MILLISECOND, 999)
            val dayEnd = dayCal.timeInMillis

            val dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK)
            val dayLabel = when (dayOfWeek) {
                Calendar.MONDAY -> "SEG"
                Calendar.TUESDAY -> "TER"
                Calendar.WEDNESDAY -> "QUA"
                Calendar.THURSDAY -> "QUI"
                Calendar.FRIDAY -> "SEX"
                Calendar.SATURDAY -> "SÁB"
                Calendar.SUNDAY -> "DOM"
                else -> "DIA"
            }

            val todayCal = Calendar.getInstance()
            todayCal.timeInMillis = now
            val isToday = (todayCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                           todayCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR))

            val cardsOnDay = cards.filter {
                it.lastReviewedTimestamp in dayStart..dayEnd
            }

            DayStudyStat(
                dayLabel = dayLabel,
                dateFormatted = dateFormat.format(Date(dayStart)),
                timestamp = dayStart,
                reviewsCount = cardsOnDay.size,
                uniqueCardsCount = cardsOnDay.distinctBy { it.id }.size,
                isToday = isToday
            )
        }.sortedBy { stat ->
            val statCal = Calendar.getInstance().apply { timeInMillis = stat.timestamp }
            when (statCal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 7
            }
        }

        // Calculate streak efficiently without creating 365 Calendar instances
        var streak = 0
        if (totalReviews > 0 && cards.isNotEmpty()) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = now
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val todayStartMs = cal.timeInMillis
            val dayMs = 24L * 3600L * 1000L

            val reviewedDayOffsets = cards
                .asSequence()
                .map { it.lastReviewedTimestamp }
                .filter { it > 0 }
                .map { ts ->
                    if (ts >= todayStartMs) {
                        0
                    } else {
                        ((todayStartMs - ts) / dayMs + 1).toInt()
                    }
                }
                .toSet()

            var checkOffset = 0
            while (checkOffset < 365) {
                if (reviewedDayOffsets.contains(checkOffset)) {
                    streak++
                    checkOffset++
                } else if (checkOffset == 0) {
                    // Today has not been reviewed yet
                    checkOffset++
                } else {
                    break
                }
            }
        }
        if (streak == 0 && totalReviews > 0) streak = 1

        // Calculate Domain Mastery per L2 Discipline
        val domainMap = cards.groupBy { it.l2 }
        val domainMasteryList = domainMap.map { (domain, domainCards) ->
            val dTotal = domainCards.size
            val dMastered = domainCards.count { it.masteryLevel == 2 }
            val dLearning = domainCards.count { it.masteryLevel == 1 }
            val dNew = domainCards.count { it.masteryLevel == 0 }
            val dScore = if (dTotal > 0) ((dMastered * 1.0f + dLearning * 0.4f) / dTotal) * 100f else 0f
            DomainMasteryStat(
                domainName = domain,
                colorHex = DisciplinePalette.getColorForDiscipline(domain),
                totalCards = dTotal,
                masteredCards = dMastered,
                learningCards = dLearning,
                newCards = dNew,
                masteryPercentage = dScore
            )
        }.sortedByDescending { it.totalCards }

        return StudyProgressReport(
            totalCards = totalCards,
            totalReviews = totalReviews,
            currentStreakDays = streak,
            overallMasteryPercentage = overallMastery,
            studyFrequency7Days = dailyStats,
            domainMasteryList = domainMasteryList,
            dueNowCount = dueNow,
            learningCount = learning,
            masteredCount = mastered,
            newCount = newCount
        )
    }

    fun parseCardTags(rawTags: String): List<String> {
        if (rawTags.isBlank()) return emptyList()
        return rawTags.split(Regex("[,\\s]+"))
            .map { it.trim().removePrefix("#") }
            .filter { it.isNotBlank() }
            .distinct()
    }

    fun extractAllTags(cards: List<FlashcardEntity>): List<String> {
        return cards.flatMap { parseCardTags(it.tags) }
            .distinct()
            .sorted()
    }

    /**
     * Smart Shuffle: Prioritizes overdue cards and lower mastery levels before showing new cards.
     */
    fun applySmartShuffle(cards: List<FlashcardEntity>): List<FlashcardEntity> {
        val now = System.currentTimeMillis()
        // Tier 1: Overdue cards (dueTimestamp <= now && reps > 0)
        val overdueCards = cards.filter { it.dueTimestamp <= now && it.reps > 0 }
            .sortedWith(
                compareBy<FlashcardEntity> { it.masteryLevel }
                    .thenByDescending { it.lapses }
                    .thenBy { it.easeFactor }
                    .thenBy { it.dueTimestamp }
            )

        val overdueIds = overdueCards.map { it.id }.toSet()

        // Tier 2: Lower mastery level cards (learning or prone to lapses)
        val lowMasteryCards = cards.filter { !overdueIds.contains(it.id) && (it.masteryLevel == 1 || (it.reps > 0 && it.masteryLevel < 2)) }
            .sortedWith(
                compareBy<FlashcardEntity> { it.masteryLevel }
                    .thenByDescending { it.lapses }
                    .thenBy { it.easeFactor }
            )

        val lowMasteryIds = lowMasteryCards.map { it.id }.toSet()

        // Tier 3: New cards (never reviewed, reps == 0)
        val newCards = cards.filter { !overdueIds.contains(it.id) && !lowMasteryIds.contains(it.id) && it.reps == 0 && it.masteryLevel == 0 }
            .shuffled()

        val handledIds = overdueIds + lowMasteryIds + newCards.map { it.id }.toSet()

        // Tier 4: Mastered / mature cards
        val masteredCards = cards.filter { !handledIds.contains(it.id) }
            .sortedBy { it.dueTimestamp }

        return overdueCards + lowMasteryCards + newCards + masteredCards
    }

    fun filterCardsForStudySession(
        cards: List<FlashcardEntity>,
        l1: String?,
        l2: String?,
        l3: String?,
        tag: String? = null,
        mode: StudyFilterMode = StudyFilterMode.DUE_ONLY,
        limit: Int = 20,
        smartShuffle: Boolean = true
    ): List<FlashcardEntity> {
        val now = System.currentTimeMillis()
        var filtered = cards

        // Cross-domain tag filtering
        if (!tag.isNullOrBlank()) {
            val targetTag = tag.trim().lowercase()
            filtered = filtered.filter { card ->
                val cardTags = parseCardTags(card.tags).map { it.lowercase() }
                cardTags.contains(targetTag)
            }
        }

        if (!l1.isNullOrBlank()) {
            filtered = filtered.filter { it.l1 == l1 }
        }
        if (!l2.isNullOrBlank()) {
            filtered = filtered.filter { it.l2 == l2 }
        }
        if (!l3.isNullOrBlank()) {
            filtered = filtered.filter { it.l3 == l3 }
        }

        filtered = when (mode) {
            StudyFilterMode.ALL -> filtered
            StudyFilterMode.DUE_ONLY -> {
                val due = filtered.filter { it.reps > 0 && it.dueTimestamp <= now }
                if (due.isNotEmpty()) due else filtered
            }
            StudyFilterMode.NEW_AND_LEARNING -> {
                val candidate = filtered.filter { it.masteryLevel < 2 || it.reps == 0 }
                if (candidate.isNotEmpty()) candidate else filtered
            }
            StudyFilterMode.DIFFICULT_ONLY -> {
                val hard = filtered.filter { it.lapses > 0 || it.easeFactor < 2.4f }
                if (hard.isNotEmpty()) hard else filtered
            }
        }

        val ordered = if (smartShuffle) {
            applySmartShuffle(filtered)
        } else {
            filtered.shuffled()
        }

        return if (limit > 0) ordered.take(limit) else ordered
    }

    suspend fun checkAndSeedInitialData() {
        if (dao.countTotal() == 0) {
            dao.insertAll(SampleData.getInitialCards())
        }
    }

    suspend fun moveL3TopicsToNewL2(
        l1: String,
        topicsToMove: List<Pair<String, String>>,
        newL2: String
    ) {
        val cleanNewL2 = newL2.trim().ifBlank { "Novo Baralho L2" }
        for ((oldL2, l3) in topicsToMove) {
            dao.moveL3TopicToNewL2(
                l1 = l1,
                oldL2 = oldL2,
                l3 = l3,
                newL2 = cleanNewL2
            )
        }
    }

    suspend fun moveMultipleL2ToL1(
        itemsToMove: List<Pair<String, String>>, // list of (sourceL1, l2)
        newL1: String
    ) {
        val cleanNewL1 = newL1.trim().ifBlank { "Novo Baralho L1" }
        for ((oldL1, l2) in itemsToMove) {
            dao.moveL2ToL1(oldL1 = oldL1, l2 = l2, newL1 = cleanNewL1)
        }
    }

    suspend fun exportL1ToCsv(l1Target: String? = null): String {
        val cards = if (l1Target.isNullOrBlank() || l1Target == "TODOS") {
            dao.getAllFlashcardsSync()
        } else {
            dao.getCardsByL1Sync(l1Target)
        }

        val sb = StringBuilder()
        // Standard Anki CSV Header
        sb.append("Deck,Tipo de Nota,Frente,Verso,Etiquetas\n")

        for (card in cards) {
            val deckRawEsc = escapeCsvField(card.deckRaw)
            val noteTypeEsc = escapeCsvField(card.noteType)
            val frontEsc = escapeCsvField(card.frontHtml)
            val backEsc = escapeCsvField(card.backHtml)
            val tagsEsc = escapeCsvField(card.tags)

            sb.append("$deckRawEsc,$noteTypeEsc,$frontEsc,$backEsc,$tagsEsc\n")
        }

        return sb.toString()
    }

    private fun escapeCsvField(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\""
        }
        return value
    }

    suspend fun getCardsByL2(l1: String, l2: String): List<FlashcardEntity> {
        return dao.getCardsByL1AndL2Sync(l1, l2)
    }

    suspend fun restoreCardsFromCloud(cards: List<FlashcardEntity>) {
        dao.insertOrReplaceAll(cards)
    }

    suspend fun deleteDeck(l1: String) {
        dao.deleteDeckL1(l1)
    }

    suspend fun renameL1Deck(oldL1: String, newL1: String) {
        dao.renameL1Deck(oldL1, newL1)
    }

    suspend fun renameL2Discipline(l1: String, oldL2: String, newL2: String) {
        dao.renameL2Discipline(l1, oldL2, newL2)
    }

    suspend fun deleteL2Discipline(l1: String, l2: String) {
        dao.deleteL2Discipline(l1, l2)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    companion object {
        fun parseCardTags(rawTags: String): List<String> {
            if (rawTags.isBlank()) return emptyList()
            return rawTags.split(Regex("[,\\s]+"))
                .map { it.trim().removePrefix("#") }
                .filter { it.isNotBlank() }
                .distinct()
        }

        fun hasTag(card: FlashcardEntity, targetTag: String): Boolean {
            if (targetTag.isBlank()) return true
            val norm = targetTag.trim().lowercase().removePrefix("#")
            return parseCardTags(card.tags).any { it.trim().lowercase() == norm }
        }

        fun applySmartShuffle(cards: List<FlashcardEntity>, now: Long = System.currentTimeMillis()): List<FlashcardEntity> {
            // Tier 1: Overdue cards (dueTimestamp <= now && reps > 0)
            val overdueCards = cards.filter { it.dueTimestamp <= now && it.reps > 0 }
                .sortedWith(
                    compareBy<FlashcardEntity> { it.masteryLevel }
                        .thenByDescending { it.lapses }
                        .thenBy { it.easeFactor }
                        .thenBy { it.dueTimestamp }
                )

            val overdueIds = overdueCards.map { it.id }.toSet()

            // Tier 2: Lower mastery level cards (learning or prone to lapses)
            val lowMasteryCards = cards.filter { !overdueIds.contains(it.id) && (it.masteryLevel == 1 || (it.reps > 0 && it.masteryLevel < 2)) }
                .sortedWith(
                    compareBy<FlashcardEntity> { it.masteryLevel }
                        .thenByDescending { it.lapses }
                        .thenBy { it.easeFactor }
                )

            val lowMasteryIds = lowMasteryCards.map { it.id }.toSet()

            // Tier 3: New cards (never reviewed, reps == 0)
            val newCards = cards.filter { !overdueIds.contains(it.id) && !lowMasteryIds.contains(it.id) && it.reps == 0 && it.masteryLevel == 0 }
                .shuffled()

            val handledIds = overdueIds + lowMasteryIds + newCards.map { it.id }.toSet()

            // Tier 4: Mastered / mature cards
            val masteredCards = cards.filter { !handledIds.contains(it.id) }
                .sortedBy { it.dueTimestamp }

            return overdueCards + lowMasteryCards + newCards + masteredCards
        }
    }
}
