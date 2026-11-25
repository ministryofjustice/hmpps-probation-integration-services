package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ContactOutcome

object ContactOutcomeGenerator {
    val COT_ESPC = generateContactOutcome(ContactOutcome.SETUP_COMPLETED)
    val COT_ESPRD = generateContactOutcome(ContactOutcome.SETUP_REMOVED)
    val COT_ESPMP = generateContactOutcome(ContactOutcome.MANUAL_STOP)
    val COT_ESPNA = generateContactOutcome(ContactOutcome.NO_ACTIVE_EVENTS)
    val COT_ESPRS = generateContactOutcome(ContactOutcome.IN_RESET)
    val COT_ESPSP = generateContactOutcome(ContactOutcome.NO_SUPERVISION_PACKAGE)
    val COT_ESPRC = generateContactOutcome(ContactOutcome.RECALLED)
    val COT_ESPME = generateContactOutcome(ContactOutcome.TIER_MISSING)
    val COT_ESPIT = generateContactOutcome(ContactOutcome.INELIGIBLE_TIER)
    val COT_ESPFT = generateContactOutcome(ContactOutcome.FINAL_THIRD)
    val COT_ESPNP = generateContactOutcome(ContactOutcome.NO_PRACTITIONER_ALLOCATED)
    val COT_ESPDI = generateContactOutcome(ContactOutcome.DEVICE_RESTRICTION)
    val COT_ESPYS = generateContactOutcome(ContactOutcome.YOUTH_SENTENCE)
    val COT_ESPEE = generateContactOutcome(ContactOutcome.EARLY_ENGAGEMENT)
    val COT_ESPAP = generateContactOutcome(ContactOutcome.ACCREDITED_PROGRAMME)

    fun generateContactOutcome(code: String, id: Long = IdGenerator.getAndIncrement()) = ContactOutcome(id, code)
}