package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.getPerson
import java.time.ZonedDateTime

@Service
class RecommendationStarted(
    private val personRepository: PersonRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository
) {

    fun recommended(crn: String, recommendationUrl: String, occurredAt: ZonedDateTime) {
        val person = personRepository.getPerson(crn)
        contactRepository.save(person.recommendationStarted(recommendationUrl, occurredAt))
    }

    private fun Person.recommendationStarted(recommendationUrl: String, occurredAt: ZonedDateTime): Contact {
        checkNotNull(manager) { "No Active Person Manager" }
        return Contact(
            0,
            id,
            occurredAt,
            occurredAt,
            type = contactTypeRepository.getByCode(ContactType.RECOMMENDATION_STARTED),
            notes = "View details of this Recommendation: $recommendationUrl",
            providerId = manager.providerId,
            teamId = manager.teamId,
            staffId = manager.staffId
        )
    }
}
