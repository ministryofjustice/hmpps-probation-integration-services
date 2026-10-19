package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import uk.gov.justice.digital.hmpps.entity.sentence.requirement.Requirement
import uk.gov.justice.digital.hmpps.entity.sentence.requirement.Requirement.Companion.RAR
import uk.gov.justice.digital.hmpps.entity.sentence.requirement.Requirement.Companion.UPW
import uk.gov.justice.digital.hmpps.model.CodeDescription
import uk.gov.justice.digital.hmpps.model.CodeDescription.Companion.toModel
import uk.gov.justice.digital.hmpps.model.DurationUnit
import uk.gov.justice.digital.hmpps.model.SentenceProgress
import uk.gov.justice.digital.hmpps.model.SentenceProgress.LicenceCondition
import uk.gov.justice.digital.hmpps.model.SentenceProgress.Requirement as ModelRequirement
import uk.gov.justice.digital.hmpps.model.SentenceProgress.Sentence
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.repository.EventRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.repository.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.service.UPWSubTypes.Companion.upwSubType

@Service
class SentenceService(
    private val eventRepository: EventRepository,
    private val personRepository: PersonRepository,
    private val contactRepository: ContactRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
) {
    @Transactional(readOnly = true)
    fun getSentenceProgress(crn: String): SentenceProgress {
        val personId = personRepository.getIdByCrn(crn)
        val events = eventRepository.findByPersonIdAndDisposalNotNull(personId)
        return SentenceProgress(
            sentences = events.mapNotNull { it.disposal }.map { disposal ->
                val apportionedUPW = disposal.apportionHoursByRequirementId()
                Sentence(
                    type = disposal.type.description,
                    startDate = disposal.date,
                    expectedEndDate = disposal.custody?.sentenceExpiryDate?.date
                        ?: disposal.enteredExpectedEndDate ?: disposal.expectedEndDate,
                    lastUpdatedAt = disposal.lastUpdatedDatetime,
                    requirements = disposal.requirements
                        .sortedBy { it.id }
                        .map { requirement ->
                            ModelRequirement(
                                type = requirement.mainCategory.description,
                                description = requirement.subCategory?.description,
                                mainCategory = requirement.mainCategory.toModel(),
                                subCategory = requirement.subCategory?.toModel(),
                                required = requirement.length,
                                completed = when (requirement.mainCategory.code) {
                                    RAR -> contactRepository.countRarDaysAttended(requirement.id)
                                    UPW -> apportionedUPW[requirement.id] ?: 0
                                    else -> null
                                },
                                unit = requirement.length?.let { DurationUnit.ofCode(requirement.mainCategory.lengthUnits.code) },
                                imposedDate = requirement.imposedDate,
                                expectedStartDate = requirement.expectedStartDate,
                                expectedEndDate = requirement.expectedEndDate,
                                actualStartDate = requirement.actualStartDate,
                                actualEndDate = requirement.actualEndDate,
                                terminationReason = requirement.terminationReason?.toModel(),
                                lastUpdatedAt = requirement.lastUpdatedDatetime,
                            )
                        },
                    licenceConditions = disposal.licenceConditions.map { licenceCondition ->
                        LicenceCondition(
                            type = licenceCondition.mainCategory.description,
                            description = licenceCondition.subCategory?.description,
                            mainCategory = licenceCondition.mainCategory.toModel(),
                            subCategory = licenceCondition.subCategory?.toModel(),
                            startDate = licenceCondition.commencementDate ?: licenceCondition.startDate,
                            expectedEndDate = licenceCondition.expectedEndDate,
                        )
                    },
                    mainOffence = disposal.event.mainOffence!!.offence.let { offence ->
                        CodeDescription(
                            code = offence.code,
                            description = offence.description,
                        )
                    },
                    additionalOffences = disposal.event.additionalOffences.map { offence ->
                        CodeDescription(
                            code = offence.offence.code,
                            description = offence.offence.description,
                        )
                    },
                )
            }
        )
    }

    fun Disposal.apportionHoursByRequirementId(): Map<Long, Int> {
        val upwRequirements = requirements
            .filter { requirement -> requirement.mainCategory.code == UPW }
        val totalUPWHours = unpaidWorkAppointmentRepository.countHoursAttended(id) ?: 0
        val totalHours = totalUPWHours.coerceAtLeast(0)
        var runningTotal = totalHours
        var concurrentRunningTotal = totalHours
        val apportionedHours = mutableMapOf<Long, Int>()

        upwRequirements
            .sortedWith(compareBy<Requirement> {
                it.upwSubType().ordinal
            }.thenBy { it.id })
            .forEach { requirement ->
                val isConcurrent = UPWSubTypes.isConcurrent(requirement.upwSubType().code)
                val remainingHours = if (isConcurrent) concurrentRunningTotal else runningTotal
                if (remainingHours <= 0) return@forEach

                val apportioned = minOf(requirement.length ?: 0, remainingHours)
                if (apportioned <= 0) return@forEach

                apportionedHours[requirement.id] = apportioned
                if (isConcurrent) {
                    concurrentRunningTotal -= apportioned
                } else {
                    runningTotal -= apportioned
                }
            }

        return apportionedHours
    }

}
