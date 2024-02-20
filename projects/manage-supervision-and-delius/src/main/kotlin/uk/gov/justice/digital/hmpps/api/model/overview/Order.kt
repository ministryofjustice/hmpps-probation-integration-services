package uk.gov.justice.digital.hmpps.api.model.overview

data class Order(
    val description: String,
    val endDate: String,
    val startDate: String
)