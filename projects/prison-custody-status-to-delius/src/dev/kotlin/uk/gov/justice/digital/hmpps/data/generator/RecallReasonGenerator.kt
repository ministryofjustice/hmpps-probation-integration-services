package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReason

object RecallReasonGenerator {
    fun generate(code: String) = RecallReason(
        id = IdGenerator.getAndIncrement(),
        code = code,
        description = "description of $code",
        licenceConditionTerminationReason = ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON
    )
}
