package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.Event
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.model.ArnsResponse
import uk.gov.justice.digital.hmpps.model.OffenceDetail
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.repository.getByCrn

@Service
class CaseDetailService(
    private val personRepository: PersonRepository,
) {
    fun getCaseDetail(crn: String): ArnsResponse = personRepository.getByCrn(crn).toModel()
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