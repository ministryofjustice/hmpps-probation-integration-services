package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.entity.sentence.requirement.Requirement as RequirementEntity
import uk.gov.justice.digital.hmpps.entity.sentence.requirement.Requirement.Companion.RAR
import uk.gov.justice.digital.hmpps.entity.sentence.requirement.Requirement.Companion.UPW
import uk.gov.justice.digital.hmpps.model.CodeDescription
import uk.gov.justice.digital.hmpps.model.CodeDescription.Companion.toModel
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
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
) {
    @Transactional(readOnly = true)
    fun getSentenceProgress(crn: String): SentenceProgress {
        val personId = personRepository.getIdByCrn(crn)
        val events = eventRepository.findByPersonIdAndDisposalNotNull(personId)
        return SentenceProgress(
            sentences = events.mapNotNull { it.disposal }.map {
                Sentence(
                    type = it.type.description,
                    startDate = it.date,
                    expectedEndDate = it.custody?.sentenceExpiryDate?.date
                        ?: it.enteredExpectedEndDate ?: it.expectedEndDate,
                    lastUpdatedAt = it.lastUpdatedDatetime,
                    requirements = mapRequirements(it.requirements.toList()),
                    licenceConditions = it.licenceConditions.map { licenceCondition ->
                        LicenceCondition(
                            type = licenceCondition.mainCategory.description,
                            description = licenceCondition.subCategory?.description,
                            mainCategory = licenceCondition.mainCategory.toModel(),
                            subCategory = licenceCondition.subCategory?.toModel(),
                            startDate = licenceCondition.commencementDate ?: licenceCondition.startDate,
                            expectedEndDate = licenceCondition.expectedEndDate,
                        )
                    },
                    mainOffence = it.event.mainOffence!!.offence.let { offence ->
                        CodeDescription(
                            code = offence.code,
                            description = offence.description,
                        )
                    },
                    additionalOffences = it.event.additionalOffences.map { offence ->
                        CodeDescription(
                            code = offence.offence.code,
                            description = offence.offence.description,
                        )
                    },
                )
            }
        )
    }

    private fun mapRequirements(requirements: List<RequirementEntity>): List<Requirement> {
        if (requirements.none { it.mainCategory.code == UPW }) {
            return requirements.map(::toSentenceRequirement)
        }

        val unpaidWorkHoursByDisposal = requirements
            .filter { it.mainCategory.code == UPW }
            .map { it.disposal.id }
            .distinct()
            .associateWith { unpaidWorkAppointmentRepository.countHoursAttended(it) ?: 0 }

        val groupedRequirements = mutableListOf<Requirement>()
        val groupedUpwDisposals = mutableSetOf<Long>()

        requirements.forEach { requirement ->
            if (requirement.mainCategory.code == UPW) {
                if (groupedUpwDisposals.add(requirement.disposal.id)) {
                    groupedRequirements += toGroupedUnpaidWorkRequirement(
                        requirements.filter { it.disposal.id == requirement.disposal.id && it.mainCategory.code == UPW },
                        unpaidWorkHoursByDisposal.getValue(requirement.disposal.id),
                    )
                }
            } else {
                groupedRequirements += toSentenceRequirement(requirement)
            }
        }

        return groupedRequirements
    }

    private fun toSentenceRequirement(requirement: RequirementEntity) = Requirement(
        type = requirement.mainCategory.description,
        description = requirement.subCategory?.description,
        mainCategory = requirement.mainCategory.toModel(),
        subCategory = requirement.subCategory?.toModel(),
        required = requirement.length,
        completed = when (requirement.mainCategory.code) {
            RAR -> contactRepository.countRarDaysAttended(requirement.id)
            UPW -> unpaidWorkAppointmentRepository.countHoursAttended(requirement.disposal.id) ?: 0
            else -> null
        },
        unit = requirement.length?.let { DurationUnit.ofCode(requirement.mainCategory.lengthUnits.code) },
        imposedDate = requirement.imposedDate,
        expectedStartDate = requirement.expectedStartDate,
        expectedEndDate = requirement.expectedEndDate,
        actualStartDate = requirement.actualStartDate,
        actualEndDate = requirement.actualEndDate,
        lastUpdatedAt = requirement.lastUpdatedDatetime,
    )

    private fun toGroupedUnpaidWorkRequirement(requirements: List<RequirementEntity>, completedHours: Int): Requirement {
        val first = requirements.first()
        val uniqueSubCategories = requirements.mapNotNull { it.subCategory }.distinctBy { it.id }

        return Requirement(
            type = first.mainCategory.description,
            description = uniqueSubCategories.singleOrNull()?.description,
            mainCategory = first.mainCategory.toModel(),
            subCategory = uniqueSubCategories.singleOrNull()?.toModel(),
            required = requirements.sumOf { it.length ?: 0 },
            completed = completedHours,
            unit = first.length?.let { DurationUnit.ofCode(first.mainCategory.lengthUnits.code) },
            imposedDate = requirements.mapNotNull { it.imposedDate }.minOrNull(),
            expectedStartDate = requirements.mapNotNull { it.expectedStartDate }.minOrNull(),
            expectedEndDate = requirements.mapNotNull { it.expectedEndDate }.maxOrNull(),
            actualStartDate = requirements.mapNotNull { it.actualStartDate }.minOrNull(),
            actualEndDate = requirements.mapNotNull { it.actualEndDate }.maxOrNull(),
            lastUpdatedAt = requirements.maxOf { it.lastUpdatedDatetime },
        )
    }
}
