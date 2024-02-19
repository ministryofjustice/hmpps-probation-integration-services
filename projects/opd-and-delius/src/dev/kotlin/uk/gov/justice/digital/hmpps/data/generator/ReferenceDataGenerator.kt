package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.NsiSubType
import uk.gov.justice.digital.hmpps.integrations.delius.NsiType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType

object ReferenceDataGenerator {
    val DS_NSI_SUB_TYPE = generateDataset(Dataset.NSI_SUB_TYPE)
    val CONTACT_TYPES = ContactType.Code.entries.map { generateContactType(it.value) }.associateBy { it.code }
    val NSI_TYPES = NsiType.Code.entries.map { generateType(it.value) }.associateBy { it.code }
    val NSI_SUBTYPES = NsiSubType.Code.entries.map { generateSubType(it.value) }.associateBy { it.code }
    val NSI_STATUSES = NsiStatus.Code.entries.map { sc ->
        generateStatus(
            sc.value,
            sc.contactTypeCode?.value?.let { CONTACT_TYPES[it] }
        )
    }.associateBy { it.code }

    fun generateDataset(code: String, id: Long = IdGenerator.getAndIncrement()) = Dataset(code, id)
    fun generateContactType(code: String, id: Long = IdGenerator.getAndIncrement()) = ContactType(code, id)
    fun generateType(code: String, id: Long = IdGenerator.getAndIncrement()) = NsiType(code, id)
    fun generateSubType(code: String, id: Long = IdGenerator.getAndIncrement()) =
        NsiSubType(code, DS_NSI_SUB_TYPE.id, id)

    fun generateStatus(code: String, contactType: ContactType?, id: Long = IdGenerator.getAndIncrement()) =
        NsiStatus(code, contactType, id)

    fun all() =
        listOf(DS_NSI_SUB_TYPE) + CONTACT_TYPES.values + NSI_TYPES.values + NSI_SUBTYPES.values + NSI_STATUSES.values
}
