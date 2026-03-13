package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.person.PersonRepository
import uk.gov.justice.digital.hmpps.entity.person.getByCrn
import uk.gov.justice.digital.hmpps.entity.sentence.EventRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.LinkedListRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UpwDetailsRepository
import uk.gov.justice.digital.hmpps.model.Case
import uk.gov.justice.digital.hmpps.model.CodeDescription
import uk.gov.justice.digital.hmpps.model.Offence
import uk.gov.justice.digital.hmpps.model.PersonName
import uk.gov.justice.digital.hmpps.model.UnpaidWorkDetails
import uk.gov.justice.digital.hmpps.model.UnpaidWorkMinutes

@Service
class CaseSummaryService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val upwDetailsRepository: UpwDetailsRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
    private val linkedListRepository: LinkedListRepository,
    private val userAccessService: UserAccessService,
) {
    fun getSummaryForCase(crn: String, username: String): UnpaidWorkDetails {
        val person = personRepository.getByCrn(crn)
        val personId = person.id
        val laoStatus = userAccessService.caseAccessFor(username, crn)
        val events = eventRepository.getByPerson_Id(personId)
        val details = upwDetailsRepository.findByEventIdIn(events.map { it.id })
        val requiredMinutes = unpaidWorkAppointmentRepository
            .getUpwRequiredAndCompletedMinutes(details.map { it.id })
        val linkedListEntry = linkedListRepository.findByData1Code("ETE")
        val eteAppts = unpaidWorkAppointmentRepository.findByDetailsDisposalEventIdInAndProjectProjectTypeCodeIn(
            eventIds = events.map { it.id },
            projectTypeCodes = linkedListEntry.map { it.data2.code }
        )
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

        val upwMinutes = details.map { detail ->
            val matchingMinutes = requiredMinutes.filter { it.id == detail.id }
            val eteMinutes = eteAppts.filter { it.details.id == detail.id }.sumOf { it.minutesCredited ?: 0 }
            val disposal = detail.disposal
            val mainOffence = disposal.mainOffence
            UnpaidWorkMinutes(
                eventNumber = disposal.event.number.toLong(),
                sentenceDate = disposal.date,
                requiredMinutes = matchingMinutes.sumOf { it.requiredMinutes },
                adjustments = matchingMinutes.sumOf { it.positiveAdjustments - it.negativeAdjustments },
                completedMinutes = matchingMinutes.sumOf { it.completedMinutes },
                completedEteMinutes = eteMinutes,
                eventOutcome = disposal.type.description,
                upwStatus = detail.status?.description,
                referralDate = disposal.event.referralDate,
                convictionDate = disposal.event.convictionDate,
                court = CodeDescription(
                    code = detail.disposal.event.court.code,
                    description = detail.disposal.event.court.courtName
                ),
                mainOffence = Offence(
                    date = mainOffence.offenceDate,
                    count = mainOffence.offenceCount,
                    code = mainOffence.offence.mainCategoryCode,
                    description = mainOffence.offence.mainCategoryDescription
                )
            )
        }
        return UnpaidWorkDetails(case, upwMinutes)
    }
}