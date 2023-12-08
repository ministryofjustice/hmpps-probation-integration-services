package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.PrisonCaseNoteGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exceptions.InvalidEstablishmentCodeException
import uk.gov.justice.digital.hmpps.integrations.delius.model.StaffName
import uk.gov.justice.digital.hmpps.integrations.delius.repository.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.TeamRepository

@ExtendWith(MockitoExtension::class)
class AssignmentServiceTest {
    @Mock
    lateinit var probationAreaRepository: ProbationAreaRepository

    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var staffService: StaffService

    @InjectMocks
    lateinit var assignmentService: AssignmentService

    private val staffName = StaffName("forename", "surname")

    @Test
    fun `validate establishment code length`() {
        assertThrows<InvalidEstablishmentCodeException> {
            assignmentService.findAssignment("PA", staffName)
        }
    }

    @Test
    fun `unable to find probation area`() {
        whenever(probationAreaRepository.findByInstitutionNomisCode(PrisonCaseNoteGenerator.EXISTING_IN_BOTH.locationId)).thenReturn(
            null,
        )
        val ex =
            assertThrows<NotFoundException> {
                assignmentService.findAssignment(PrisonCaseNoteGenerator.EXISTING_IN_BOTH.locationId, staffName)
            }
        assertThat(
            ex.message,
            equalTo("Probation Area not found for NOMIS institution: ${PrisonCaseNoteGenerator.EXISTING_IN_BOTH.locationId}"),
        )
    }

    @Test
    fun `unable to find team`() {
        whenever(probationAreaRepository.findByInstitutionNomisCode(PrisonCaseNoteGenerator.EXISTING_IN_BOTH.locationId))
            .thenReturn(ProbationAreaGenerator.DEFAULT)
        whenever(teamRepository.findByCode(TeamGenerator.DEFAULT.code)).thenReturn(null)
        val ex =
            assertThrows<NotFoundException> {
                assignmentService.findAssignment(PrisonCaseNoteGenerator.EXISTING_IN_BOTH.locationId, staffName)
            }
        assertThat(
            ex.message,
            equalTo("Team with code of ${TeamGenerator.DEFAULT.code} not found"),
        )
    }

    @Test
    fun `find staff successfully`() {
        val probationArea = ProbationAreaGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT
        val staff = StaffGenerator.DEFAULT

        whenever(probationAreaRepository.findByInstitutionNomisCode(PrisonCaseNoteGenerator.NEW_TO_DELIUS.locationId))
            .thenReturn(probationArea)
        whenever(teamRepository.findByCode(team.code)).thenReturn(team)
        whenever(staffService.findStaff(probationArea.id, staffName)).thenReturn(staff)

        val res = assignmentService.findAssignment(PrisonCaseNoteGenerator.NEW_TO_DELIUS.locationId, staffName)

        verify(staffService).findStaff(probationArea.id, staffName)
        assertThat(res.first, equalTo(probationArea.id))
        assertThat(res.second, equalTo(team.id))
        assertThat(res.third, equalTo(staff.id))
    }

    @Test
    fun `staff not found and create new`() {
        val probationArea = ProbationAreaGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT
        val newStaffCode = "C12A001"
        val newStaff = StaffGenerator.generate(newStaffCode, staffName.forename, staffName.surname)

        whenever(probationAreaRepository.findByInstitutionNomisCode(PrisonCaseNoteGenerator.NEW_TO_DELIUS.locationId))
            .thenReturn(probationArea)
        whenever(teamRepository.findByCode(team.code)).thenReturn(team)
        whenever(staffService.findStaff(probationArea.id, staffName)).thenReturn(null)
        whenever(staffService.create(probationArea, team, staffName))
            .thenReturn(newStaff)

        val res = assignmentService.findAssignment(PrisonCaseNoteGenerator.NEW_TO_DELIUS.locationId, staffName)

        verify(staffService).create(probationArea, team, staffName)
        assertThat(res.first, equalTo(probationArea.id))
        assertThat(res.second, equalTo(team.id))
        assertThat(res.third, equalTo(newStaff.id))
    }

    @Test
    fun `retry find when exception thrown creating a new staff`() {
        val probationArea = ProbationAreaGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT
        val newStaffCode = "C12A001"
        val newStaff = StaffGenerator.generate(newStaffCode, staffName.forename, staffName.surname)

        whenever(probationAreaRepository.findByInstitutionNomisCode(PrisonCaseNoteGenerator.NEW_TO_DELIUS.locationId))
            .thenReturn(probationArea)
        whenever(teamRepository.findByCode(team.code)).thenReturn(team)
        whenever(staffService.findStaff(probationArea.id, staffName))
            .thenReturn(null)
            .thenReturn(newStaff)
        whenever(staffService.create(probationArea, team, staffName))
            .thenThrow(RuntimeException())

        val res = assignmentService.findAssignment(PrisonCaseNoteGenerator.NEW_TO_DELIUS.locationId, staffName)

        verify(staffService).create(probationArea, team, staffName)
        verify(staffService, times(2)).findStaff(probationArea.id, staffName)
        assertThat(res.first, equalTo(probationArea.id))
        assertThat(res.second, equalTo(team.id))
        assertThat(res.third, equalTo(newStaff.id))
    }

    @Test
    fun `throws exception if second find fails`() {
        val probationArea = ProbationAreaGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT

        whenever(probationAreaRepository.findByInstitutionNomisCode(PrisonCaseNoteGenerator.NEW_TO_DELIUS.locationId))
            .thenReturn(probationArea)
        whenever(teamRepository.findByCode(team.code)).thenReturn(team)
        whenever(staffService.findStaff(probationArea.id, staffName))
            .thenReturn(null)
        whenever(staffService.create(probationArea, team, staffName))
            .thenThrow(NotFoundException("Staff not found"))

        assertThrows<NotFoundException> {
            assignmentService.findAssignment(
                PrisonCaseNoteGenerator.NEW_TO_DELIUS.locationId,
                staffName,
            )
        }
    }
}
