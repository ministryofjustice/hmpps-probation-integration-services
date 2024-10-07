package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.api.model.conviction.CourtAppearanceBasic
import uk.gov.justice.digital.hmpps.api.model.conviction.CourtAppearanceBasicWrapper
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.getByPersonAndEventNumber
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson

@Service
class CourtAppearanceService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val courtAppearanceRepository: CourtAppearanceRepository
) {

    fun getCourtAppearancesFor(crn: String, eventId: Long): CourtAppearanceBasicWrapper {
        val person = personRepository.getPerson(crn)
        val event = eventRepository.getByPersonAndEventNumber(person, eventId)
        val courtAppearances = courtAppearanceRepository.findByPersonIdAndEventId(person.id, event.id)
            .sortedByDescending { it.appearanceDate }
            .map { it.toCourtAppearance() }
        return CourtAppearanceBasicWrapper(courtAppearances)
    }
}

fun CourtAppearance.toCourtAppearance() = CourtAppearanceBasic(
    courtAppearanceId = id,
    appearanceDate = appearanceDate,
    courtCode = court.code,
    courtName = court.courtName,
    appearanceType = KeyValue(appearanceType.code, appearanceType.description),
    crn = person.crn
)
