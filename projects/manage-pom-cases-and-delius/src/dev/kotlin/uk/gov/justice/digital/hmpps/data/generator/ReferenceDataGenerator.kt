package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDate
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceDataSet

object ReferenceDataGenerator {
    val KEY_DATE_TYPE_DATASET = generateDataSet(ReferenceDataSet.Code.KEY_DATE_TYPE.value)
    val POM_ALLOCATION_DATASET = generateDataSet(ReferenceDataSet.Code.POM_ALLOCATION_REASON.value)
    val CUSTODY_STATUS_DATASET = generateDataSet(ReferenceDataSet.Code.CUSTODY_STATUS.value)
    val TIER_2 = generate("2", "Tier 2")
    val DECISION_ENHANCED = generate("R")
    val DECISION_NORMAL = generate("A")
    val DECISION_NOT_ASSESSED = generate("N")
    val LEVEL_M1 = generate("M1")
    val LEVEL_M2 = generate("M2")
    val LEVEL_M3 = generate("M3")
    val ALLOCATION_AUTO = generate("AUT", dataSetId = POM_ALLOCATION_DATASET.id)
    val ALLOCATION_INA = generate("INA", dataSetId = POM_ALLOCATION_DATASET.id)

    val TERMINATED_CUSTODY_STATUS = generate("P", dataSetId = CUSTODY_STATUS_DATASET.id)
    val DEFAULT_CUSTODY_STATUS = generate("D", dataSetId = CUSTODY_STATUS_DATASET.id)

    val KEY_DATE_HANDOVER_TYPE = generate(KeyDate.TypeCode.HANDOVER_DATE.value, dataSetId = KEY_DATE_TYPE_DATASET.id)
    val KEY_DATE_HANDOVER_START_DATE_TYPE =
        generate(KeyDate.TypeCode.HANDOVER_START_DATE.value, dataSetId = KEY_DATE_TYPE_DATASET.id)

    val ALL = listOf(
        DECISION_ENHANCED,
        DECISION_NORMAL,
        DECISION_NOT_ASSESSED,
        LEVEL_M1,
        LEVEL_M2,
        LEVEL_M3,
        TIER_2,
        KEY_DATE_HANDOVER_TYPE,
        KEY_DATE_HANDOVER_START_DATE_TYPE,
        ALLOCATION_AUTO,
        ALLOCATION_INA,
        TERMINATED_CUSTODY_STATUS,
        DEFAULT_CUSTODY_STATUS
    )

    fun generateDataSet(name: String, id: Long = IdGenerator.getAndIncrement()) = ReferenceDataSet(name, id)

    fun generate(
        code: String,
        description: String = "Description of $code",
        dataSetId: Long = 0,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(code, description, dataSetId, id)
}
