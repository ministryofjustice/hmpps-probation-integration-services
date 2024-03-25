package uk.gov.justice.digital.hmpps.model

data class PrisonIdentifiers(
    val prisonerNumber: String,
    val bookingNumber: String? = null,
)