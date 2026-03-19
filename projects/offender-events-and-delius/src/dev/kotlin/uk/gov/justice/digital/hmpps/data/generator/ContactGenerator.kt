package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.integrations.delius.offender.Contact

object ContactGenerator {
    val DEFAULT = Contact(id = id())
    val DELETED = Contact(id = id(), softDeleted = true)
    val DELETED_VISOR = Contact(id = id(), visorContact = true, softDeleted = true)
    val VISOR = Contact(id = id(), visorContact = true)
}
