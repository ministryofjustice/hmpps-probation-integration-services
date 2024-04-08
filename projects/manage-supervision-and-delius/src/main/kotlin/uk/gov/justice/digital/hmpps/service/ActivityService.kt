package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.activity.Activity
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivity
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getSummary

@Service
class ActivityService(
    private val personRepository: PersonRepository,
    private val contactRepository: ContactRepository
) {

    @Transactional
    fun getPersonActivity(crn: String): PersonActivity {
        val summary = personRepository.getSummary(crn)
        val contacts = contactRepository.findByPersonId(summary.id)

        return PersonActivity(
            personSummary = summary.toPersonSummary(),
            activities = contacts.map { it.toActivity() }
        )
    }

    @Transactional
    fun getPersonSentenceActivity(personId: Long, eventId: List<Long>): List<Activity> {
        return contactRepository.findByPersonIdAndEventIdIn(personId, eventId).map { it.toActivity() }
    }
}

