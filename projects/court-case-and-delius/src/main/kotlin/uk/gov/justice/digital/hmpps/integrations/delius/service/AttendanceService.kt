package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Attendance
import uk.gov.justice.digital.hmpps.api.model.Attendances
import uk.gov.justice.digital.hmpps.api.model.ContactTypeDetail
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.AttendanceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson
import java.time.LocalDate

@Service
class AttendanceService(
    private val personRepository: PersonRepository,
    private val attendanceRepository: AttendanceRepository
) {

    fun getAttendancesFor(crn: String, eventId: Long): Attendances {
        val person = personRepository.getPerson(crn)
        val attendances =
            attendanceRepository.findByOffenderAndEventId(eventId, person.id, LocalDate.now()).map { it.toAttendance() }
        return Attendances(attendances)
    }
}

fun Contact.toAttendance() = Attendance(
    attended = attended ?: false,
    complied = complied ?: false,
    attendanceDate = date,
    contactId = id,
    outcome = outcome?.description,
    contactType = ContactTypeDetail(code = type?.code, description = type?.description)
)
