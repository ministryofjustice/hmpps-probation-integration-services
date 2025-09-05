package uk.gov.justice.digital.hmpps.model

data class PduOfficeLocations(val code: String, val description: String, val officeLocations: List<CodedValue>)