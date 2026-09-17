package com.example.data.importer

import com.example.data.model.FlashcardEntity
import java.security.MessageDigest

object AnkiCsvImporter {

    fun parseAnkiCsv(csvContent: String): CsvParseResult {
        // Strip BOM if present
        val cleanContent = csvContent.removePrefix("\uFEFF").trim()
        if (cleanContent.isEmpty()) {
            return CsvParseResult(0, emptyList(), 0, emptyList())
        }

        val delimiter = detectDelimiter(cleanContent)
        val rawRows = parseCsvLines(cleanContent, delimiter)

        if (rawRows.isEmpty()) {
            return CsvParseResult(0, emptyList(), 0, emptyList())
        }

        // Determine header indices
        val firstRow = rawRows.first()
        val hasHeader = isHeaderRow(firstRow)

        var deckIdx = -1
        var noteTypeIdx = -1
        var frontIdx = -1
        var backIdx = -1
        var tagsIdx = -1

        val rowsToProcess: List<List<String>>

        if (hasHeader) {
            firstRow.forEachIndexed { index, colName ->
                val col = colName.trim().lowercase()
                when {
                    col.contains("deck") || col.contains("baralho") -> deckIdx = index
                    col.contains("note type") || col.contains("tipo de nota") || col.contains("modelo") -> noteTypeIdx = index
                    col.contains("field 1") || col.contains("front") || col.contains("frente") || col.contains("pergunta") -> frontIdx = index
                    col.contains("field 2") || col.contains("back") || col.contains("verso") || col.contains("resposta") -> backIdx = index
                    col.contains("tag") || col.contains("etiqueta") -> tagsIdx = index
                }
            }
            rowsToProcess = rawRows.drop(1)
        } else {
            rowsToProcess = rawRows
            // Default Anki export column order
            val maxCols = rawRows.maxOfOrNull { it.size } ?: 0
            when {
                maxCols >= 5 -> {
                    deckIdx = 0
                    noteTypeIdx = 1
                    frontIdx = 2
                    backIdx = 3
                    tagsIdx = 4
                }
                maxCols == 4 -> {
                    deckIdx = 0
                    frontIdx = 1
                    backIdx = 2
                    tagsIdx = 3
                }
                maxCols == 3 -> {
                    deckIdx = 0
                    frontIdx = 1
                    backIdx = 2
                }
                else -> {
                    frontIdx = 0
                    backIdx = 1
                }
            }
        }

        val validCards = mutableListOf<FlashcardEntity>()
        val errors = mutableListOf<RowError>()
        val seenHashes = mutableSetOf<String>()
        var skippedDuplicates = 0

        rowsToProcess.forEachIndexed { index, row ->
            val rowNumber = if (hasHeader) index + 2 else index + 1
            if (row.all { it.isBlank() }) {
                // Ignore completely empty lines as per instructions
                return@forEachIndexed
            }

            val front = row.getOrNull(frontIdx)?.trim().orEmpty()
            val back = row.getOrNull(backIdx)?.trim().orEmpty()
            val deckRaw = (if (deckIdx >= 0) row.getOrNull(deckIdx)?.trim() else null).orEmpty()
            val noteType = (if (noteTypeIdx >= 0) row.getOrNull(noteTypeIdx)?.trim() else null)
                .takeUnless { it.isNullOrBlank() } ?: "Básico+"
            val tags = (if (tagsIdx >= 0) row.getOrNull(tagsIdx)?.trim() else null).orEmpty()

            if (front.isBlank() && back.isBlank()) {
                errors.add(
                    RowError(
                        rowNumber = rowNumber,
                        rawPreview = row.joinToString(" | ").take(80),
                        reason = "Campos de pergunta e resposta vazios"
                    )
                )
                return@forEachIndexed
            }

            // Hierarquia L1 / L2 / L3
            val hierarchy = parseDeckHierarchy(deckRaw)
            val l1 = hierarchy.l1
            val l2 = hierarchy.l2
            val l3 = hierarchy.l3

            // Hash for deduplication
            val hash = sha256("$l1::$l2::$l3|$front|$back")
            if (seenHashes.contains(hash)) {
                skippedDuplicates++
                return@forEachIndexed
            }
            seenHashes.add(hash)

            validCards.add(
                FlashcardEntity(
                    deckRaw = deckRaw.ifBlank { "$l1::$l2::$l3" },
                    l1 = l1,
                    l2 = l2,
                    l3 = l3,
                    noteType = noteType,
                    frontHtml = front,
                    backHtml = back,
                    tags = tags,
                    contentHash = hash,
                    intervalDays = 0,
                    easeFactor = 2.5f,
                    reps = 0,
                    lapses = 0,
                    masteryLevel = 0,
                    dueTimestamp = System.currentTimeMillis() // Due immediately for initial review
                )
            )
        }

        return CsvParseResult(
            totalRows = rowsToProcess.size,
            validCards = validCards,
            skippedDuplicates = skippedDuplicates,
            errors = errors
        )
    }

