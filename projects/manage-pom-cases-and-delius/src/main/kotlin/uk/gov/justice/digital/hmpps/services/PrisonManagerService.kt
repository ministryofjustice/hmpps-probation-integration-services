package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.entity.PrisonStaff
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.pomAllocationReason
import uk.gov.justice.digital.hmpps.integrations.managepomcases.PomAllocation
import uk.gov.justice.digital.hmpps.model.StaffName
import uk.gov.justice.digital.hmpps.service.AssignmentService
import java.time.ZonedDateTime

@Service
class PrisonManagerService(
    private val probationAreaRepository: ProbationAreaRepository,
    private val teamRepository: TeamRepository,
    private val assignmentService: AssignmentService,
    private val staffRepository: StaffRepository,
    private val prisonManagerRepository: PrisonManagerRepository,
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val contactService: ContactService,
    private val personRepository: PersonRepository
) {

    @Transactional
    fun allocatePrisonManager(
        personId: Long,
        allocationDate: ZonedDateTime,
        allocation: PomAllocation
    ): PomAllocationResult {
        val probationArea = probationAreaRepository.getByNomisCdeCode(allocation.prison.code)
        val team = teamRepository.getByCode(probationArea.code + Team.POM_SUFFIX)
        val staff = assignmentService.getStaff(
            probationArea.id,
            probationArea.code,
            team.id,
            allocation.manager.name.asStaffName(),
            allocationDate
        )
        personRepository.findForUpdate(personId)
        val currentPom = prisonManagerRepository.findActiveManagerAtDate(personId, allocationDate)
        val newPom = currentPom.changeTo(personId, allocationDate, probationArea, team, staff)
        deleteFuturePrisonManagers(allocationDate, newPom)
        return newPom?.let { new ->
            currentPom?.let { old -> prisonManagerRepository.saveAndFlush(old) }
            new.emailAddress = allocation.manager.email
            prisonManagerRepository.save(new)
            PomAllocationResult.PomAllocated
        } ?: PomAllocationResult.NoPomChange
    }

    @Transactional
    fun deallocatePrisonManager(personId: Long, deallocationDate: ZonedDateTime): PomAllocationResult {
        val currentPom = prisonManagerRepository.findActiveManagerAtDate(personId, deallocationDate)
        return if (currentPom?.isUnallocated() == false) {
            val probationArea = currentPom.probationArea
            val team = teamRepository.getByCode(probationArea.code + Team.UNALLOCATED_SUFFIX)
            val staff = staffRepository.getByCode(team.code + "U")
            val newPom = currentPom.changeTo(personId, deallocationDate, probationArea, team, staff.toPrisonStaff())!!
            deleteFuturePrisonManagers(deallocationDate, newPom)
            prisonManagerRepository.saveAndFlush(currentPom)
            prisonManagerRepository.save(newPom)
            PomAllocationResult.PomDeallocated
        } else {
            PomAllocationResult.NoPomChange
        }
    }

    private fun PrisonManager.hasChanged(probationArea: ProbationArea, team: Team, staff: PrisonStaff) =
        this.probationArea.id != probationArea.id || this.team.id != team.id || this.staff.id != staff.id

    fun PrisonManager.allocationNotes() =
        """
        |Transfer Reason: ${allocationReason.description}
        |Transfer Date: ${DeliusDateFormatter.format(date)}
        """.trimMargin()

    fun PrisonManager.transferNotes() =
        """
        |
        |From Establishment Provider: ${probationArea.description}
        |From Team: ${team.description}
        |From Officer: ${staff.surname}, ${staff.forename}
        """.trimMargin()

    fun PrisonManager.responsibleOfficerDetails() = listOfNotNull(
        "Responsible Officer Type: Prison Offender Manager",
        "Responsible Officer: ${staff.surname}, ${staff.forename} (${team.description}, ${probationArea.description})",
        "Start Date: ${DeliusDateTimeFormatter.format(date)}",
        endDate?.let { "End Date: " + DeliusDateTimeFormatter.format(it) },
        "Allocation Reason: ${allocationReason.description}"
    ).joinToString(System.lineSeparator())

    private fun deleteFuturePrisonManagers(allocationDate: ZonedDateTime, pom: PrisonManager?) {
        val futurePoms = prisonManagerRepository.findAllByDateGreaterThan(allocationDate)
        futurePoms.flatMap { it.responsibleOfficers }.forEach { ro ->
            if (ro.isActive() && pom != null) {
                ro.apply {
                    pom.responsibleOfficers.add(this)
                    prisonManager = pom
                }
            } else {
                responsibleOfficerRepository.delete(ro)
                null
            }
        }
        prisonManagerRepository.deleteAll(futurePoms)
    }

    private fun PrisonManager?.changeTo(
        personId: Long,
        dateTime: ZonedDateTime,
        probationArea: ProbationArea,
        team: Team,
        staff: PrisonStaff
    ) = if (this?.hasChanged(probationArea, team, staff) != false) {
        val allocationReasonCode =
            when {
                this == null || staff.isUnallocated() -> PrisonManager.AllocationReasonCode.AUTO
                this.probationArea.id == probationArea.id -> PrisonManager.AllocationReasonCode.INTERNAL
                else -> PrisonManager.AllocationReasonCode.EXTERNAL
            }
        val newPom = PrisonManager(
            personId = personId,
            probationArea = probationArea,
            allocationReason = referenceDataRepository.pomAllocationReason(allocationReasonCode.value),
            date = dateTime,
            staff = staff.toStaff(),
            team = team,
            responsibleOfficers = mutableListOf()
        )
        val notes = newPom.allocationNotes() + (this?.transferNotes() ?: "")
        contactService.createContact(personId, allocationReasonCode.ctc, dateTime, newPom, notes)
        this?.apply {
            responsibleOfficer()?.also {
                newPom.makeResponsibleOfficer()
                contactService.createContact(
                    personId,
                    ContactType.Code.RESPONSIBLE_OFFICER_CHANGE,
                    dateTime,
                    newPom,
                    """
                    |New Details:
                    |${newPom.responsibleOfficerDetails()}
                    |
                    |Previous Details:
                    |${this.responsibleOfficerDetails()}
                    """.trimMargin()
                )
            }
            endDate = dateTime
        }
        newPom
    } else {
        null
    }
}

fun Name.asStaffName() = StaffName(forename, surname)
enum class PomAllocationResult {
    PomAllocated, PomDeallocated, NoPomChange
}

fun PrisonStaff.toStaff() =
    Staff(id = id, code = code, forename = forename, surname = surname, probationAreaId = probationAreaId)

fun Staff.toPrisonStaff() =
    PrisonStaff(id = id, code = code, forename = forename, surname = surname, probationAreaId = probationAreaId)