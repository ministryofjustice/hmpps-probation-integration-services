package uk.gov.justice.digital.hmpps.model

data class Beneficiary(
    val name: String?,
    val contactName: String?,
    val emailAddress: String?,
    val website: String?,
    val telephoneNumber: String?,
    val location: ProjectAddress?,
)