package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison

import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.AdditionalIdentifierRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.entity.PrisonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.entity.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.entity.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getByCodeAndSetName
import uk.gov.justice.digital.hmpps.integrations.delius.staff.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.staff.entity.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.entity.getByCodeAndTeamsId
import uk.gov.justice.digital.hmpps.integrations.delius.team.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.team.entity.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.entity.getByCodeAndProbationAreaId
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
    private val additionalIdentifierRepository: AdditionalIdentifierRepository,
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
            allocationDate = allocationDate
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

        val activePrisonManager: PrisonManager?
        try {
            activePrisonManager = prisonManagerRepository.findActiveManagerAtDate(person.id, allocationDate)
        } catch (e: IncorrectResultSizeDataAccessException) {
            if (!additionalIdentifierRepository.personHasBeenMerged(person.id)) {
                throw e
            }
            //where crn has a merged from record
            //we can ignore the IncorrectResultSizeDataAccessException and
            //process the record no further
            return
        }

        // end-date the previous prison manager
        if (activePrisonManager?.probationArea?.id == probationArea.id) return
        val activePrisonManagerEndDate = activePrisonManager?.endDate
            ?: prisonManagerRepository.findFirstManagerAfterDate(person.id, allocationDate).singleOrNull()?.date
        if (activePrisonManager != null) {
            activePrisonManager.active = false
            activePrisonManager.endDate = allocationDate
            prisonManagerRepository.saveAndFlush(activePrisonManager)
            contactRepository.save(
                Contact(
                    type = contactTypeRepository.getByCode(ContactType.Code.PRISON_MANAGER_AUTOMATIC_TRANSFER.value),
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
                    """.trimIndent()
                )
            )
        }

        prisonManagerRepository.save(
            PrisonManager(
                date = allocationDate,
                endDate = activePrisonManagerEndDate,
                active = activePrisonManagerEndDate == null,
                allocationReason = allocationReason,
                personId = person.id,
                staff = staff,
                team = team,
                probationArea = probationArea
            )
        )
    }
}
