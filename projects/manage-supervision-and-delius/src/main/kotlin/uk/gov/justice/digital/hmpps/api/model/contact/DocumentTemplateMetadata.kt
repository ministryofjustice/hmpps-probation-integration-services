package uk.gov.justice.digital.hmpps.api.model.contact

import java.time.LocalDate

data class DocumentTemplateMetadata(
    val id: Long,
    val name: String,
    val dateCreated: LocalDate
)
