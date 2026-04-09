package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ContactType

object ContactTypeGenerator {
    val CT_ESPCHI = generateContactType(ContactType.E_SUPERVISION_CHECK_IN)
    val CT_ESPCHS = generateContactType(ContactType.E_SUPERVISION_SETUP_COMPLETED)

    fun generateContactType(code: String, id: Long = IdGenerator.getAndIncrement()) = ContactType(id, code)
}

