package uk.gov.justice.digital.hmpps.api.model.overview

import java.time.LocalDate

data class PreviousOrders(
    val breaches: Int,
    val count: Int,
    val lastEndedDate: LocalDate? = null,
    val orders: List<Order> = emptyList()
)