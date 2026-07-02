package uk.gov.justice.digital.hmpps.model

data class Team(
    val telephoneNumber: String?,
    val officeAddresses: List<OfficeAddress>,
)