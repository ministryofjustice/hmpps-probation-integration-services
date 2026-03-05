package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.Sentences
import uk.gov.justice.digital.hmpps.model.Sentences.Condition
import uk.gov.justice.digital.hmpps.model.Sentences.Sentence
import uk.gov.justice.digital.hmpps.repository.EventRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository

@Service
class SentenceService(
    private val eventRepository: EventRepository,
    private val personRepository: PersonRepository
) {
    fun getSentences(crn: String): Sentences {
        val personId = personRepository.findIdByCrn(crn) ?: throw NotFoundException("Person", "CRN", crn)
        return Sentences(
            sentences = eventRepository.findByPersonIdAndDisposalNotNull(personId).mapNotNull { it.disposal }.map {
                Sentence(
                    type = it.type.description,
                    startDate = it.date,
                    expectedEndDate = it.enteredExpectedEndDate ?: it.expectedEndDate,
                    requirements = it.requirements.map { requirement ->
                        Condition(
                            type = requirement.mainCategory.description,
                            description = requirement.subCategory?.description,
                            length = requirement.length?.let { length -> "$length ${requirement.mainCategory.lengthUnits.description}" }
                        )
                    },
                    licenceConditions = it.licenceConditions.map { licenceCondition ->
                        Condition(
                            type = licenceCondition.mainCategory.description,
                            description = licenceCondition.subCategory?.description,
                        )
                    },
                )
            }
        )
    }
}
