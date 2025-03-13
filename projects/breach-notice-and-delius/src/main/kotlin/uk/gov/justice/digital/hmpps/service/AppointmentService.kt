package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.ldap.findAttributeByUsername
import uk.gov.justice.digital.hmpps.model.*
import java.time.LocalDateTime

@Service
class AppointmentService(
    private val personManagerRepository: PersonManagerRepository,
    private val ldapTemplate: LdapTemplate,
    private val contactRepository: ContactRepository
) {
    fun getNextAppointmentDetails(crn: String): NextAppointmentDetails {
        val manager = personManagerRepository.getCurrentManagerFor(crn)
        manager.staff.user?.apply {
            ldapTemplate.findAttributeByUsername(username, "telephoneNumber")?.let { telephone = it }
        }

        val futureAppointments = contactRepository.findFutureAppointments(crn)

        return NextAppointmentDetails(
            manager.asResponsibleOfficer(),
            futureAppointments.map(Contact::asAppointment)
        )
    }
}

fun PersonManager.asResponsibleOfficer(): ResponsibleOfficer = ResponsibleOfficer(staff.name(), staff.user?.telephone)

fun Contact.asAppointment() = Appointment(
    id,
    type.codedDescription(),
    LocalDateTime.of(date, startTime.withZoneSameInstant(EuropeLondon).toLocalTime()),
    description,
    location?.toAddress(),
    Officer(staff.code, staff.name()),
)

fun Staff.name() = Name(forename, middleName, surname)