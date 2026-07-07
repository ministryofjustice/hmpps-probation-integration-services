package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class BasicDetails(
    val title: String?,
    val name: Name,
    val dateOfBirth: LocalDate,
    val nationalInsuranceNumber: String?,
    val telephoneNumber: String?,
    val mobileNumber: String?,
    val emailAddress: String?,
    val lastHomeVisitDate: LocalDate?,
    val addresses: List<AddressDetail>,
    val employers: List<Employer>,
)

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String,
)

data class AddressDetail(
    val id: Long,
    val status: String,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val townCity: String?,
    val district: String?,
    val county: String?,
    val postcode: String?,
)

data class Employer(
    val employerName: Name,
    val employerAddress: EmployerAddress?,
    val telephoneNumber: String?,
    val mobileNumber: String?,
)

data class EmployerAddress(
    val id: Long,
    val status: String?,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val townCity: String?,
    val district: String?,
    val county: String?,
    val postcode: String?,
)
