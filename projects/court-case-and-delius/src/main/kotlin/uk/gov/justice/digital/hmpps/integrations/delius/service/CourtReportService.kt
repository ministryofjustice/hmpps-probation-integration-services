package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.api.model.StaffHuman
import uk.gov.justice.digital.hmpps.api.model.conviction.CourtReportMinimal
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtReport
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtReportRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.ReportManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.getByPersonAndEventNumber
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff

@Service
class CourtReportService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val courtReportRepository: CourtReportRepository
) {

    fun getCourtReportsFor(crn: String, eventId: Long): List<CourtReportMinimal> {
        val person = personRepository.getPerson(crn)
        val event = eventRepository.getByPersonAndEventNumber(person, eventId)
        return courtReportRepository.findByPersonIdAndEventId(person.id, event.id)
            .map { it.toCourtReport() }
    }
}

fun CourtReport.toCourtReport() = CourtReportMinimal(
    courtReportId = id,
    offenderId = personId,
    requestedDate = dateRequested,
    requiredDate = dateRequired,
    allocationDate = allocationDate,
    completedDate = dateCompleted,
    sentToCourtDate = sentToCourtDate,
    receivedByCourtDate = receivedByCourtDate,
    courtReportType = courtReportType?.let { KeyValue(it.code, it.description) },
    reportManagers = reportManagers.filter { it.staff != null }.map { it.toReportManager() },
    deliveredCourtReportType = deliveredCourtReportType?.let { KeyValue(it.code, it.description) }
)

fun Staff.toStaffHuman() = StaffHuman(
    forenames = listOfNotNull(forename, forename2).joinToString(" "),
    surname = surname,
    code = code
)

fun ReportManager.toReportManager() = uk.gov.justice.digital.hmpps.api.model.conviction.ReportManager(
    active = active,
    staff = staff?.toStaffHuman()
)
