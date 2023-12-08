package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exceptions.InvalidEstablishmentCodeException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.model.StaffName
import uk.gov.justice.digital.hmpps.integrations.delius.repository.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.TeamRepository
import uk.gov.justice.digital.hmpps.retry.retry

@Service
class AssignmentService(
    private val probationAreaRepository: ProbationAreaRepository,
    private val teamRepository: TeamRepository,
    private val staffService: StaffService,
) {
    fun findAssignment(
        establishmentCode: String,
        staffName: StaffName,
    ): Triple<Long, Long, Long> {
        if (establishmentCode.length < 3) throw InvalidEstablishmentCodeException(establishmentCode)

        val pa =
            probationAreaRepository.findByInstitutionNomisCode(establishmentCode.substring(0, 3))
                ?: throw NotFoundException(
                    "Probation Area not found for NOMIS institution: ${establishmentCode.substring(0, 3)}",
                )
        val team =
            teamRepository.findByCode("${pa.code}CSN")
                ?: throw NotFoundException("Team", "code", "${pa.code}CSN")
        val staff = getStaff(pa, team, staffName)
        return Triple(pa.id, team.id, staff.id)
    }

    private fun getStaff(
        probationArea: ProbationArea,
        team: Team,
        staffName: StaffName,
    ): Staff {
        val findStaff = { staffService.findStaff(probationArea.id, staffName) }
        return retry(3) {
            findStaff() ?: staffService.create(probationArea, team, staffName)
        }
    }
}
