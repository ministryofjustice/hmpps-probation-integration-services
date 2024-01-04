package uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.KeyDate
import java.lang.System.lineSeparator

@Service
class ContactService(
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository
) {
    fun createForKeyDateChanges(custody: Custody, updates: List<KeyDate>) {
        if (updates.isEmpty()) return
        val event = custody.disposal?.event!!
        val om = event.manager!!
        val contact = Contact(
            personId = event.person.id,
            eventId = event.id,
            type = contactTypeRepository.edssType(),
            notes = updates.joinToString(lineSeparator()) {
                "${it.type.description} ${DeliusDateFormatter.format(it.date)}"
            },
            staffId = om.staffId,
            teamId = om.teamId,
            providerId = event.manager.providerId
        )

        contactRepository.save(contact)
    }
}
