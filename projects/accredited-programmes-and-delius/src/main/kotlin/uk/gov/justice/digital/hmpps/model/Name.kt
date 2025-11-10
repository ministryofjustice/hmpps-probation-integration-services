package uk.gov.justice.digital.hmpps.model

data class Name(
    val forename: String,
    val surname: String,
    val middleNames: String? = null
)
