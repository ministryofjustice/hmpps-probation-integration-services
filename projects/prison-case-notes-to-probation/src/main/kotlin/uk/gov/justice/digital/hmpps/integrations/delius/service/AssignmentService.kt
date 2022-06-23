package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exceptions.InvalidEstablishmentCodeException
import uk.gov.justice.digital.hmpps.exceptions.ProbationAreaNotFoundException
import uk.gov.justice.digital.hmpps.exceptions.TeamNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.entity.StaffTeam
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.model.StaffName
import uk.gov.justice.digital.hmpps.integrations.delius.repository.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.StaffTeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.TeamRepository

@Service
class AssignmentService(
    private val probationAreaRepository: ProbationAreaRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val officerCodeGenerator: OfficerCodeGenerator,
    private val userService: UserService,
    private val staffTeamRepository: StaffTeamRepository
) {

    fun findAssignment(establishmentCode: String, staffName: StaffName): Triple<Long, Long, Long> {
        if (establishmentCode.length < 3) throw InvalidEstablishmentCodeException(establishmentCode)

        val pa = probationAreaRepository.findByInstitutionNomisCode(establishmentCode.substring(0, 3))
            ?: throw ProbationAreaNotFoundException(establishmentCode.substring(0, 3))
        val team = teamRepository.findByCode("${pa.code}CSN")
            ?: throw TeamNotFoundException("${pa.code}CSN")
        val staff = getStaff(pa, team, staffName)
        return Triple(pa.id, team.id, staff.id)
    }

    private fun getStaff(probationArea: ProbationArea, team: Team, staffName: StaffName): Staff {
        return staffRepository.findTopByProbationAreaIdAndForenameIgnoreCaseAndSurnameIgnoreCase(
            probationArea.id,
            staffName.forename,
            staffName.surname
        ) ?: run {
            val user = userService.findServiceUser()
            val staff = staffRepository.save(
                Staff(
                    forename = staffName.forename,
                    surname = staffName.surname,
                    probationAreaId = probationArea.id,
                    code = officerCodeGenerator.generateFor(probationArea.code),
                    createdByUserId = user.id,
                    lastModifiedUserId = user.id
                )
            )
            staffTeamRepository.save(StaffTeam(staff.id, team.id, user.id, user.id))
            staff
        }
    }
}
