package uk.gov.justice.digital.hmpps.api.model.sentence

import java.time.LocalDate

data class CourtDocument(
    val id: String,
    val lastSaved: LocalDate?,
    val documentName: String?
)
