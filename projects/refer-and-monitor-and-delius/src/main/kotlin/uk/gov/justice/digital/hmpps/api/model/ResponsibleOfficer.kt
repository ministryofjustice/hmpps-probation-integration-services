package uk.gov.justice.digital.hmpps.api.model

data class ResponsibleOfficer(
    val communityManager: Manager
)

data class Manager(
    val code: String,
    val name: Name,
    val email: String?,
    val responsibleOfficer: Boolean
)

data class Name(val forename: String, val surname: String)