    data class ParsedHierarchy(val l1: String, val l2: String, val l3: String)

    fun parseDeckHierarchy(deckRaw: String): ParsedHierarchy {
        if (deckRaw.isBlank()) {
            return ParsedHierarchy(
                l1 = "Deck Geral",
                l2 = "Geral",
                l3 = "Geral"
            )
        }

        // Split by "::"
        val parts = deckRaw.split("::").map { it.trim() }.filter { it.isNotEmpty() }
        return when {
            parts.isEmpty() -> ParsedHierarchy("Deck Geral", "Geral", "Geral")
            parts.size == 1 -> ParsedHierarchy(parts[0], "Geral", "Geral")
            parts.size == 2 -> ParsedHierarchy(parts[0], parts[1], "Geral")
            parts.size == 3 -> ParsedHierarchy(parts[0], parts[1], parts[2])
            else -> {
                // L1::L2::L3::L4...
                // Robustness rule: [0]=L1, [1]=L2, [2..]=joined L3
                val l1 = parts[0]
                val l2 = parts[1]
                val l3 = parts.drop(2).joinToString(" - ")
                ParsedHierarchy(l1, l2, l3)
            }
        }
    }

    private fun detectDelimiter(content: String): Char {
        // Sample lines outside of quotes
        val sample = content.lineSequence().take(15).joinToString("\n")
        var inQuotes = false
        var commas = 0
        var semicolons = 0
        var tabs = 0

        for (char in sample) {
            when (char) {
                '"' -> inQuotes = !inQuotes
                ',' -> if (!inQuotes) commas++
                ';' -> if (!inQuotes) semicolons++
                '\t' -> if (!inQuotes) tabs++
            }
        }

        return when {
            tabs > commas && tabs > semicolons -> '\t'
            semicolons >= commas -> ';'
            else -> ','
        }
    }

    private fun parseCsvLines(content: String, delimiter: Char): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentField = java.lang.StringBuilder()
        var inQuotes = false
        var i = 0
        val length = content.length

        while (i < length) {
            val c = content[i]

            if (c == '"') {
                if (inQuotes && i + 1 < length && content[i + 1] == '"') {
                    // Escaped quote
                    currentField.append('"')
                    i += 2
                    continue
                } else {
                    inQuotes = !inQuotes
                    i++
                    continue
                }
            }

            if (!inQuotes && c == delimiter) {
                currentRow.add(currentField.toString())
                currentField.setLength(0)
                i++
                continue
            }

            if (!inQuotes && (c == '\n' || c == '\r')) {
                // End of row
                if (c == '\r' && i + 1 < length && content[i + 1] == '\n') {
                    i++ // Skip '\n' of '\r\n'
                }
                currentRow.add(currentField.toString())
                currentField.setLength(0)
                if (currentRow.any { it.isNotBlank() }) {
                    rows.add(ArrayList(currentRow))
                }
                currentRow.clear()
                i++
                continue
            }

            currentField.append(c)
            i++
        }

        // Add trailing field and row
        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString())
            if (currentRow.any { it.isNotBlank() }) {
                rows.add(currentRow)
            }
        }

        return rows
    }

    private fun isHeaderRow(row: List<String>): Boolean {
        val line = row.joinToString(" ").lowercase()
        return (line.contains("deck") || line.contains("baralho")) &&
                (line.contains("field") || line.contains("front") || line.contains("note") || line.contains("frente") || line.contains("pergunta"))
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
