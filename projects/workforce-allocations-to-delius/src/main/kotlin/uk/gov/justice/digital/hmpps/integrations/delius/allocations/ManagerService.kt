package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ManagerBaseEntity
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactContext
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

abstract class ManagerService<T : ManagerBaseEntity>(
    auditedInteractionService: AuditedInteractionService,
    private val managerRepository: JpaRepository<T, Long>,
) : AuditableService(auditedInteractionService) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    fun updateDateTimes(
        managerActive: T,
        newManager: T,
    ): Pair<T, T> {
        newManager.endDate = managerActive.endDate
        managerActive.endDate = newManager.startDate
        val managerActiveSaved = managerRepository.save(managerActive)
        val newManagerSaved = managerRepository.save(newManager)
        return Pair(managerActiveSaved, newManagerSaved)
    }

    protected fun createTransferContact(
        oldManager: T,
        newManager: T,
        cci: ContactContext,
    ): Contact {
        return Contact(
            type = cci.contactType,
            personId = cci.offenderId,
            eventId = cci.eventId,
            requirementId = cci.requirementId,
            date = newManager.startDate.toLocalDate(),
            startTime = newManager.startDate,
            teamId = newManager.team.id,
            staffId = newManager.staff.id,
            providerId = newManager.provider.id,
            notes =
                """
        |Transfer Reason: Internal Transfer
        |Transfer Date: ${newManager.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}
        |From Trust: ${newManager.provider.description}
        |From Team: ${oldManager.team.description}
        |From Officer: ${oldManager.staff.displayName}
        |-------------------------------/${System.lineSeparator()}
        |"""
                    .trimMargin(),
        )
    }

    protected fun AllocationDetail.isDuplicate(manager: ManagerBaseEntity): Boolean {
        if (staffCode == manager.staff.code && teamCode == manager.team.code) {
            log.info("Ignoring duplicate allocation request for $this")
            return true
        }

        if (createdDate.truncatedTo(ChronoUnit.SECONDS) == manager.startDate.truncatedTo(ChronoUnit.SECONDS)) {
            throw ConflictException("Allocation date conflicts with the current active manager: $this")
        }

        return false
    }
}
