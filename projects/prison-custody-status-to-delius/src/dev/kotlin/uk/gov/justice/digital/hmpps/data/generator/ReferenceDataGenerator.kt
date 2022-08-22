package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataSet
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.RecallReasonCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode

object ReferenceDataGenerator {
    val RELEASE_TYPE =
        ReleaseTypeCode.values().associateWith { generate(it.code, ReferenceDataSetGenerator.RELEASE_TYPE) }

    val CUSTODIAL_STATUS =
        CustodialStatusCode.values().associateWith { generate(it.code, ReferenceDataSetGenerator.CUSTODIAL_STATUS) }

    val RECALL_REASON =
        RecallReasonCode.values().associateWith { RecallReason(IdGenerator.getAndIncrement(), it.code) }

    fun generate(
        code: String,
        dataset: ReferenceDataSet,
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(id, code, selectable, dataset)
}

object ReferenceDataSetGenerator {
    val RELEASE_TYPE = generate("RELEASE TYPE")
    val CUSTODIAL_STATUS = generate("CUSTODIAL STATUS")

    fun generate(name: String, id: Long = IdGenerator.getAndIncrement()) = ReferenceDataSet(id, name)
}
