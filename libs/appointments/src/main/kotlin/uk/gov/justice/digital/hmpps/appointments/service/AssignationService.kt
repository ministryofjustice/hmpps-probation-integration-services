package uk.gov.justice.digital.hmpps.appointments.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities
import uk.gov.justice.digital.hmpps.appointments.model.Assigned
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentsRepositories
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentsRepositories.getByCode

@Service
class AssignationService(
    private val teamRepository: AppointmentsRepositories.TeamRepository,
    private val staffRepository: AppointmentsRepositories.StaffRepository,
    private val locationRepository: AppointmentsRepositories.LocationRepository,
) {
    fun retrieveAssignation(assigned: Assigned?, manager: AppointmentEntities.PersonManager): Assignation {
        val team = assigned?.team?.code?.let { teamRepository.getByCode(it) } ?: manager.team
        val staff = assigned?.staff?.code?.let { staffRepository.getByCode(it) } ?: manager.staff
        val location = assigned?.location?.code?.let { locationRepository.getByCode(it) }
        return Assignation(team = team, staff = staff, officeLocation = location)
    }
}

data class Assignation(
    val officeLocation: AppointmentEntities.OfficeLocation?,
    val team: AppointmentEntities.Team,
    val staff: AppointmentEntities.Staff,
)