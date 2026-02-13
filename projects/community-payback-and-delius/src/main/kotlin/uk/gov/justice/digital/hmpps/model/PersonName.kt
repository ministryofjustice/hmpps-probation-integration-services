package uk.gov.justice.digital.hmpps.model

data class PersonName(
    val forename: String,
    val middleNames: List<String>,
    val surname: String
)

data class StaffName(
    val forename: String,
    val middleName: String?,
    val surname: String
)