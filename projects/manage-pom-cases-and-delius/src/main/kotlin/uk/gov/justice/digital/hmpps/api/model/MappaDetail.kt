package uk.gov.justice.digital.hmpps.api.model

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
    val startDate: LocalDate,

    @Schema(description = "Next review date", example = "2021-04-27")
    val reviewDate: LocalDate? = null,
)
