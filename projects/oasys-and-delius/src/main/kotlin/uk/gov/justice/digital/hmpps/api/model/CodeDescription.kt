package uk.gov.justice.digital.hmpps.api.model

data class CodeDescription(val code: String, val description: String)
data class Officer(val code: String, val forename: String, val surname: String) {
    val isUnallocated = code.endsWith("U", ignoreCase = true)
}