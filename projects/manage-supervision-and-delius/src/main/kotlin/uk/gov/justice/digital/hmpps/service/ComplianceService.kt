package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.activity.Activity
import uk.gov.justice.digital.hmpps.api.model.compliance.Breach
import uk.gov.justice.digital.hmpps.api.model.compliance.PersonCompliance
import uk.gov.justice.digital.hmpps.api.model.compliance.SentenceCompliance
import uk.gov.justice.digital.hmpps.api.model.overview.*
import uk.gov.justice.digital.hmpps.api.model.overview.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.getAllBreaches
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import java.time.LocalDate

@Service
class ComplianceService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val nsiRepository: NsiRepository,
    private val activityService: ActivityService,
    private val requirementService: RequirementService
) {

    @Transactional
    fun getPersonCompliance(crn: String): PersonCompliance {

        val summary = personRepository.getSummary(crn)
        val events = eventRepository.findByPersonId(summary.id)
        val currentSentences = events.filter { !it.isInactiveEvent() }

        val allActiveSentenceActivity =
            activityService.getPersonSentenceActivity(summary.id, currentSentences.map { it.id })
        val allBreaches = nsiRepository.getAllBreaches(summary.id)
        val previousOrders = events.filter { it.isInactiveEvent() }

        fun breachesForSentence(eventId: Long) = allBreaches.filter { (it.eventId == eventId) }
        fun activeBreachCountForSentence(eventId: Long) =
            allBreaches.firstOrNull { it.eventId == eventId && it.active }

        fun sentenceActivity(eventNumber: String) = allActiveSentenceActivity.filter { it.eventNumber == eventNumber }

        fun getRarCategoryFromSentence(eventNumber: String) =
            allActiveSentenceActivity.firstOrNull { it.eventNumber == eventNumber && it.isRarRelated == true }?.rarCategory

        fun rarActivity(eventNumber: String) =
            allActiveSentenceActivity.filter { it.eventNumber == eventNumber && it.isRarRelated == true }

        fun Event.toSentenceCompliance() = mainOffence?.offence?.let { offence ->
            SentenceCompliance(
                eventNumber = eventNumber,
                mainOffence = Offence(code = offence.code, description = offence.description),
                rarCategory = getRarCategoryFromSentence(eventNumber),
                rarDescription = disposal?.let { requirementService.getRarDescription(id, eventNumber, disposal.id) },
                order = disposal?.let {
                    Order(
                        description = it.type.description,
                        length = it.length,
                        startDate = it.date,
                        endDate = it.expectedEndDate()
                    )
                },
                activeBreach = activeBreachCountForSentence(id)?.let {
                    Breach(
                        startDate = it.startDate(),
                        status = it.nsiStatus?.description
                    )
                },
                activity = toActivityCounts(
                    rarActivity(
                        eventNumber
                    )
                ),
                compliance = toSentenceCompliance(sentenceActivity(eventNumber), breachesForSentence(id))
            )
        }
        return PersonCompliance(
            personSummary = summary.toPersonSummary(),
            currentSentences = currentSentences.mapNotNull { it.toSentenceCompliance() },
            previousOrders = PreviousOrders(
                breaches = previousOrders.flatMap { breachesForSentence(it.id) }.count(),
                count = previousOrders.size,
                lastEndedDate = previousOrders.firstOrNull()?.disposal?.terminationDate,
                orders = previousOrders.filter {
                    // only display those within the last two years
                    val endDate =
                        listOfNotNull(it.disposal?.terminationDate, it.lastUpdatedDateTime.toLocalDate()).max()
                    endDate.isAfter(LocalDate.now().minusYears(2))
                }.mapNotNull {
                    it.disposal?.toOrder(
                        it.eventNumber,
                        it.mainOffence?.offence?.description,
                        breachesForSentence(it.id).size
                    )
                }
            ))
    }

    fun Disposal.toOrder(eventNumber: String, mainOffence: String?, breachCount: Int) =
        Order(
            eventNumber,
            description = type.description,
            length = length,
            startDate = date,
            endDate = expectedEndDate(),
            mainOffence = mainOffence,
            breaches = breachCount,
            status = terminationReason?.description
        )
}

fun toActivityCounts(activities: List<Activity>) = ActivityCount(
    waitingForEvidenceCount = activities.count { it.isPastAppointment && it.absentWaitingEvidence == true },
    absentCount = activities.count { it.isPastAppointment && it.wasAbsent == true },
    attendedButDidNotComplyCount = activities.count { it.isPastAppointment && it.wasAbsent == false && it.didTheyComply == false },
    compliedAppointmentsCount = activities.count { it.isInPast && it.didTheyComply == true },
    lettersCount = activities.count { it.isPastAppointment && it.action != null },
    nationalStandardAppointmentsCount = activities.count { it.isPastAppointment && it.isNationalStandard },
    outcomeNotRecordedCount = activities.count { it.isInPast && it.hasOutcome == false },
    rescheduledByPersonOnProbationCount = activities.count { it.rescheduled && it.rescheduledPop },
    rescheduledByStaffCount = activities.count { it.rescheduled && it.rescheduledStaff },
    rescheduledCount = activities.count { it.rescheduled },
    unacceptableAbsenceCount = activities.count { it.isPastAppointment && it.wasAbsent == true && it.acceptableAbsence == false },
    acceptableAbsenceCount = activities.count { it.isPastAppointment && it.wasAbsent == true && it.acceptableAbsence == true }
)

fun toSentenceCompliance(activities: List<Activity>, breaches: List<Nsi>) = Compliance(
    breachStarted = breaches.count { it.active } > 0,
    breachesOnCurrentOrderCount = breaches.count { it.active },
    priorBreachesOnCurrentOrderCount = breaches.count { !it.active },
    currentBreaches = breaches.count(),
    failureToComplyCount = activities.count { it.isPastAppointment && it.didTheyComply == false }
)



