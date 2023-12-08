package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Release as ReleaseEntity

data class Release(
    val releaseDate: LocalDate,
    val recallDate: LocalDate?,
)

fun ReleaseEntity.dates() = Release(date, recall?.date)
