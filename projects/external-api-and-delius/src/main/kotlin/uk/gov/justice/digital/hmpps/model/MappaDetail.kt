package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class MappaDetail(
    val level: Int?,
    val levelDescription: String?,
    val category: Int?,
    val categoryDescription: String?,
    val startDate: LocalDate,
    val reviewDate: LocalDate?,
    val notes: String?
)

enum class Level(val number: Int) { M0(0), M1(1), M2(2), M3(3) }

enum class Category(val number: Int) { X9(0), M1(1), M2(2), M3(3), M4(4) }

fun String.toMappaLevel() = Level.entries.find { it.name == this }?.number
    ?: throw IllegalStateException("Unexpected MAPPA level: $this")

fun String.toMappaCategory() = Category.entries.find { it.name == this }?.number
    ?: throw IllegalStateException("Unexpected MAPPA category: $this")