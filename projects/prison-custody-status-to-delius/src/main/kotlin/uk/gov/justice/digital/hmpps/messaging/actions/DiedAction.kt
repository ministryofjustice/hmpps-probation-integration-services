package uk.gov.justice.digital.hmpps.messaging.actions

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactDetail
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.messaging.ActionResult
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementAction
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementContext
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties

@Component
class DiedAction(
    private val personRepository: PersonRepository,
    private val contactService: ContactService
) : PrisonerMovementAction {
    override val name = "Died"

    override fun accept(context: PrisonerMovementContext): ActionResult {
        val people = personRepository.findByNomsNumberAndSoftDeletedIsFalse(context.prisonerMovement.nomsId)
        val person = when (people.size) {
            1 -> people[0]
            0 -> throw IgnorableMessageException("MissingNomsNumber")
            else -> throw IgnorableMessageException("DuplicateNomsNumber")
        }
        val notes = "This information has been provided via a movement reason recorded at ${
        DeliusDateTimeFormatter.format(context.prisonerMovement.occurredAt)
        } in NOMIS"
        contactService.createContact(
            ContactDetail(ContactType.Code.DIED_IN_CUSTODY, context.prisonerMovement.occurredAt, notes, alert = true),
            person
        )
        return ActionResult.Success(ActionResult.Type.Died, context.prisonerMovement.telemetryProperties())
    }
}
