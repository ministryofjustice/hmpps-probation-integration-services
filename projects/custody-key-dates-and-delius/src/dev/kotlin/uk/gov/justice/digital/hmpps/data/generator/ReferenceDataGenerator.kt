package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.DatasetCode
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceData

object ReferenceDataGenerator {
    val DS_CUSTODY_STATUS = Dataset(IdGenerator.getAndIncrement(), DatasetCode.CUSTODY_STATUS)
    val DS_KEY_DATE_TYPE = Dataset(IdGenerator.getAndIncrement(), DatasetCode.KEY_DATE_TYPE)
    val KEY_DATE_TYPES = CustodyDateType.entries.map { generateKeyDateType(it.code) }.associateBy { it.code }

    val DEFAULT_CUSTODY_STATUS = generateCustodyStatus("D")

    fun generateCustodyStatus(code: String) =
        ReferenceData(IdGenerator.getAndIncrement(), code, code, DS_CUSTODY_STATUS)

    fun generateKeyDateType(code: String) =
        ReferenceData(IdGenerator.getAndIncrement(), code, code, DS_KEY_DATE_TYPE)
}
