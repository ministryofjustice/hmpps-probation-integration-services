package uk.gov.justice.digital.hmpps.api.proxy

data class CompareReport(
    val endPointName: String,
    val message: String,
    val url: String? = null,
    val testExecuted: Boolean? = false,
    val success: Boolean
)
