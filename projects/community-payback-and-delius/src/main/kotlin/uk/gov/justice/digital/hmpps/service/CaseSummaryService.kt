package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.person.PersonRepository
import uk.gov.justice.digital.hmpps.entity.person.getByCrn
import uk.gov.justice.digital.hmpps.entity.sentence.EventRepository
import uk.gov.justice.digital.hmpps.entity.sentence.MainOffenceRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.LinkedListRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UpwDetailsRepository
import uk.gov.justice.digital.hmpps.model.*

@Service
class CaseSummaryService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val upwDetailsRepository: UpwDetailsRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
    private val linkedListRepository: LinkedListRepository,
    private val userAccessService: UserAccessService,
    private val mainOffenceRepository: MainOffenceRepository,
) {
    fun getSummaryForCase(crn: String, username: String?): UnpaidWorkDetails {
        val person = personRepository.getByCrn(crn)
        val personId = person.id
        val laoStatus = getLaoStatus(crn, username)
        val events = eventRepository.getByPersonId(personId)
        val details = upwDetailsRepository.findByEventIdIn(events.map { it.id })
        val requiredMinutes = unpaidWorkAppointmentRepository
            .getUpwRequiredAndCompletedMinutes(details.map { it.id })
        val linkedListEntry = linkedListRepository.findByData1Code("ETE")
        val eteAppts = unpaidWorkAppointmentRepository.findByDetailsDisposalEventIdInAndProjectProjectTypeCodeIn(
            eventIds = events.map { it.id },
            projectTypeCodes = linkedListEntry.map { it.data2.code }
        )
        val mainOffencesByEventId = mainOffenceRepository.getByEventIdIn(events.map { it.id })

        val case = Case(
            crn = crn,
            name = PersonName(
                forename = person.forename,
                surname = person.surname,
                middleNames = listOfNotNull(person.secondName, person.thirdName)
            ),
            dateOfBirth = person.dateOfBirth,
            currentExclusion = laoStatus.userExcluded,
            exclusionMessage = laoStatus.exclusionMessage,
            currentRestriction = laoStatus.userRestricted,
            restrictionMessage = laoStatus.restrictionMessage,
        )

        val upwMinutes = details.mapNotNull { detail ->
            val matchingMinutes = requiredMinutes.filter { it.id == detail.id }
            val eteMinutes = eteAppts.filter { it.details.id == detail.id }.sumOf { it.minutesCredited ?: 0 }
            val disposal = detail.disposal
            val mainOffence = mainOffencesByEventId[disposal.event.id] ?: return@mapNotNull null
            UnpaidWorkMinutes(
                eventNumber = disposal.event.number.toLong(),
                sentenceDate = disposal.date,
                requiredMinutes = matchingMinutes.sumOf { it.requiredMinutes },
                adjustments = matchingMinutes.sumOf { it.positiveAdjustments - it.negativeAdjustments },
                completedMinutes = matchingMinutes.sumOf { it.completedMinutes },
                completedEteMinutes = eteMinutes,
                eventOutcome = disposal.type.description,
                eventOutcomeCode = disposal.type.code,
                upwStatus = detail.status?.description,
                referralDate = disposal.event.referralDate,
                convictionDate = disposal.event.convictionDate,
                court = detail.disposal.event.court?.let { CodeDescription(it.code, it.courtName) },
                mainOffence = Offence(
                    date = mainOffence.offenceDate,
                    count = mainOffence.offenceCount,
                    code = mainOffence.offence.code,
                    description = mainOffence.offence.description
                ),
                unpaidWorkRequirements = disposal.requirements.mapNotNull { requirement ->
                    requirement.requirementSubCategory?.let {
                        RequirementSubType(
                            CodeDescription(
                                it.codeValue,
                                it.codeDescription
                            ),
                        )
                    }
                }
            )
        }
        return UnpaidWorkDetails(case, upwMinutes)
    }

    private fun getLaoStatus(crn: String, username: String?) =
        username?.let {
            userAccessService.caseAccessFor(username, crn)
        } ?: CaseAccess(
            crn = crn,
            userExcluded = true,
            userRestricted = true,
            exclusionMessage = "username not provided so cannot determine exclusion",
            restrictionMessage = "username not provided so cannot determine restriction",
        )
}