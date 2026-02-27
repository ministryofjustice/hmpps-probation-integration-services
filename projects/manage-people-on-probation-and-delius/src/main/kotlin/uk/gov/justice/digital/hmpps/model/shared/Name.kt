package uk.gov.justice.digital.hmpps.model.shared

data class Name(
    val forename: String,
    val middleName: String? = null,
    val surname: String,
)