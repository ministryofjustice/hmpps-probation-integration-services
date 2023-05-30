package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReason
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonCode

object TransferReasonGenerator {
    val CASE_ORDER = TransferReason(IdGenerator.getAndIncrement(), TransferReasonCode.CASE_ORDER.value)
    val COMPONENT = TransferReason(IdGenerator.getAndIncrement(), TransferReasonCode.COMPONENT.value)
}
