package uk.gov.justice.digital.hmpps.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class MappaDetail(
    @Schema(
        example = "1",
        allowableValues = ["0,1,2,3"],
        description = "MAPPA Level (0=unknown). If the level is unknown due to an unrecognised Delius MAPPA level code (e.g. one not used anymore) the original Delius level description will still be returned"
    )
    val level: Int? = null,
    @Schema(description = "MAPPA Level Description", example = "MAPPA Level 1")
    val levelDescription: String? = null,

    @Schema(
        example = "3",
        allowableValues = ["0,1,2,3"],
        description = "MAPPA Category (0 = unknown). If the category is unknown due to an unrecognised Delius MAPPA category code (e.g. one not used anymore) the original Delius category  description will still be returned"
    )
    val category: Int? = null,

    @Schema(description = "MAPPA Category Description", example = "MAPPA Cat 1")
    val categoryDescription: String? = null,

    @Schema(description = "Start date", example = "2021-01-27")
    val startDate: LocalDate? = null,

    @Schema(description = "Next review date", example = "2021-04-27")
    val reviewDate: LocalDate? = null,

    @Schema(description = "Team")
    val team: KeyValue? = null,

    @Schema(description = "Officer")
    val officer: StaffHuman? = null,

    @Schema(description = "Probation area")
    val probationArea: KeyValue? = null,

    @Schema(description = "Notes")
    private val notes: String? = null
)

data class KeyValue(
    val code: String? = null,
    val description: String
)

data class StaffHuman(
    @Schema(description = "Staff code", example = "AN001A")
    val code: String? = null,
    @Schema(description = "Given names", example = "Sheila Linda")
    val forenames: String? = null,
    @Schema(description = "Family name", example = "Hancock")
    val surname: String? = null,
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val isUnallocated: Boolean = code?.endsWith("U") ?: false
)