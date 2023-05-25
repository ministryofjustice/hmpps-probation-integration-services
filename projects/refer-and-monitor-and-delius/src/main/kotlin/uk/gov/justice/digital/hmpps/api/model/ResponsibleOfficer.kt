package uk.gov.justice.digital.hmpps.api.model

data class ResponsibleOfficer(
    val communityManager: Manager,
    val prisonManager: Manager?
)

data class Manager(
    val code: String,
    val name: Name,
    val username: String?,
    val email: String?,
    val responsibleOfficer: Boolean,
    val pdu: Pdu
)

data class Name(val forename: String, val surname: String)
data class Pdu(val code: String, val description: String)
