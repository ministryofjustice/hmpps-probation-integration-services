package uk.gov.justice.digital.hmpps.appointments.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.appointments.domain.person.Person
import uk.gov.justice.digital.hmpps.appointments.domain.person.PersonRepository
import uk.gov.justice.digital.hmpps.appointments.domain.person.getPerson
import uk.gov.justice.digital.hmpps.appointments.domain.event.Event
import uk.gov.justice.digital.hmpps.appointments.domain.event.EventRepository
import uk.gov.justice.digital.hmpps.appointments.domain.event.component.Requirement
import uk.gov.justice.digital.hmpps.appointments.domain.event.component.RequirementRepository
import uk.gov.justice.digital.hmpps.appointments.model.ReferencedEntities

@Transactional
@Service
class RelatedToService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val requirementRepository: RequirementRepository,
) {
    fun findRelatedTo(relatedTo: ReferencedEntities): RelatedTo {
        val person = personRepository.getPerson(relatedTo.person.id)
        val event = relatedTo.event?.id?.let { eventRepository.findByIdOrNull(it) }
        val requirement = relatedTo.requirement?.id?.let { requirementRepository.findByIdOrNull(it) }
        return RelatedTo(person, event, requirement)
    }
}

class RelatedTo(
    val person: Person,
    val event: Event?,
    val requirement: Requirement?,
)