package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.api.model.Nsi
import uk.gov.justice.digital.hmpps.api.model.NsiDetails
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.getByPersonAndEventNumber
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.Nsi as NsiEntity
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson

@Service
class InterventionService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val nsiRepository: NsiRepository,
) {

    fun getNsiByCodes(crn: String, convictionId: Long, nsiCodes: List<String>): NsiDetails {
        val person = personRepository.getPerson(crn)
        val event = eventRepository.getByPersonAndEventNumber(person, convictionId)


        return NsiDetails(
            nsiRepository
                .findByPersonIdAndEventIdAndTypeCodeIn(person.id, event.id, nsiCodes)
            .map { it.toNsi() })
    }

    fun NsiEntity.toNsi(): Nsi =
        Nsi (
            id,
            KeyValue(type.code, type.description),
            subType?.let { KeyValue(it.code, it.description) },
            outcome?.let{ KeyValue(it.code, it.description) },
            requirement?.toRequirementModel(),
            KeyValue(nsiStatus.code, nsiStatus.description),
            statusDate,
            actualStartDate,
            expectedStartDate,
            actualEndDate,
            expectedEndDate
        )
}