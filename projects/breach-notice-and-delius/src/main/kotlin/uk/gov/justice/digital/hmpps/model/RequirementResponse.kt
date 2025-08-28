package uk.gov.justice.digital.hmpps.model

data class RequirementResponse(val requirements: List<Requirement>)
data class Requirement(val id: Long, val type: CodedDescription, val subType: CodedDescription?)