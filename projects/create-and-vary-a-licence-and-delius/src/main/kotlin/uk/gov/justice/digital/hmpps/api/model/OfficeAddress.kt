package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class OfficeAddress(
    val officeName: String,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
    val telephoneNumber: String?,
    val from: LocalDate,
    val to: LocalDate?
)
