package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.ConcurrentReferenceHashMap
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.entity.StaffTeam
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.model.StaffName
import uk.gov.justice.digital.hmpps.integrations.delius.repository.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.StaffTeamRepository

@Service
class StaffService(
    private val staffRepository: StaffRepository,
    private val officerCodeGenerator: OfficerCodeGenerator,
    private val staffTeamRepository: StaffTeamRepository,
) {
    companion object {
        private val mutexMap = ConcurrentReferenceHashMap<Long, Any>()

        private fun getMutex(key: Long) {
            mutexMap.compute(key) { _, v -> v ?: Any() }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun create(
        pa: ProbationArea,
        team: Team,
        staffName: StaffName,
    ): Staff =
        synchronized(getMutex(pa.id)) {
            val staff =
                staffRepository.save(
                    Staff(
                        forename = staffName.forename,
                        surname = staffName.surname,
                        probationAreaId = pa.id,
                        code = officerCodeGenerator.generateFor(pa.code),
                    ),
                )
            staffTeamRepository.save(StaffTeam(staff.id, team.id))
            return staff
        }

    fun findStaff(
        probationAreaId: Long,
        staffName: StaffName,
    ) =
        staffRepository.findTopByProbationAreaIdAndForenameIgnoreCaseAndSurnameIgnoreCase(
            probationAreaId,
            staffName.forename,
            staffName.surname,
        )
}
