package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.event.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.responsibleofficer.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.responsibleofficer.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getByCodeAndSetName
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getByCodeAndTeamsId
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.getByCodeAndProbationAreaId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class PrisonManagerService(
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val prisonManagerRepository: PrisonManagerRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
) {
    fun allocateToProbationArea(
        disposal: Disposal,
        probationArea: ProbationArea,
        allocationDate: ZonedDateTime
    ) {
        val allStaffTeam = teamRepository.getByCodeAndProbationAreaId("${probationArea.code}ALL", probationArea.id)
        val unallocatedStaff = staffRepository.getByCodeAndTeamsId("${allStaffTeam.code}U", allStaffTeam.id)
        allocateToStaffAndTeam(
            disposal = disposal,
            staff = unallocatedStaff,
            team = allStaffTeam,
            probationArea = probationArea,
            allocationReason = referenceDataRepository.getByCodeAndSetName("AUT", "POM ALLOCATION REASON"),
            allocationDate = allocationDate,
        )
    }

    fun allocateToStaffAndTeam(
        disposal: Disposal,
        staff: Staff,
        team: Team,
        probationArea: ProbationArea,
        allocationReason: ReferenceData,
        allocationDate: ZonedDateTime
    ) {
        val person = disposal.event.person

        // end-date the previous prison manager
        val activePrisonManager = prisonManagerRepository.findActiveManagerAtDate(person.id, allocationDate)
        val activePrisonManagerEndDate = activePrisonManager?.endDate
        if (activePrisonManager != null) {
            activePrisonManager.active = false
            activePrisonManager.endDate = allocationDate
            prisonManagerRepository.saveAndFlush(activePrisonManager)
            contactRepository.save(
                Contact(
                    type = contactTypeRepository.getByCode(ContactTypeCode.PRISON_MANAGER_AUTOMATIC_TRANSFER.code),
                    date = allocationDate,
                    person = person,
                    teamId = team.id,
                    staffId = staff.id,
                    notes = """
                    Transfer Reason: ${allocationReason.description}
                    Transfer Date: ${activePrisonManager.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}
                    From Establishment: ${activePrisonManager.probationArea.description}
                    From Team: ${activePrisonManager.team.description}
                    From Officer: ${activePrisonManager.staff.displayName()}
                    """.trimIndent(),
                )
            )
        }

        // create a new prison manager
        val newPrisonManager = prisonManagerRepository.save(
            PrisonManager(
                date = allocationDate,
                endDate = activePrisonManagerEndDate,
                active = activePrisonManagerEndDate == null,
                allocationReason = allocationReason,
                personId = person.id,
                staff = staff,
                team = team,
                probationArea = probationArea,
            )
        )

        // update Responsible Officer to the new Prison Manager if disposal is longer than 20 months.
        if (disposal.isLongerThan20Months() == true) {
            updateResponsibleOfficer(person, newPrisonManager, allocationDate)
        }
    }

    private fun updateResponsibleOfficer(person: Person, prisonManager: PrisonManager, allocationDate: ZonedDateTime) {
        val activeResponsibleOfficer = responsibleOfficerRepository.findActiveManagerAtDate(person.id, allocationDate)
        val activeResponsibleOfficerEndDate = activeResponsibleOfficer?.endDate

        // end-date previous responsible officer
        if (activeResponsibleOfficer != null) {
            activeResponsibleOfficer.endDate = allocationDate
            // Need to flush changes here to ensure single active RO constraint isn't violated when new RO is added.
            responsibleOfficerRepository.saveAndFlush(activeResponsibleOfficer)
        }

        // create new responsible officer
        val newResponsibleOfficer = responsibleOfficerRepository.save(
            ResponsibleOfficer(
                personId = person.id,
                prisonManager = prisonManager,
                startDate = allocationDate,
                endDate = activeResponsibleOfficerEndDate,
            )
        )

        // create contact with change details
        contactRepository.save(
            Contact(
                type = contactTypeRepository.getByCode(ContactTypeCode.RESPONSIBLE_OFFICER_CHANGE.code),
                date = allocationDate,
                person = person,
                staffId = prisonManager.staff.id,
                teamId = prisonManager.team.id,
                notes = generateResponsibleOfficerChangeNotes(activeResponsibleOfficer, newResponsibleOfficer),
            )
        )
    }

    private fun generateResponsibleOfficerChangeNotes(
        oldResponsibleOfficer: ResponsibleOfficer?,
        newResponsibleOfficer: ResponsibleOfficer
    ): String {
        var notes = "New Details:\n${newResponsibleOfficer.stringDetails()}"
        oldResponsibleOfficer?.stringDetails()?.let {
            notes += "\n\nPrevious Details:\n$it"
        }
        return notes
    }
}
