package uk.gov.justice.digital.hmpps.api.proxy

data class CompareReport(
    val endPointName: String,
    val message: String,
    val url: String? = null,
    val success: Boolean,
    val issues: List<String>? = null
)
