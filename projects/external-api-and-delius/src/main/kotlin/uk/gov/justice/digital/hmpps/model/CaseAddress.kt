package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class AddressWrapper(val contactDetails: ContactDetailAddresses)

data class ContactDetailAddresses(val addresses: List<CaseAddress> = listOf())

data class CaseAddress(
    val noFixedAbode: Boolean,
    val type: CodedValue,
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val town: String?,
    val district: String?,
    val county: String?,
    val postcode: String?,
    val from: LocalDate?,
    val to: LocalDate?,
    val notes: String?,
)