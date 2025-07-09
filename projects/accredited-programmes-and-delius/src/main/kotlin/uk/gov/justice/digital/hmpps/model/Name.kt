package uk.gov.justice.digital.hmpps.model

data class Name(
    val forename: String,
    val middleNames: String? = null,
    val surname: String
)
