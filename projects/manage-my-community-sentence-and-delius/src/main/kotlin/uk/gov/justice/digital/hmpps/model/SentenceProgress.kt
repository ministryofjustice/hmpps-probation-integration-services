package uk.gov.justice.digital.hmpps.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class SentenceProgress(
    val sentences: List<Sentence>
) {
    data class Sentence(
        val type: String,
        val startDate: LocalDate,
        val expectedEndDate: LocalDate?,
        val requirements: List<Requirement>,
        val licenceConditions: List<LicenceCondition>,
    )

    data class Requirement(
        val type: String,
        val description: String?,
        @Schema(description = "The length of the requirement. Note: for unpaid work requirements, this does not currently include any adjustments.")
        val required: Int? = null,
        @Schema(description = "The progress made against the requirement. For rehabilitative activity requirements (RAR), this is the number of RAR days attended. For unpaid work requirements, this is the hours completed. For other requirement types, this is null.")
        val completed: Int? = null,
        val unit: DurationUnit? = null,
    )

    data class LicenceCondition(
        val type: String,
        val description: String?,
        val startDate: LocalDate,
        val expectedEndDate: LocalDate? = null,
    )
}