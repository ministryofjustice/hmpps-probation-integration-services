package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Registration
import java.time.LocalDate

data class MappaAndRoshHistory(
    val personalDetails: PersonalDetailsOverview,
    val mappa: Mappa?,
    val roshHistory: List<Rosh>
) {
    data class Mappa(
        val category: Int?,
        val level: Int?,
        val startDate: LocalDate
    )
    data class Rosh(
        val active: Boolean,
        val type: String,
        val typeDescription: String,
        val notes: String?,
        val startDate: LocalDate
    )
}

fun Registration.toMappa() = MappaAndRoshHistory.Mappa(
    category = category?.code?.toMappaCategory(),
    level = level?.code?.toMappaLevel(),
    startDate = date
)

fun Registration.toRosh() = MappaAndRoshHistory.Rosh(
    active = !deregistered,
    type = type.code,
    typeDescription = type.description,
    notes = notes,
    startDate = date
)

enum class Category(val number: Int) { X9(0), M1(1), M2(2), M3(3), M4(4) }
private fun String.toMappaCategory() = Category.values().find { it.name == this }?.number
    ?: throw IllegalStateException("Unexpected MAPPA category: $this")

enum class Level(val number: Int) { M0(0), M1(1), M2(2), M3(3) }
private fun String.toMappaLevel() = Level.values().find { it.name == this }?.number
    ?: throw IllegalStateException("Unexpected MAPPA level: $this")
