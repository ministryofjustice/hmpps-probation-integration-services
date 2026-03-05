package uk.gov.justice.digital.hmpps.model

data class Manager(
    val name: Name,
    val telephoneNumber: String?,
    val team: Team,
)