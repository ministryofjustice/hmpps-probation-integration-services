package uk.gov.justice.digital.hmpps.api.model.sentence

import uk.gov.justice.digital.hmpps.api.model.Name

data class PreviousOrderHistory(
    val name: Name,
    val previousOrders: List<PreviousOrder> = emptyList()
)

data class PreviousOrderInformation(
    val name: Name,
    val title: String? = null,
    val sentence: Sentence? = null,
)