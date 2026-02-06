package uk.gov.justice.digital.hmpps.model

data class CodedDescription(val code: String, val description: String)
data class SentenceType(val code: String, val description: String, val conditionBeingEnforced: String)