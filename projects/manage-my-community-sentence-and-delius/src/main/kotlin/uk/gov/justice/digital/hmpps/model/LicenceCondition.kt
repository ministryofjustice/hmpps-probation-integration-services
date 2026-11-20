package uk.gov.justice.digital.hmpps.model

data class LicenceCondition(
    val id: Long,
    val mainCategory: CodeDescription,
    val subCategory: CodeDescription?,
)
