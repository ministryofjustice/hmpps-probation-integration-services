package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class ReleaseRecall(
    val lastRelease: Release?,
    val lastRecall: Recall?
)

data class Release(
    val date: LocalDate,
    val notes: String?,
    val institution: Institution?,
    val reason: CodeDescription
)

data class Recall(
    val date: LocalDate,
    val reason: CodeDescription,
    val notes: String?
)

data class Institution(
    val institutionId: Long,
    val isEstablishment: Boolean,
    val code: String,
    val description: String,
    val institutionName: String?,
    val establishmentType: CodeDescription?,
    val isPrivate: Boolean?,
    val nomsPrisonInstitutionCode: String?
)

