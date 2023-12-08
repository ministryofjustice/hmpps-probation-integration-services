package uk.gov.justice.digital.hmpps.api.model

data class ManagedCases(val managedCases: List<CaseIdentifier>)

data class CaseIdentifier(val crn: String, val nomsId: String? = null)
