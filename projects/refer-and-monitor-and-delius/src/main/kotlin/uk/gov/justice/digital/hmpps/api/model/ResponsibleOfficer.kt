package uk.gov.justice.digital.hmpps.api.model

import com.fasterxml.jackson.annotation.JsonInclude

data class ResponsibleOfficer(
    val communityManager: Manager,
    val prisonManager: Manager?
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Manager(
    val code: String,
    val name: Name,
    val username: String?,
    val email: String?,
    val telephoneNumber: String?,
    val responsibleOfficer: Boolean,
    val pdu: Pdu,
    val team: Team,
    val officeLocations: List<OfficeLocation> = listOf()
) {
    val unallocated = code.endsWith("U")
}

data class Name(val forename: String, val surname: String)

data class Team(val code: String, val description: String, val email: String?, val telephoneNumber: String?)
data class Pdu(val code: String, val description: String)
