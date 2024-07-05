package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.util.ConcurrentReferenceHashMap
import uk.gov.justice.digital.hmpps.entity.PrisonStaff
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exceptions.InvalidEstablishmentCodeException
import uk.gov.justice.digital.hmpps.model.StaffName
import uk.gov.justice.digital.hmpps.repository.PrisonProbationAreaRepository
import uk.gov.justice.digital.hmpps.repository.PrisonTeamRepository
import uk.gov.justice.digital.hmpps.retry.retry
import java.time.ZonedDateTime
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class AssignmentService(
    private val probationAreaRepository: PrisonProbationAreaRepository,
    private val teamRepository: PrisonTeamRepository,
    private val staffService: StaffService
) {
    companion object {
        private val mutexMap = ConcurrentReferenceHashMap<Long, ReentrantLock>()
        private fun getMutex(key: Long) = mutexMap.compute(key) { _, v -> v ?: ReentrantLock() }!!
    }

    fun findAssignment(establishmentCode: String, staffName: StaffName): Triple<Long, Long, Long> {
        if (establishmentCode.length < 3) throw InvalidEstablishmentCodeException(establishmentCode)

        val pa = probationAreaRepository.findByInstitutionNomisCode(establishmentCode.substring(0, 3))
            ?: throw NotFoundException(
                "Probation Area not found for NOMIS institution: ${establishmentCode.substring(0, 3)}"
            )
        val team = teamRepository.findByCode("${pa.code}CSN")
            ?: throw NotFoundException("Team", "code", "${pa.code}CSN")
        val staff = getStaff(pa.id, pa.code, team.id, staffName)
        return Triple(pa.id, team.id, staff.id)
    }

    fun getStaff(
        probationAreaId: Long,
        probationAreaCode: String,
        teamId: Long,
        staffName: StaffName,
        allocationDate: ZonedDateTime? = null
    ): PrisonStaff = getMutex(probationAreaId).withLock {
        val findStaff = { staffService.findStaff(probationAreaId, staffName) }
        return retry(3) {
            findStaff() ?: staffService.create(probationAreaId, probationAreaCode, teamId, staffName, allocationDate)
        }
    }
}
