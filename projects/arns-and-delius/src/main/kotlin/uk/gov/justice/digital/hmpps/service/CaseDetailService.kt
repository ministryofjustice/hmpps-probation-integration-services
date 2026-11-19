package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.Event
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.toResponse
import uk.gov.justice.digital.hmpps.model.AdditionalSentences
import uk.gov.justice.digital.hmpps.model.ArnsResponse
import uk.gov.justice.digital.hmpps.model.OffenceDetail
import uk.gov.justice.digital.hmpps.repository.AdditionalSentenceRepository
import uk.gov.justice.digital.hmpps.repository.EventRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.repository.getByCrn
import java.time.ZonedDateTime

@Service
class CaseDetailService(
    private val personRepository: PersonRepository,
    private val additionalSentenceRepository: AdditionalSentenceRepository,
    private val eventRepository: EventRepository,
) {
    fun getCaseDetail(crn: String): ArnsResponse = personRepository.getByCrn(crn).toModel()

    fun getAdditionalSentences(crn: String): AdditionalSentences {
        val events = eventRepository.findAllByPerson_CrnAndActiveFlagEqualsTrue(crn)
        .map { it.id }
        val additionalSentences = additionalSentenceRepository.findAllByEvent_IdIn(events)
        return AdditionalSentences(
            additionalSentences = additionalSentences.map { it.toResponse() }
        )
    }
}

private fun Person.toModel() = ArnsResponse(
    crn = crn,
    dateOfBirth = dateOfBirth,
    gender = gender.description,
    offences = events.map(Event::toModel),
)

private fun Event.toModel() = OffenceDetail(
    mainOffence = mainOffence?.offence?.description,
    sentenceStartDate = disposal?.startDate,
    convictionDate = convictionDate,
)