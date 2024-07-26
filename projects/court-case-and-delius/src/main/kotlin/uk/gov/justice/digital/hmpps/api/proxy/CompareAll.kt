package uk.gov.justice.digital.hmpps.api.proxy

data class CompareAll(
    val crns: List<String>?,
    val uriConfig: Map<String, Map<String, Any>>,
    val pageNumber: Int = 1,
    val pageSize: Int = 10
)
