package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class PersonDetail(
    val identifiers: Identifiers,
    val name: Name,
    val dateOfBirth: LocalDate,
    val title: CodeDescription?,
    val gender: CodeDescription?,
    val nationality: CodeDescription?,
    val ethnicity: CodeDescription?,
    val ethnicityDescription: String?,
    val contactDetails: ContactDetails?,
    val aliases: List<Alias>,
    val addresses: List<Address>,
    val excludedFrom: LimitedAccess?,
    val restrictedTo: LimitedAccess?,
)

data class Identifiers(
    val deliusId: Long,
    val crn: String,
    val nomsId: String?,
    val prisonerNumber: String?,
    val pnc: String?,
    val cro: String?,
    val ni: String?
)

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String,
    val previousSurname: String? = null,
    val preferred: String? = null
)

data class ContactDetails(val telephone: String?, val mobile: String?, val email: String?) {
    companion object {
        fun of(telephone: String?, mobile: String?, email: String?): ContactDetails? =
            if (telephone == null && mobile == null && email == null) {
                null
            } else {
                ContactDetails(telephone, mobile, email)
            }
    }
}

data class CodeDescription(val code: String, val description: String)

data class Alias(val name: Name, val dateOfBirth: LocalDate)

data class Address(
    val fullAddress: String,
    val postcode: String,
    val noFixedAbode: Boolean,
    val status: CodeDescription,
    val startDate: LocalDate,
    val endDate: LocalDate?,
)