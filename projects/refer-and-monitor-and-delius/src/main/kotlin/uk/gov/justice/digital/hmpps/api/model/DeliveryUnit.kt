package uk.gov.justice.digital.hmpps.api.model

data class DeliveryUnit(val code: String, val description: String, val region: Region)

data class Region(val code: String, val description: String)
