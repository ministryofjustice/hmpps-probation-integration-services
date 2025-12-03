package uk.gov.justice.digital.hmpps.model

data class ContactDetails(
    val crn: String,
    val name: Name,
    val mobile: String?,
    val email: String?,
    val practitioner: Practitioner,
)

data class Name(val forename: String, val surname: String)

data class CodedDescription(val code: String, val description: String)

data class Practitioner(
    val code: String,
    val name: Name,
    val localAdminUnit: CodedDescription,
    val probationDeliveryUnit: CodedDescription,
    val provider: CodedDescription,
    val email: String?,
)