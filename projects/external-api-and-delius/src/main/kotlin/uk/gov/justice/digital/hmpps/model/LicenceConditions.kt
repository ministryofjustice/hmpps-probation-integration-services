package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class LicenceConditions(
    val sentences: List<Sentence>
) {
    data class Sentence(
        val eventNumber: Int,
        val licenceConditions: List<LicenceCondition>,
    )

    data class LicenceCondition(
        val mainCategory: CodedValue,
        val subCategory: CodedValue?,
        val createAndVaryALicenceCode: String?,
        val createdDate: LocalDate,
        val updatedDate: LocalDate,
    )
}
