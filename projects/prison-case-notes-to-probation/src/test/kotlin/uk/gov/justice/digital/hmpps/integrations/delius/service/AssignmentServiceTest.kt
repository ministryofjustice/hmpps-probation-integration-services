package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.NomisCaseNoteGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.exceptions.InvalidEstablishmentCodeException
import uk.gov.justice.digital.hmpps.exceptions.ProbationAreaNotFoundException
import uk.gov.justice.digital.hmpps.exceptions.TeamNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.entity.StaffTeam
import uk.gov.justice.digital.hmpps.integrations.delius.model.StaffName
import uk.gov.justice.digital.hmpps.integrations.delius.repository.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.StaffTeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.TeamRepository

@ExtendWith(MockitoExtension::class)
class AssignmentServiceTest {

    @Mock
    lateinit var probationAreaRepository: ProbationAreaRepository

    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var officerCodeGenerator: OfficerCodeGenerator

    @Mock
    lateinit var userService: UserService

    @Mock
    lateinit var staffTeamRepository: StaffTeamRepository

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
        whenever(probationAreaRepository.findByInstitutionNomisCode(NomisCaseNoteGenerator.EXISTING_IN_BOTH.locationId)).thenReturn(
            null
        )
        val ex = assertThrows<ProbationAreaNotFoundException> {
            assignmentService.findAssignment(NomisCaseNoteGenerator.EXISTING_IN_BOTH.locationId, staffName)
        }
        assertThat(
            ex.message,
            equalTo("Probation area not found for NOMIS institution: ${NomisCaseNoteGenerator.EXISTING_IN_BOTH.locationId}")
        )
    }

    @Test
    fun `unable to find team`() {
        whenever(probationAreaRepository.findByInstitutionNomisCode(NomisCaseNoteGenerator.EXISTING_IN_BOTH.locationId))
            .thenReturn(ProbationAreaGenerator.DEFAULT)
        whenever(teamRepository.findByCode(TeamGenerator.DEFAULT.code)).thenReturn(null)
        val ex = assertThrows<TeamNotFoundException> {
            assignmentService.findAssignment(NomisCaseNoteGenerator.EXISTING_IN_BOTH.locationId, staffName)
        }
        assertThat(
            ex.message,
            equalTo("Team not found: ${TeamGenerator.DEFAULT.code}")
        )
    }

    @Test
    fun `staff not found and create new`() {
        val staff = StaffGenerator.DEFAULT
        val probationArea = ProbationAreaGenerator.DEFAULT
        val newStaffCode = "C12A001"
        whenever(probationAreaRepository.findByInstitutionNomisCode(NomisCaseNoteGenerator.EXISTING_IN_BOTH.locationId))
            .thenReturn(ProbationAreaGenerator.DEFAULT)
        whenever(teamRepository.findByCode(TeamGenerator.DEFAULT.code)).thenReturn(TeamGenerator.DEFAULT)
        whenever(
            staffRepository.findTopByProbationAreaIdAndForenameIgnoreCaseAndSurnameIgnoreCase(
                staff.probationAreaId,
                staff.forename,
                staff.surname
            )
        ).thenReturn(null)

        whenever(userService.findServiceUser()).thenReturn(UserGenerator.APPLICATION_USER)
        whenever(officerCodeGenerator.generateFor(probationArea.code)).thenReturn("C12A001")
        whenever(staffRepository.save(any())).thenReturn(StaffGenerator.generate("C12A001"))

        val staffCaptor = ArgumentCaptor.forClass(Staff::class.java)

        assignmentService.findAssignment(NomisCaseNoteGenerator.EXISTING_IN_BOTH.locationId, staffName)

        verify(staffRepository, Mockito.times(1)).save(staffCaptor.capture())
        assertThat(staffCaptor.value.forename, equalTo(staffName.forename))
        assertThat(staffCaptor.value.surname, equalTo(staffName.surname))
        assertThat(staffCaptor.value.probationAreaId, equalTo(staff.probationAreaId))
        assertThat(staffCaptor.value.code, equalTo("C12A001"))
        val staffTeamCaptor = ArgumentCaptor.forClass(StaffTeam::class.java)
        verify(staffTeamRepository, Mockito.times(1)).save(staffTeamCaptor.capture())
        assertThat(staffTeamCaptor.value.staffId, equalTo(staffCaptor.value.id))
        assertThat(staffTeamCaptor.value.teamId, equalTo(TeamGenerator.DEFAULT.id))
    }
}
