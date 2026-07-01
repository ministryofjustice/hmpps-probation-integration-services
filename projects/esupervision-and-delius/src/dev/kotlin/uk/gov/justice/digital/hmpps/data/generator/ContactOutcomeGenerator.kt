package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ContactOutcome

object ContactOutcomeGenerator {
    val COT_ESPC = generateContactOutcome(ContactOutcome.SETUP_COMPLETED)
    val COT_ESPRD = generateContactOutcome(ContactOutcome.SETUP_REMOVED)
    val COT_ESPMP = generateContactOutcome(ContactOutcome.MANUAL_STOP)
    val COT_ESPNA = generateContactOutcome(ContactOutcome.NO_ACTIVE_EVENTS)
    val COT_ESPRS = generateContactOutcome(ContactOutcome.IN_RESET)

    fun generateContactOutcome(code: String, id: Long = IdGenerator.getAndIncrement()) = ContactOutcome(id, code)
}