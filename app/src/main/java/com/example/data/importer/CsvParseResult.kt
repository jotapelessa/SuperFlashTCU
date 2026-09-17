package com.example.data.importer

import com.example.data.model.FlashcardEntity

data class CsvParseResult(
    val totalRows: Int,
    val validCards: List<FlashcardEntity>,
    val skippedDuplicates: Int,
    val errors: List<RowError>,
    val preImportReport: PreImportReport? = null
)

data class PreImportReport(
    val totalIncomingRecords: Int,
    val validRecordsParsed: Int,
    val redundantCardsSkipped: Int,
    val newCardsToInsert: Int,
    val matchedExistingL1Folders: List<String> = emptyList(),
    val newL1FoldersToCreate: List<String> = emptyList(),
    val matchedExistingL2Folders: List<String> = emptyList(),
    val newL2FoldersToCreate: List<String> = emptyList(),
    val matchedExistingL3Folders: List<String> = emptyList(),
    val newL3FoldersToCreate: List<String> = emptyList(),
    val folderNormalizationsApplied: Int = 0,
    val isFullyDuplicateDeck: Boolean = false
)

data class RowError(
    val rowNumber: Int,
    val rawPreview: String,
    val reason: String
)

