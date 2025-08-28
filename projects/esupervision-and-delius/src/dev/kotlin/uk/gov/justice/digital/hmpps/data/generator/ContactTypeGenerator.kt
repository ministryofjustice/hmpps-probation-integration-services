package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.ContactType

object ContactTypeGenerator {
    val CT_ESPCHI = generateContactType(ContactType.E_SUPERVISION_CHECK_IN)
    fun generateContactType(code: String, id: Long = IdGenerator.getAndIncrement()) = ContactType(code, id)
}