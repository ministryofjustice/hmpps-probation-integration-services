package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class DefendantDetails(
    val crn: String?,
    val eventNumber: Int?,
    val name: Name?,
    val dateOfBirth: LocalDate?,
    val mainAddress: Address?,
)

data class Name(val forename: String?, val middleName: String?, val surname: String?)
data class Address(
    val buildingName: String?, val buildingNumber: String?, val streetName: String?, val district: String?,
    val town: String?, val county: String?, val postcode: String?, val noFixedAbode: Boolean?
)