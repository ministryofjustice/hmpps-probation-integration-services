package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataSet
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.TransferStatusCode

object ReferenceDataGenerator {
    val CUSTODIAL_STATUS =
        CustodialStatusCode.entries.associateWith { generate(it.code, ReferenceDataSetGenerator.CUSTODIAL_STATUS) }

    val CUSTODY_EVENT_TYPE =
        CustodyEventTypeCode.entries.associateWith { generate(it.code, ReferenceDataSetGenerator.CUSTODY_EVENT_TYPE) }

    val TRANSFER_STATUS =
        TransferStatusCode.entries.associateWith { generate(it.code, ReferenceDataSetGenerator.TRANSFER_STATUS) }

    val LICENCE_CONDITION_TERMINATION_REASON =
        generate("TEST", ReferenceDataSetGenerator.generate("TERMINATION REASON"))

    val PERSON_MANAGER_ALLOCATION_REASON =
        generate("TEST", ReferenceDataSetGenerator.generate("ALLOCATION REASON"))

    val PRISON_MANAGER_ALLOCATION_REASON =
        generate("AUT", ReferenceDataSetGenerator.generate("POM ALLOCATION REASON"))

    val RELEASE_TYPE =
        ReleaseTypeCode.entries.associateWith { generate(it.code, ReferenceDataSetGenerator.RELEASE_TYPE) }

    val RECALL_REASON =
        RecallReason.Code.entries.associateWith { RecallReasonGenerator.generate(it.value) }

    val CONTACT_TYPE =
        ContactType.Code.entries.associateWith { ContactType(IdGenerator.getAndIncrement(), it.value) }

    val ACR_DATE_TYPE = generate("ACR", ReferenceDataSetGenerator.KEY_DATE_TYPE)

    fun generate(
        code: String,
        dataset: ReferenceDataSet,
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(id, code, "description of $code", dataset, selectable)
}

object ReferenceDataSetGenerator {
    val RELEASE_TYPE = generate("RELEASE TYPE")
    val CUSTODIAL_STATUS = generate("THROUGHCARE STATUS")
    val CUSTODY_EVENT_TYPE = generate("CUSTODY EVENT TYPE")
    val TRANSFER_STATUS = generate("TRANSFER STATUS")
    val KEY_DATE_TYPE = generate("THROUGHCARE DATE TYPE")

    fun generate(name: String, id: Long = IdGenerator.getAndIncrement()) = ReferenceDataSet(id, name)
}
