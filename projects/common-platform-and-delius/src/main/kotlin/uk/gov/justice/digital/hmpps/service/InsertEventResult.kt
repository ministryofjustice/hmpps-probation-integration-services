package uk.gov.justice.digital.hmpps.service

import uk.gov.justice.digital.hmpps.integrations.delius.entity.*

data class InsertEventResult(
    val event: Event,
    val mainOffence: MainOffence,
    val courtAppearance: CourtAppearance,
    val contact: Contact,
    val orderManager: OrderManager
)