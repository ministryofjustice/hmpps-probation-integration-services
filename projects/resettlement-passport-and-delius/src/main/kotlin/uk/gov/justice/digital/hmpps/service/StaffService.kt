package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.ConcurrentReferenceHashMap
import uk.gov.justice.digital.hmpps.api.model.Author
import uk.gov.justice.digital.hmpps.entity.*

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
    fun create(pa: ProbationArea, team: Team, author: Author): Staff = synchronized(getMutex(pa.id)) {
        val staff = staffRepository.save(
            Staff(
                forename = author.forename,
                surname = author.surname,
                middleName = null,
                probationAreaId = pa.id,
                code = officerCodeGenerator.generateFor(pa.code)
            )
        )
        staffTeamRepository.save(StaffTeam(staff.id, team.id))
        return staff
    }

    fun findStaff(probationAreaId: Long, author: Author) =
        staffRepository.findTopByProbationAreaIdAndForenameIgnoreCaseAndSurnameIgnoreCase(
            probationAreaId,
            author.forename,
            author.surname
        )
}
