package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.exception.NotActiveException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exceptions.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exceptions.StaffNotInTeamException
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamStaffContainer
import uk.gov.justice.digital.hmpps.integrations.delius.provider.verifyTeamMembership
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail

@Component
class AllocationValidator(
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val referenceDataRepository: ReferenceDataRepository
) {

    fun initialValidations(
        providerId: Long,
        allocationDetail: AllocationDetail
    ): TeamStaffContainer {
        val team = teamRepository.findByCode(allocationDetail.teamCode)
            ?: throw NotFoundException("Team", "code", allocationDetail.teamCode)

        if (team.providerId != providerId) {
            throw IgnorableMessageException("External transfer not permitted")
        }

        if (team.endDate != null && team.endDate.isBefore(allocationDetail.createdDate)) {
            throw NotActiveException("Team", "code", team.code)
        }

        val allocationReason = referenceDataRepository.findByDatasetAndCode(
            allocationDetail.datasetCode,
            allocationDetail.code
        ) ?: throw NotFoundException(allocationDetail.datasetCode.value, "code", allocationDetail.code)

        val staff = staffRepository.findByCode(allocationDetail.staffCode)
            ?: throw NotFoundException("Staff", "code", allocationDetail.staffCode)

        if (staff.endDate != null && staff.endDate.isBefore(allocationDetail.createdDate)) {
            throw NotActiveException("Staff", "code", staff.code)
        }
        if (!staffRepository.verifyTeamMembership(staff.id, team.id)) {
            throw StaffNotInTeamException(staff.code, team.code)
        }
        return TeamStaffContainer(team, staff, allocationReason)
    }
}
