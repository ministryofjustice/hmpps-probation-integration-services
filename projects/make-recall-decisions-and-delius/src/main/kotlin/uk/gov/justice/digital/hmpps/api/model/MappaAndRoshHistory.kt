package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Registration
import java.time.LocalDate

data class MappaAndRoshHistory(
    val personalDetails: PersonalDetailsOverview,
    val mappa: Mappa?,
    val roshHistory: List<Rosh>,
) {
    data class Rosh(
        val active: Boolean,
        val type: String,
        val typeDescription: String,
        val notes: String?,
        val startDate: LocalDate,
    )
}

fun Registration.toRosh() =
    MappaAndRoshHistory.Rosh(
        active = !deregistered,
        type = type.code,
        typeDescription = type.description,
        notes = notes,
        startDate = date,
    )
