package uk.gov.justice.digital.hmpps.integrations.delius.managers

import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactContext
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.exceptions.ConflictException
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

abstract class ManagerService<T : ManagerBaseEntity>(
    private val managerRepository: JpaRepository<T, Long>,
    private val contactTypeRepository: ContactTypeRepository
) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    fun updateDateTimes(
        managerActive: T,
        newManager: T
    ): Pair<T, T> {
        newManager.endDate = managerActive.endDate
        managerActive.endDate = newManager.allocationDate
        val managerActiveSaved = managerRepository.save(managerActive)
        val newManagerSaved = managerRepository.save(newManager)
        return Pair(managerActiveSaved, newManagerSaved)
    }

    protected fun createTransferContact(
        oldManager: T,
        newManager: T,
        cci: ContactContext
    ): Contact {
        return Contact(
            type = contactTypeRepository.findByCode(cci.contactTypeCode.value)
                ?: throw ContactTypeNotFoundException(cci.contactTypeCode.value),
            offenderId = cci.offenderId,
            eventId = cci.eventId,
            requirementId = cci.requirementId,
            date = newManager.allocationDate,
            startTime = newManager.allocationDate,
            teamId = newManager.team.id,
            staffId = newManager.staffAllocation.id,
            staffEmployeeId = newManager.staffAllocation.id,
            providerId = newManager.provider.id,
            notes =
            """
        |Transfer Reason: Internal Transfer
        |Transfer Date: ${newManager.allocationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}
        |From Trust: ${newManager.provider.description}
        |From Team: ${oldManager.team.description}
        |From Officer: ${oldManager.staffAllocation.displayName}
        |-------------------------------/${System.lineSeparator()}
        |"""
                .trimMargin()
        )
    }

    protected fun AllocationDetail.isDuplicate(manager: ManagerBaseEntity): Boolean {
        if (staffCode == manager.staffAllocation.code && teamCode == manager.team.code) {
            log.info("Ignoring duplicate allocation request for $this")
            return true
        }

        if (createdDate.truncatedTo(ChronoUnit.SECONDS) == manager.allocationDate.truncatedTo(ChronoUnit.SECONDS)) {
            throw ConflictException("Allocation date conflicts with the current active manager: $this")
        }

        return false
    }
}