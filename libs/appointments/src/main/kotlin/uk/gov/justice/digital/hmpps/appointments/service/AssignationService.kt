package uk.gov.justice.digital.hmpps.appointments.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.appointments.domain.provider.Location
import uk.gov.justice.digital.hmpps.appointments.domain.provider.LocationRepository
import uk.gov.justice.digital.hmpps.appointments.domain.person.PersonManager
import uk.gov.justice.digital.hmpps.appointments.domain.provider.Staff
import uk.gov.justice.digital.hmpps.appointments.domain.provider.StaffRepository
import uk.gov.justice.digital.hmpps.appointments.domain.provider.Team
import uk.gov.justice.digital.hmpps.appointments.domain.provider.TeamRepository
import uk.gov.justice.digital.hmpps.appointments.domain.provider.getStaffByCode
import uk.gov.justice.digital.hmpps.appointments.domain.provider.getLocationByCode
import uk.gov.justice.digital.hmpps.appointments.domain.provider.getTeamByCode
import uk.gov.justice.digital.hmpps.appointments.model.Assigned

@Service
class AssignationService(
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val locationRepository: LocationRepository,
) {
    fun retrieveAssignation(assigned: Assigned?, manager: PersonManager): Assignation {
        val team = assigned?.team?.code?.let { teamRepository.getTeamByCode(it) } ?: manager.team
        val staff = assigned?.staff?.code?.let { staffRepository.getStaffByCode(it) } ?: manager.staff
        val location = assigned?.location?.code?.let { locationRepository.getLocationByCode(team.provider.code, it) }
        return Assignation(team = team, staff = staff, location = location)
    }
}

data class Assignation(
    val location: Location?,
    val team: Team,
    val staff: Staff,
)