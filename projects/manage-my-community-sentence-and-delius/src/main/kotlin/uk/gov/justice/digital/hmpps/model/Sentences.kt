package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class Sentences(
    val sentences: List<Sentence>
) {
    data class Sentence(
        val type: String,
        val startDate: LocalDate,
        val expectedEndDate: LocalDate?,
        val requirements: List<Condition>,
        val licenceConditions: List<Condition>,
    )

    data class Condition(
        val type: String,
        val description: String?,
        val length: String? = null,
    )
}