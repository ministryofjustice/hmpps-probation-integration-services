package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Address
import uk.gov.justice.digital.hmpps.api.model.Appointment
import uk.gov.justice.digital.hmpps.api.model.Location
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.Staff
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.AppointmentRepository
import uk.gov.justice.digital.hmpps.entity.LdapUserDetails
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import java.time.LocalDate
import java.time.ZonedDateTime

@Service
class AppointmentService(
    private val appointmentRepository: AppointmentRepository,
    private val ldap: LdapTemplate,
) {
    fun findAppointmentsFor(
        crn: String,
        startDate: LocalDate,
        endDate: LocalDate,
        pageable: Pageable,
    ) = appointmentRepository.findAppointmentsFor(crn, startDate, endDate, pageable).map { it.asAppointment() }

    private fun uk.gov.justice.digital.hmpps.entity.Appointment.asAppointment(): Appointment =
        Appointment(
            Appointment.Type(type.code, type.description),
            ZonedDateTime.of(date, startTime.toLocalTime(), EuropeLondon),
            duration,
            staff.asStaff(),
            location?.asLocation(),
            description ?: type.description,
            outcome?.let { Appointment.Outcome(it.code, it.description) },
        )

    private fun uk.gov.justice.digital.hmpps.entity.Staff.asStaff(): Staff {
        val userDetails = user?.let { ldap.findByUsername<LdapUserDetails>(it.username) }
        return Staff(code, Name(forename, surname), userDetails?.email, userDetails?.telephone)
    }

    private fun uk.gov.justice.digital.hmpps.entity.Location.asLocation(): Location =
        Location(
            code,
            description,
            Address.from(buildingName, buildingNumber, streetName, district, townCity, county, postcode),
            telephoneNumber,
        )
}
