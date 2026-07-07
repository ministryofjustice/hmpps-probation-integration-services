package uk.gov.justice.digital.hmpps.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class MappaInformation(
    @Schema(description = "Whether the person is subject to MAPPA procedures")
    val subjectOfMappaProcedures: Boolean,

    @Schema(description = "MAPPA registration details, null if no MAPPA registration exists")
    val mappaRegistration: MappaRegistration? = null,
)

data class MappaRegistration(
    @Schema(description = "Registration ID", example = "123456")
    val id: Long,

    @Schema(description = "Registration type details")
    val type: MappaType,

    @Schema(description = "Start date of the registration", example = "2025-01-01")
    val startDate: LocalDate,

    @Schema(description = "Notes associated with the registration")
    val notes: String? = null,
)

data class MappaType(
    @Schema(description = "Registration type code", example = "M1")
    val code: String,

    @Schema(description = "Registration type description", example = "MAPPA Level 1")
    val description: String,
)
