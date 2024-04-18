package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class CaseDetails(
    val identifiers: Identifiers,
    val person: Person,
    val profile: Profile?,
    val contactDetails: ContactDetails?
)

data class Identifiers(val crn: String, val noms: String?, val pnc: String?, val cro: String?)
data class Person(val name: Name, val dateOfBirth: LocalDate, val gender: CodeDescription?)
data class Name(val surname: String, val forename: String, val middleNames: List<String>)
data class Profile(
    val language: CodeDescription?,
    val ethnicity: CodeDescription?,
    val religion: CodeDescription?
) {
    companion object {
        fun from(
            language: CodeDescription?,
            ethnicity: CodeDescription?,
            religion: CodeDescription?
        ): Profile? =
            if (language == null && ethnicity == null && religion == null) {
                null
            } else {
                Profile(language, ethnicity, religion)
            }
    }
}

data class ContactDetails(
    val mainAddress: Address?,
    val emailAddress: String?,
    val telephoneNumber: String?,
    val mobileNumber: String?
) {
    companion object {
        fun from(
            mainAddress: Address? = null,
            emailAddress: String? = null,
            telephoneNumber: String? = null,
            mobileNumber: String? = null
        ): ContactDetails? {
            return if (mainAddress == null && emailAddress.isNullOrBlank() && telephoneNumber.isNullOrBlank() && mobileNumber.isNullOrBlank()) {
                null
            } else {
                ContactDetails(mainAddress, emailAddress, telephoneNumber, mobileNumber)
            }
        }
    }
}

data class Address(
    val noFixedAbode: Boolean,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?
) {
    companion object {
        fun from(
            noFixedAbode: Boolean = false,
            buildingName: String? = null,
            buildingNumber: String? = null,
            streetName: String? = null,
            district: String? = null,
            town: String? = null,
            county: String? = null,
            postcode: String? = null
        ): Address? =
            if (
                listOf(buildingName, buildingNumber, streetName, district, town, county, postcode)
                    .all(String?::isNullOrBlank) && !noFixedAbode
            ) {
                null
            } else {
                Address(noFixedAbode, buildingName, buildingNumber, streetName, district, town, county, postcode)
            }
    }
}