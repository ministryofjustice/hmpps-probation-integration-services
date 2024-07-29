package uk.gov.justice.digital.hmpps.api.proxy

data class CompareAllReport(
    val totalNumberOfCrns: Int,
    val totalPages: Int,
    val currentPageNumber: Int,
    val totalNumberOfRequests: Int,
    val numberOfSuccessfulRequests: Int,
    val numberOfUnsuccessfulRequests: Int,
    val executionFailures: List<CompareReport>,
    val failureReports: List<CompareReport>,
)
