package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.document.Document

object DocumentGenerator {
    fun Contact.document() = Document(
        id = id(),
        primaryKeyId = id,
        tableName = "CONTACT",
    )
}
