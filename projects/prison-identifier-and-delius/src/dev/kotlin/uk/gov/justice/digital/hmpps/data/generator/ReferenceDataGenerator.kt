package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ContactType
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.ReferenceDataSet

object ReferenceDataGenerator {
    val GENDER_SET = generateReferenceDataSet("GENDER")
    val MALE = generateGender("M")

    val CUSTODY_STATUS_SET = generateReferenceDataSet("THROUGHCARE STATUS")
    val CUSTODY_STATUS = generateCustodyStatus("A")

    val ADDITIONAL_IDENTIFIER_TYPE_SET = generateReferenceDataSet("ADDITIONAL IDENTIFIER TYPE")
    val DUPLICATE_NOMS = generateIdentifierType("DNOMS")
    val FORMER_NOMS = generateIdentifierType("XNOMS")

    val CONTACT_TYPE = generateContactType("EDSS")

    fun generateGender(code: String, id: Long = IdGenerator.getAndIncrement()) = ReferenceData(id, code, GENDER_SET)
    fun generateCustodyStatus(code: String, id: Long = IdGenerator.getAndIncrement()) =
        ReferenceData(id, code, CUSTODY_STATUS_SET)

    fun generateIdentifierType(code: String, id: Long = IdGenerator.getAndIncrement()) =
        ReferenceData(id, code, ADDITIONAL_IDENTIFIER_TYPE_SET)

    fun generateReferenceDataSet(name: String, id: Long = IdGenerator.getAndIncrement()) = ReferenceDataSet(id, name)

    fun generateContactType(code: String, id: Long = IdGenerator.getAndIncrement()) = ContactType(id, code)
}