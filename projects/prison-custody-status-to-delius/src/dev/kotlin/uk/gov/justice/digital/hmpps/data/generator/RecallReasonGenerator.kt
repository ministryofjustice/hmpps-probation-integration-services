package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReason

object RecallReasonGenerator {
    fun generate(code: String) = RecallReason(
        id = IdGenerator.getAndIncrement(),
        code = code,
        description = "description of $code",
        licenceConditionTerminationReason = ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON
    )
}
