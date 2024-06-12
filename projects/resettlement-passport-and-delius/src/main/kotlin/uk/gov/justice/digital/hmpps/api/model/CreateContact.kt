package uk.gov.justice.digital.hmpps.api.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.time.ZonedDateTime

data class CreateContact(
    @Schema(
        type = "string",
        example = "IMMEDIATE_NEEDS_REPORT",
        description = "Nomis contact type. Must either IMMEDIATE_NEEDS_REPORT or PRE_RELEASE_REPORT"
    )
    val type: CaseNoteType,
    val description: String?,
    val dateTime: ZonedDateTime,
    @field:NotBlank
    val notes: String,

    @NotBlank
    val author: Author
) {
    enum class CaseNoteType(val code: String) {
        IMMEDIATE_NEEDS_REPORT("RP9"),
        PRE_RELEASE_REPORT("RP10"),
    }
}

data class Author(
    @field:NotBlank
    val prisonCode: String,

    @field:NotBlank
    val forename: String,

    @field:NotBlank
    val surname: String
)