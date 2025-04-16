package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class Manager(
    val id: Long,
    val crn: String,
    val code: String,
    val name: Name,
    val provider: Provider,
    val team: Team,
    val username: String?,
    val email: String?,
    val telephoneNumber: String?,
    val unallocated: Boolean
)

data class Name(val forename: String, val middleName: String?, val surname: String)
data class Provider(val code: String, val description: String)
data class Team(
    val code: String, val description: String,
    val telephone: String?,
    val emailAddress: String?,
    val addresses: List<OfficeAddress>?,
    val district: District,
    val borough: Borough,
    val startDate: LocalDate,
    val endDate: LocalDate?
)

data class District(val code: String, val description: String, val borough: Borough)
data class Borough(val code: String, val description: String)
