package uk.gov.justice.digital.hmpps.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.ZonedDateTime

data class SentenceProgress(
    val sentences: List<Sentence>
) {
    data class Sentence(
        val type: String,
        val startDate: LocalDate,
        val expectedEndDate: LocalDate?,
        val lastUpdatedAt: ZonedDateTime,
        val requirements: List<Requirement>,
        val licenceConditions: List<LicenceCondition>,
        val mainOffence: CodeDescription,
        val additionalOffences: List<CodeDescription>,
    )

    data class Requirement(
        @Deprecated("Use mainCategory", replaceWith = ReplaceWith("mainCategory.description"))
        val type: String,
        @Deprecated("Use subCategory", replaceWith = ReplaceWith("subCategory.description"))
        val description: String?,
        val mainCategory: CodeDescription,
        val subCategory: CodeDescription? = null,
        @Schema(description = "The length of the requirement. Note: for unpaid work requirements, this does not currently include any adjustments.")
        val required: Int? = null,
        @Schema(description = "The progress made against the requirement. For rehabilitative activity requirements (RAR), this is the number of RAR days attended. For unpaid work requirements, this is the hours completed. For other requirement types, this is null.")
        val completed: Int? = null,
        val unit: DurationUnit? = null,
        val imposedDate: LocalDate? = null,
        val expectedStartDate: LocalDate? = null,
        val expectedEndDate: LocalDate? = null,
        val actualStartDate: LocalDate? = null,
        val actualEndDate: LocalDate? = null,
        val lastUpdatedAt: ZonedDateTime,
    )

    data class LicenceCondition(
        @Deprecated("Use mainCategory", replaceWith = ReplaceWith("mainCategory.description"))
        val type: String,
        @Deprecated("Use subCategory", replaceWith = ReplaceWith("subCategory.description"))
        val description: String?,
        val mainCategory: CodeDescription,
        val subCategory: CodeDescription? = null,
        val startDate: LocalDate,
        val expectedEndDate: LocalDate? = null,
    )
}