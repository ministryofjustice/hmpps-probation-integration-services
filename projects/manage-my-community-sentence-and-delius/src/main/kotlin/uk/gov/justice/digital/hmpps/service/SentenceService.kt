package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.sentence.requirement.Requirement.Companion.RAR
import uk.gov.justice.digital.hmpps.entity.sentence.requirement.Requirement.Companion.UPW
import uk.gov.justice.digital.hmpps.model.DurationUnit
import uk.gov.justice.digital.hmpps.model.SentenceProgress
import uk.gov.justice.digital.hmpps.model.SentenceProgress.*
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.repository.EventRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.repository.UnpaidWorkAppointmentRepository

@Service
class SentenceService(
    private val eventRepository: EventRepository,
    private val personRepository: PersonRepository,
    private val contactRepository: ContactRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository
) {
    fun getSentenceProgress(crn: String): SentenceProgress {
        val personId = personRepository.getIdByCrn(crn)
        return SentenceProgress(
            sentences = eventRepository.findByPersonIdAndDisposalNotNull(personId).mapNotNull { it.disposal }.map {
                Sentence(
                    type = it.type.description,
                    startDate = it.date,
                    expectedEndDate = it.enteredExpectedEndDate ?: it.expectedEndDate,
                    requirements = it.requirements.map { requirement ->
                        Requirement(
                            type = requirement.mainCategory.description,
                            description = requirement.subCategory?.description,
                            required = requirement.length,
                            completed = when (requirement.mainCategory.code) {
                                RAR -> contactRepository.countRarDaysAttended(requirement.id)
                                UPW -> unpaidWorkAppointmentRepository.countHoursAttended(requirement.disposal.id)
                                else -> null
                            },
                            unit = requirement.length?.let { DurationUnit.ofCode(requirement.mainCategory.lengthUnits.code) },
                        )
                    },
                    licenceConditions = it.licenceConditions.map { licenceCondition ->
                        LicenceCondition(
                            type = licenceCondition.mainCategory.description,
                            description = licenceCondition.subCategory?.description,
                            startDate = licenceCondition.commencementDate ?: licenceCondition.startDate,
                            expectedEndDate = licenceCondition.expectedEndDate,
                        )
                    },
                )
            }
        )
    }
}
