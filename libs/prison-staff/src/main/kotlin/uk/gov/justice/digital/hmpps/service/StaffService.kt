package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.entity.PrisonStaff
import uk.gov.justice.digital.hmpps.entity.PrisonStaffTeam
import uk.gov.justice.digital.hmpps.model.StaffName
import uk.gov.justice.digital.hmpps.repository.PrisonStaffRepository
import uk.gov.justice.digital.hmpps.repository.PrisonStaffTeamRepository
import uk.gov.justice.digital.hmpps.repository.getByCode
import java.time.ZonedDateTime

@Service
class StaffService(
    private val staffRepository: PrisonStaffRepository,
    private val officerCodeGenerator: OfficerCodeGenerator,
    private val staffTeamRepository: PrisonStaffTeamRepository
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun create(
        probationAreaId: Long,
        probationAreaCode: String,
        teamId: Long,
        staffName: StaffName,
        startDate: ZonedDateTime? = null
    ) = staffRepository.save(
        PrisonStaff(
            forename = staffName.forename,
            surname = staffName.surname,
            probationAreaId = probationAreaId,
            code = officerCodeGenerator.generateFor(probationAreaCode),
            startDate = startDate ?: ZonedDateTime.now()
        )
    ).also { staffTeamRepository.save(PrisonStaffTeam(it.id, teamId)) }

    fun findStaff(probationAreaId: Long, staffName: StaffName) =
        staffRepository.findTopByProbationAreaIdAndForenameIgnoreCaseAndSurnameIgnoreCase(
            probationAreaId,
            staffName.forename,
            staffName.surname
        )

    fun getByCode(code: String) = staffRepository.getByCode(code)
}
