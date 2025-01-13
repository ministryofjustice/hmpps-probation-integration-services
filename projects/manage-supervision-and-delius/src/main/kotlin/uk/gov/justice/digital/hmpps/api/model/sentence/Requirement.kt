package uk.gov.justice.digital.hmpps.api.model.sentence

import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.overview.Rar
import java.time.LocalDate

data class Requirement(
    val id: Long,
    val code: String,
    val expectedStartDate: LocalDate?,
    val actualStartDate: LocalDate,
    val expectedEndDate: LocalDate?,
    val actualEndDate: LocalDate?,
    val terminationReason: String?,
    val description: String,
    val length: Long?,
    val lengthUnitValue: String?,
    val requirementNotes: List<NoteDetail>? = null,
    val requirementNote: NoteDetail? = null,
    val rar: Rar? = null
)

data class MinimalRequirement(
    val id: Long,
    val description: String
)

data class RequirementNoteDetail(
    val personSummary: PersonSummary,
    val requirement: Requirement? = null
)
