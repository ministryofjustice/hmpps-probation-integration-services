package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.exceptions.ReferenceDataNotFound
import uk.gov.justice.digital.hmpps.exceptions.StaffNotActiveException
import uk.gov.justice.digital.hmpps.exceptions.StaffNotFoundException
import uk.gov.justice.digital.hmpps.exceptions.StaffNotInTeamException
import uk.gov.justice.digital.hmpps.exceptions.TeamNotActiveException
import uk.gov.justice.digital.hmpps.exceptions.TeamNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamStaffContainer
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail

@Component
class AllocationRequestValidator(
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val referenceDataRepository: ReferenceDataRepository
) {

    fun initialValidations(
        providerId: Long,
        allocationDetail: AllocationDetail,
    ): TeamStaffContainer {
        val team = teamRepository.findByCodeAndProviderId(allocationDetail.teamCode, providerId)
            ?: throw TeamNotFoundException(allocationDetail.teamCode)

        if (team.endDate != null && team.endDate.isBefore(allocationDetail.createdDate)) {
            throw TeamNotActiveException(team.code)
        }

        val allocationReason = referenceDataRepository.findByDatasetAndCode(
            allocationDetail.datasetCode, allocationDetail.code
        ) ?: throw ReferenceDataNotFound(allocationDetail.datasetCode.value, allocationDetail.code)

        val staff = staffRepository.findByCode(allocationDetail.staffCode)
            ?: throw StaffNotFoundException(allocationDetail.staffCode)

        if (staff.endDate != null && staff.endDate.isBefore(allocationDetail.createdDate)) {
            throw StaffNotActiveException(staff.code)
        }
        if (!staffRepository.verifyTeamMembership(staff.id, team.id)) {
            throw StaffNotInTeamException(staff.code, team.code)
        }
        return TeamStaffContainer(team, staff, allocationReason)
    }

    fun hasNoPendingTransfers(person: Person) {
//        val pendingStatus = referenceDataRepository.findPendingTransfer()
//        val pendingTransfers = offender.transfers.filter { it.transferStatus.id == pendingStatus.id }
//        if (pendingTransfers.isNotEmpty()) throw ConflictException("Pending transfer exists for this offender: ${offender.crn}")
    }

    fun hasNoPendingTransfers(event: Event) {
//        val pendingStatus = referenceDataService.getReferenceData(WellKnownTransferStatus.PENDING_TRANSFER)
//        val pendingTransfers = event.transfers.filter { it.transferStatus.id == pendingStatus.id }
//        if (pendingTransfers.isNotEmpty()) throw ConflictException("Pending transfer exists for this event: ${event.id}")
    }

    fun hasNoPendingTransfers(requirement: Requirement) {
//        val pendingStatus = referenceDataService.getReferenceData(WellKnownTransferStatus.PENDING_TRANSFER)
//        val pendingTransfers = requirement.transfers.filter { it.transferStatus.id == pendingStatus.id }
//        if (pendingTransfers.isNotEmpty()) throw ConflictException("Pending transfer exists for this requirement: ${requirement.id}")
    }
}
