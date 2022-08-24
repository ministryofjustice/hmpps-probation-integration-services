package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReasonCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataSet
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode

object ReferenceDataGenerator {
    val RELEASE_TYPE =
        ReleaseTypeCode.values().associateWith { generate(it.code, ReferenceDataSetGenerator.RELEASE_TYPE) }

    val RECALL_REASON =
        RecallReasonCode.values().associateWith { RecallReason(IdGenerator.getAndIncrement(), it.code) }

    val CONTACT_TYPE =
        ContactTypeCode.values().associateWith { ContactType(IdGenerator.getAndIncrement(), it.code) }

    val CUSTODIAL_STATUS =
        CustodialStatusCode.values().associateWith { generate(it.code, ReferenceDataSetGenerator.CUSTODIAL_STATUS) }

    val CUSTODY_EVENT_TYPE =
        CustodyEventTypeCode.values().associateWith { generate(it.code, ReferenceDataSetGenerator.CUSTODY_EVENT_TYPE) }

    fun generate(
        code: String,
        dataset: ReferenceDataSet,
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(id, code, "description of $code", selectable, dataset)
}

object ReferenceDataSetGenerator {
    val RELEASE_TYPE = generate("RELEASE TYPE")
    val CUSTODIAL_STATUS = generate("THROUGHCARE STATUS")
    val CUSTODY_EVENT_TYPE = generate("CUSTODY EVENT TYPE")

    fun generate(name: String, id: Long = IdGenerator.getAndIncrement()) = ReferenceDataSet(id, name)
}
