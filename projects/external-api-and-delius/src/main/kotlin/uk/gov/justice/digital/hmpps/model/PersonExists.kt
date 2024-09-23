package uk.gov.justice.digital.hmpps.model

data class PersonExists(val crn: String, val existsInDelius: Boolean)
