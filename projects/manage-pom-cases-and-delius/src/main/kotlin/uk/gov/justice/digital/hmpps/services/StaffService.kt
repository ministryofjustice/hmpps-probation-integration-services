package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.ConcurrentReferenceHashMap
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffTeam
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffTeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.getByCode
import java.time.ZonedDateTime

@Service
class StaffService(
    private val staffRepository: StaffRepository,
    private val officerCodeGenerator: OfficerCodeGenerator,
    private val staffTeamRepository: StaffTeamRepository
) {

    companion object {
        private val mutexMap = ConcurrentReferenceHashMap<Long, Any>()
        private fun getMutex(key: Long) {
            mutexMap.compute(key) { _, v -> v ?: Any() }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun create(pa: ProbationArea, team: Team, staffName: Name, startDate: ZonedDateTime): Staff = synchronized(getMutex(pa.id)) {
        val staff = staffRepository.save(
            Staff(
                forename = staffName.forename,
                surname = staffName.surname,
                probationAreaId = pa.id,
                code = officerCodeGenerator.generateFor(pa.code),
                startDate = startDate
            )
        )
        staffTeamRepository.save(StaffTeam(staff.id, team.id))
        return staff
    }

    fun findStaff(probationAreaId: Long, staffName: Name) =
        staffRepository.findTopByProbationAreaIdAndForenameIgnoreCaseAndSurnameIgnoreCase(
            probationAreaId,
            staffName.forename,
            staffName.surname
        )

    fun getStaffByCode(code: String) = staffRepository.getByCode(code)
}
