package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData

object ReferenceDataGenerator {
    val TIER_2 = generate("2", "Tier 2")
    val DECISION_ENHANCED = generate("R")
    val DECISION_NORMAL = generate("A")
    val DECISION_NOT_ASSESSED = generate("N")
    val LEVEL_M1 = generate("M1")
    val LEVEL_M2 = generate("M2")
    val LEVEL_M3 = generate("M3")

    val ALL = listOf(DECISION_ENHANCED, DECISION_NORMAL, DECISION_NOT_ASSESSED, LEVEL_M1, LEVEL_M2, LEVEL_M3, TIER_2)

    fun generate(code: String, description: String = "Description of $code", id: Long = IdGenerator.getAndIncrement()) =
        ReferenceData(code, description, id)
}
