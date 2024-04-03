package uk.gov.justice.digital.hmpps.api.model.sentence

import java.time.LocalDate

data class CourtDocument(
    val id: Long,
    val lastSaved: LocalDate,
    val description: String
)
