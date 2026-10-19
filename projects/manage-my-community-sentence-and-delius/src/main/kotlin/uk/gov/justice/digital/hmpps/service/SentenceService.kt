package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
            sentences = events.mapNotNull { it.disposal }.map { disposal ->
                val upwRequirements = disposal.requirements
                    .filter { requirement -> requirement.mainCategory.code == UPW }
                val apportionedUPW = if (upwRequirements.isEmpty()) {
                    emptyMap<Long, Int>()
                } else {
                    apportionHoursByRequirementId(
                        upwRequirements.map { requirement ->
                            Triple(requirement.id, requirement.subCategory?.code ?: UPW, requirement.length)
                        },
                        unpaidWorkAppointmentRepository.countHoursAttended(disposal.id) ?: 0,
                    )
                }

                Sentence(
                    type = disposal.type.description,
                    startDate = disposal.date,
                    expectedEndDate = disposal.custody?.sentenceExpiryDate?.date
                        ?: disposal.enteredExpectedEndDate ?: disposal.expectedEndDate,
                    lastUpdatedAt = disposal.lastUpdatedDatetime,
                    requirements = disposal.requirements
                        .sortedBy { it.id }
                        .map { requirement ->
                            Requirement(
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

    fun apportionHoursByRequirementId(
        upwRequirements: List<Triple<Long, String, Int?>>,
        totalUPWHours: Int
    ): Map<Long, Int> {
        val totalHours = totalUPWHours.coerceAtLeast(0)
        var runningTotal = totalHours
        var concurrentRunningTotal = totalHours
        val apportionedHours = mutableMapOf<Long, Int>()

        upwRequirements
            .sortedWith(compareBy<Triple<Long, String, Int?>> { UPWSubTypes.priorityOf(it.second) }.thenBy { it.first })
            .forEach { (requirementId, subTypeCode, requiredHours) ->
                val remainingHours = if (UPWSubTypes.isConcurrent(subTypeCode)) concurrentRunningTotal else runningTotal
                if (remainingHours <= 0) return@forEach

                val apportioned = minOf(requiredHours ?: 0, remainingHours)
                if (apportioned <= 0) return@forEach

                apportionedHours[requirementId] = apportioned
                if (UPWSubTypes.isConcurrent(subTypeCode)) {
                    concurrentRunningTotal -= apportioned
                } else {
                    runningTotal -= apportioned
                }
            }

        return apportionedHours
    }

    fun apportionHours(upwSubTypes: List<Pair<String, Int?>>, totalUPWHours: Int): Map<String, Int> {
        val totalHours = totalUPWHours.coerceAtLeast(0)
        var runningTotal = totalHours
        var concurrentRunningTotal = totalHours
        val apportionedHours = mutableMapOf<String, Int>()

        upwSubTypes
            .sortedWith(compareBy<Pair<String, Int?>> { UPWSubTypes.priorityOf(it.first) })
            .forEach { (subTypeCode, requiredHours) ->
                val remainingHours = if (UPWSubTypes.isConcurrent(subTypeCode)) concurrentRunningTotal else runningTotal
                if (remainingHours <= 0) return@forEach

                val apportioned = minOf(requiredHours ?: 0, remainingHours)
                if (apportioned <= 0) return@forEach

                apportionedHours[subTypeCode] = (apportionedHours[subTypeCode] ?: 0) + apportioned
                if (UPWSubTypes.isConcurrent(subTypeCode)) {
                    concurrentRunningTotal -= apportioned
                } else {
                    runningTotal -= apportioned
                }
            }

        return apportionedHours
    }

    enum class UPWSubTypes(val code: String, val description: String, val concurrency: Boolean) {
        REGULAR("W01", "Regular UPW Hours", false),
        ADDITIONAL("W03", "Additional UPW Hours", false),
        HOURS_CONSECUTIVE_TO_OTHER_ORDER("W07", "Hours consecutive to another order", false),
        HOURS_CONCURRENT_TO_ANOTHER_ORDER("W06", "Hours Concurrent to another Order", true),
        HOURS_OUTSTANDING_AT_SENTENCE_EXPIRY("W08", "Hours Outstanding at Sentence Expiry", false),
        HOURS_OUTSTANDING_FROM_PREVIOUS_ORDER("W05", "Z - Hours Outstanding from a Previous Order", false);

        companion object {
            fun fromCode(code: String): UPWSubTypes? {
                return when {
                    code.equals("UPW", ignoreCase = true) -> REGULAR
                    else -> entries.find { it.code.equals(code, ignoreCase = true) }
                }
            }

            fun priorityOf(code: String): Int {
                return when (fromCode(code)) {
                    REGULAR -> 0
                    ADDITIONAL -> 1
                    HOURS_CONSECUTIVE_TO_OTHER_ORDER -> 2
                    HOURS_CONCURRENT_TO_ANOTHER_ORDER -> 3
                    HOURS_OUTSTANDING_FROM_PREVIOUS_ORDER -> 4
                    HOURS_OUTSTANDING_AT_SENTENCE_EXPIRY -> 5
                    null -> Int.MAX_VALUE
                }
            }

            fun isConcurrent(code: String): Boolean {
                return fromCode(code)?.concurrency == true
            }
        }
    }
}
