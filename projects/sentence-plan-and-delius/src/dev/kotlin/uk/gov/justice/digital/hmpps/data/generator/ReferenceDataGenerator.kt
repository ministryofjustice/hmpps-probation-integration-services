package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.service.ReferenceData

object ReferenceDataGenerator {
    val TIER_1 = generate("T1", "Tier 1")

    fun generate(code: String, description: String = "Description of $code", id: Long = IdGenerator.getAndIncrement()) =
        ReferenceData(code, id, description)
}
