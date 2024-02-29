package uk.gov.justice.digital.hmpps.client.approvedpremises.model

data class ApplicationStatus(
    val name: String,
    val label: String,
    val description: String,
    val statusDetails: List<ApplicationStatusDetail>,
)
data class ApplicationStatusDetail(
    val label: String,
    val name: String,
)