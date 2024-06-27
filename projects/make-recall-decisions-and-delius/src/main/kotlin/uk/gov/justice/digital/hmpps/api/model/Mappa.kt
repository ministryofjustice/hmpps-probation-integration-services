package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Registration
import java.time.LocalDate

data class Mappa(
    val category: Int?,
    val level: Int?,
    val startDate: LocalDate
)

fun Registration.toMappa() = Mappa(
    category = category?.code?.toMappaCategory(),
    level = level?.code?.toMappaLevel(),
    startDate = date
)

enum class Category(val number: Int) {
    X9(0), // X9 = Legacy category code, still referenced by some old cases
    M0(0), // M0 = Legacy category code, still referenced by some old cases
    M1(1),
    M2(2),
    M3(3),
    M4(4)
}

private fun String.toMappaCategory() = Category.entries.find { it.name == this }?.number
    ?: throw IllegalStateException("Unexpected MAPPA category: $this")

enum class Level(val number: Int) {
    M0(0),
    M1(1),
    M2(2),
    M3(3)
}

private fun String.toMappaLevel() = Level.entries.find { it.name == this }?.number
    ?: throw IllegalStateException("Unexpected MAPPA level: $this")
