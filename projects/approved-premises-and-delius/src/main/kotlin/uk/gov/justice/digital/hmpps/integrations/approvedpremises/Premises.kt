package uk.gov.justice.digital.hmpps.integrations.approvedpremises

data class Premises(
    val id: String,
    val name: String,
    val apCode: String,
    val legacyApCode: String,
)
