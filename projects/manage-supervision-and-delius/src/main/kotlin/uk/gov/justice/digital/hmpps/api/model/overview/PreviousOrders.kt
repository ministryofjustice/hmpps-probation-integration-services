package uk.gov.justice.digital.hmpps.api.model.overview

data class PreviousOrders(
    val breaches: Int,
    val count: Int,
    val orders: List<Order> = emptyList()
)