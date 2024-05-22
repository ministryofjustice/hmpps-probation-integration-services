package uk.gov.justice.digital.hmpps.api.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.time.ZonedDateTime

data class CreateContact(
    @Schema(
        type = "string",
        example = "RP9",
        description = "Nomis contact type. Must either RP9 or RP10"
    )
    val type: Type,
    val dateTime: ZonedDateTime,
    @field:NotBlank
    val notes: String,
) {
    enum class Type {
        RP9,
        RP10,
    }
}
