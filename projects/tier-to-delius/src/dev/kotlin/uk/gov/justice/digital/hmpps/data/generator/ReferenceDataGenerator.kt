package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataSetGenerator.NSI_OUTCOME
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataSetGenerator.RSR_TYPE
import uk.gov.justice.digital.hmpps.integrations.delius.nsi.EnforcementActivityCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataSet

object ReferenceDataGenerator {

    val GENDER_MALE = generate("MALE", ReferenceDataSetGenerator.GENDER)
    val TIER_ONE = generate("T1", ReferenceDataSetGenerator.TIER)
    val LEVEL_ONE = generate("L1", ReferenceDataSetGenerator.REGISTER_LEVEL)
    val FLAG = generate("F1", ReferenceDataSetGenerator.REGISTER_TYPE_FLAG)
    val ENFORCEMENT_OUTCOMES = EnforcementActivityCode.stringValues.map { generate(it, NSI_OUTCOME) }
    val STATIC_RSR = generate("S", RSR_TYPE)
    val DYNAMIC_RSR = generate("D", RSR_TYPE)

    fun generate(
        code: String,
        dataset: ReferenceDataSet,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(id, code, "description of $code", dataset)
}

object ReferenceDataSetGenerator {
    val TIER = generate("TIER")
    val TIER_CHANGE_REASON = generate("TIER CHANGE REASON")
    val GENDER = generate("GENDER")
    val REGISTER_LEVEL = generate("REGISTER LEVEL")
    val REGISTER_TYPE_FLAG = generate("REGISTER TYPE FLAG")
    val NSI_OUTCOME = generate("NSI OUTCOME")
    val RSR_TYPE = generate("RSR SCORE CHANGE")

    fun generate(name: String, id: Long = IdGenerator.getAndIncrement()) = ReferenceDataSet(id, name)
}
