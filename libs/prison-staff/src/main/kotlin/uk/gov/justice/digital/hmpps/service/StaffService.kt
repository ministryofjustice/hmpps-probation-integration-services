package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.ConcurrentReferenceHashMap
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

    companion object {
        private val mutexMap = ConcurrentReferenceHashMap<Long, Any>()
        private fun getMutex(key: Long) {
            mutexMap.compute(key) { _, v -> v ?: Any() }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun create(
        paId: Long,
        paCode: String,
        teamId: Long,
        staffName: StaffName,
        startDate: ZonedDateTime? = null
    ): PrisonStaff = synchronized(getMutex(paId)) {
        val staff = staffRepository.save(
            PrisonStaff(
                forename = staffName.forename,
                surname = staffName.surname,
                probationAreaId = paId,
                code = officerCodeGenerator.generateFor(paCode),
                startDate = startDate ?: ZonedDateTime.now()
            )
        )
        staffTeamRepository.save(PrisonStaffTeam(staff.id, teamId))
        return staff
    }

    fun findStaff(probationAreaId: Long, staffName: StaffName) =
        staffRepository.findTopByProbationAreaIdAndForenameIgnoreCaseAndSurnameIgnoreCase(
            probationAreaId,
            staffName.forename,
            staffName.surname
        )

    fun getByCode(code: String) = staffRepository.getByCode(code)
}
