package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.ContactType
import uk.gov.justice.digital.hmpps.entity.ContactType.Companion.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.entity.ContactType.Companion.REFERRAL_UPDATED
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.ADD_CONTACT
import java.time.ZonedDateTime

object EntityGenerator {
    val PERSON = Person(id(), "A000001")
    val MANAGER = PersonManager(id(), PERSON.id, 1, 2, 3)
    val CONTACT_TYPES = arrayOf(
        ContactType(id(), REFERRAL_SUBMITTED),
        ContactType(id(), REFERRAL_UPDATED)
    )
    val BUSINESS_INTERACTION = BusinessInteraction(id(), ADD_CONTACT.code, ZonedDateTime.now())
}
