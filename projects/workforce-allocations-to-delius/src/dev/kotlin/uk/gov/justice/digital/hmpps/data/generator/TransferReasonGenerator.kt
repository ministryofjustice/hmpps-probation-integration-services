package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReason
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonCode

object TransferReasonGenerator {
    val CASE_ORDER = TransferReason(IdGenerator.getAndIncrement(), TransferReasonCode.CASE_ORDER.value)
}