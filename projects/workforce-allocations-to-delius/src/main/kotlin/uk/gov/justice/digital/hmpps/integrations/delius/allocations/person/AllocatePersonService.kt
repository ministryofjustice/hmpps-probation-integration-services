package uk.gov.justice.digital.hmpps.integrations.delius.allocations.person

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.exceptions.PersonManagerNotFoundException
import uk.gov.justice.digital.hmpps.exceptions.PersonNotFoundException
import uk.gov.justice.digital.hmpps.exceptions.ResponsibleOfficerNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationRequestValidator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactContext
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.findByCodeOrThrow
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.PersonAllocationDetail

@Service
class AllocatePersonService(
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val allocationRequestValidator: AllocationRequestValidator,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,

) : ManagerService<PersonManager>(personManagerRepository) {

    @Transactional
    fun createPersonAllocation(allocationDetail: PersonAllocationDetail) {
        val person = personRepository.findByCrn(allocationDetail.crn)
            ?: throw PersonNotFoundException(allocationDetail.crn)

        // TODO: Auditing

        val activeOffenderManager =
            personManagerRepository.findActiveManagerAtDate(person.id, allocationDetail.createdDate)
                ?: throw PersonManagerNotFoundException(allocationDetail.crn, allocationDetail.createdDate)

        if (allocationDetail.isDuplicate(activeOffenderManager)) {
            return
        }

        allocationRequestValidator.hasNoPendingTransfers(person)
        val ts = allocationRequestValidator.initialValidations(
            activeOffenderManager.provider.id,
            allocationDetail,
        )

        val newOffenderManager = PersonManager(personId = person.id).apply {
            populate(allocationDetail.createdDate, ts, activeOffenderManager)
        }

        val (activeOm, newOm) = updateDateTimes(activeOffenderManager, newOffenderManager)

        updateResponsibleOfficer(newOm)
        contactRepository.save(
            createTransferContact(
                activeOm,
                newOm,
                ContactContext(
                    contactTypeRepository.findByCodeOrThrow(ContactTypeCode.OFFENDER_MANAGER_TRANSFER.value),
                    person.id
                )
            )
        )

        personRepository.updateIaps(person.id)
    }

    private fun updateResponsibleOfficer(
        newPersonManager: PersonManager,
    ) {
        val activeResponsibleOfficer = responsibleOfficerRepository
            .findActiveManagerAtDate(newPersonManager.personId, newPersonManager.startDate)
            ?: throw ResponsibleOfficerNotFoundException(newPersonManager.personId, newPersonManager.startDate)

        val newResponsibleOfficer = ResponsibleOfficer(
            personId = newPersonManager.personId,
            startDate = newPersonManager.startDate,
            endDate = newPersonManager.endDate,
            communityManager = newPersonManager
        )
        activeResponsibleOfficer.endDate = newPersonManager.startDate

        // Need to flush changes here to ensure single active RO constraint isn't violated when new RO is added.
        responsibleOfficerRepository.saveAndFlush(activeResponsibleOfficer)
        responsibleOfficerRepository.save(newResponsibleOfficer)
        if (newResponsibleOfficer.communityManager != null) {
            createResponsibleOfficerInternalTransferContact(
                newResponsibleOfficer,
                activeResponsibleOfficer
            )
        }
    }

    private fun createResponsibleOfficerInternalTransferContact(
        newResponsibleOfficer: ResponsibleOfficer,
        oldResponsibleOfficer: ResponsibleOfficer
    ) {
        val newCommunityOffenderManager = newResponsibleOfficer.communityManager!!
        val sc = Contact(
            type = contactTypeRepository.findByCodeOrThrow(ContactTypeCode.RESPONSIBLE_OFFICER_CHANGE.value),
            personId = newResponsibleOfficer.personId,
            date = newResponsibleOfficer.startDate,
            startTime = newResponsibleOfficer.startDate,
            providerId = newCommunityOffenderManager.provider.id,
            teamId = newCommunityOffenderManager.team.id,
            staffId = newCommunityOffenderManager.staff.id,
            notes = generateRoContactNotes(oldResponsibleOfficer, newResponsibleOfficer)
        )
        contactRepository.save(sc)
    }

    private fun generateRoContactNotes(
        oldResponsibleOfficer: ResponsibleOfficer,
        newResponsibleOfficer: ResponsibleOfficer
    ): String {
        return """
      |New Details:
      |${newResponsibleOfficer.stringDetails()}
      |Previous Details:
      |${oldResponsibleOfficer.stringDetails()}
      |Allocation Reason: ${newResponsibleOfficer.communityManager!!.allocationReason.description}
    """.trimMargin()
    }

    private fun ResponsibleOfficer.stringDetails(): String {
        val communityManager = communityManager
        val prisonManager = prisonManager
        var string = ""
        if (communityManager != null) {
            string += """
        |Responsible Officer Type: Offender Manager
        |Responsible Officer: ${communityManager.staff.displayName}(${communityManager.team.description},${communityManager.provider.description})
      """.trimMargin()
        }
        if (prisonManager != null) {
            string += """
        |Responsible Officer Type: Prison Offender Manager
        |Responsible Officer: ${prisonManager.staff.displayName}(${prisonManager.team.description},${prisonManager.provider.description})
      """.trimMargin()
        }
        string += """
      |
      |Start Date: ${DeliusDateTimeFormatter.format(startDate)}
      """.trimMargin()

        if (endDate != null) {
            string += """
        |
        |End Date: ${DeliusDateTimeFormatter.format(endDate)}
        """.trimMargin()
        }

        return string
    }
}
