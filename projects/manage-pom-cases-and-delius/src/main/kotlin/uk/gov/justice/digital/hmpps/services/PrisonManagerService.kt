package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.PrisonManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.getByNomisCdeCode
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.pomAllocationReason
import uk.gov.justice.digital.hmpps.integrations.managepomcases.PomAllocation
import uk.gov.justice.digital.hmpps.retry.retry
import java.time.ZonedDateTime

@Service
class PrisonManagerService(
    private val staffService: StaffService,
    private val probationAreaRepository: ProbationAreaRepository,
    private val teamRepository: TeamRepository,
    private val prisonManagerRepository: PrisonManagerRepository,
    private val referenceDataRepository: ReferenceDataRepository
) {

    @Transactional
    fun allocatePrisonManager(personId: Long, allocationDate: ZonedDateTime, allocation: PomAllocation) {
        val probationArea = probationAreaRepository.getByNomisCdeCode(allocation.prison.code)
        val team = teamRepository.getByCode(probationArea.code + Team.POM_SUFFIX)
        val staff = getStaff(probationArea, team, allocation.manager, allocationDate)
        val currentPom = prisonManagerRepository.findActiveManagerAtDate(personId, allocationDate)?.apply {
            endDate = allocationDate
        }
        val allocationReason = referenceDataRepository.pomAllocationReason(
            when {
                currentPom == null -> "AUT"
                currentPom.probationArea.id == probationArea.id -> "INA"
                else -> "EXT"
            }
        )

        val newPom = PrisonManager(
            personId = personId,
            probationArea = probationArea,
            allocationReason = allocationReason,
            date = allocationDate,
            staff = staff,
            team = team
        )
        prisonManagerRepository.save(newPom)
    }

    private fun getStaff(probationArea: ProbationArea, team: Team, staffName: Name, allocationDate: ZonedDateTime): Staff {
        val findStaff = { staffService.findStaff(probationArea.id, staffName) }
        return retry(3) {
            findStaff() ?: staffService.create(probationArea, team, staffName, allocationDate)
        }
    }
}
