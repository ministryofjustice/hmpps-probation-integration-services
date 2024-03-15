package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.PersonSummary

data class AddressOverview (
    val personSummary: PersonSummary,
    val mainAddress: Address?,
    val otherAddresses: List<Address>,
    val previousAddresses: List<Address>
)
