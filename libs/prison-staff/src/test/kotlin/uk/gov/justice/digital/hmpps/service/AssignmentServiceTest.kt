package uk.gov.justice.digital.hmpps.service

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
import uk.gov.justice.digital.hmpps.entity.Prison
import uk.gov.justice.digital.hmpps.entity.PrisonStaff
import uk.gov.justice.digital.hmpps.entity.PrisonTeam
import uk.gov.justice.digital.hmpps.entity.Provider
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exceptions.InvalidEstablishmentCodeException
import uk.gov.justice.digital.hmpps.model.StaffName
import uk.gov.justice.digital.hmpps.repository.PrisonProbationAreaRepository
import uk.gov.justice.digital.hmpps.repository.PrisonTeamRepository

@ExtendWith(MockitoExtension::class)
class AssignmentServiceTest {

    @Mock
    lateinit var probationAreaRepository: PrisonProbationAreaRepository

    @Mock
    lateinit var teamRepository: PrisonTeamRepository

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
        whenever(probationAreaRepository.findByInstitutionNomisCode("LEI")).thenReturn(
            null
        )
        val ex = assertThrows<NotFoundException> {
            assignmentService.findAssignment("LEI", staffName)
        }
        assertThat(
            ex.message,
            equalTo("Probation Area not found for NOMIS institution: LEI")
        )
    }

    @Test
    fun `unable to find team`() {
        whenever(probationAreaRepository.findByInstitutionNomisCode("LEI"))
            .thenReturn(ProbationAreaGenerator.DEFAULT)
        whenever(teamRepository.findByCode(TeamGenerator.DEFAULT.code)).thenReturn(null)
        val ex = assertThrows<NotFoundException> {
            assignmentService.findAssignment("LEI", staffName)
        }
        assertThat(
            ex.message,
            equalTo("Team with code of ${TeamGenerator.DEFAULT.code} not found")
        )
    }

    @Test
    fun `find staff successfully`() {
        val probationArea = ProbationAreaGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT
        val staff = StaffGenerator.DEFAULT

        whenever(probationAreaRepository.findByInstitutionNomisCode("LEI"))
            .thenReturn(probationArea)
        whenever(teamRepository.findByCode(team.code)).thenReturn(team)
        whenever(staffService.findStaff(probationArea.id, staffName)).thenReturn(staff)

        val res = assignmentService.findAssignment("LEI", staffName)

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
        val newStaff = PrisonStaff(
            code = newStaffCode,
            forename = staffName.forename,
            surname = staffName.surname,
            probationAreaId = probationArea.id
        )

        whenever(probationAreaRepository.findByInstitutionNomisCode("LEI"))
            .thenReturn(probationArea)
        whenever(teamRepository.findByCode(team.code)).thenReturn(team)
        whenever(staffService.findStaff(probationArea.id, staffName)).thenReturn(null)
        whenever(staffService.create(probationArea.id, probationArea.code, team.id, staffName, null))
            .thenReturn(newStaff)

        val res = assignmentService.findAssignment("LEI", staffName)

        verify(staffService).create(probationArea.id, probationArea.code, team.id, staffName)
        assertThat(res.first, equalTo(probationArea.id))
        assertThat(res.second, equalTo(team.id))
        assertThat(res.third, equalTo(newStaff.id))
    }

    @Test
    fun `retry find when exception thrown creating a new staff`() {
        val probationArea = ProbationAreaGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT
        val newStaffCode = "C12A001"
        val newStaff = PrisonStaff(
            code = newStaffCode,
            forename = staffName.forename,
            surname = staffName.surname,
            probationAreaId = probationArea.id
        )

        whenever(probationAreaRepository.findByInstitutionNomisCode("LEI"))
            .thenReturn(probationArea)
        whenever(teamRepository.findByCode(team.code)).thenReturn(team)
        whenever(staffService.findStaff(probationArea.id, staffName))
            .thenReturn(null)
            .thenReturn(newStaff)
        whenever(staffService.create(probationArea.id, probationArea.code, team.id, staffName))
            .thenThrow(RuntimeException())

        val res = assignmentService.findAssignment("LEI", staffName)

        verify(staffService).create(probationArea.id, probationArea.code, team.id, staffName)
        verify(staffService, times(2)).findStaff(probationArea.id, staffName)
        assertThat(res.first, equalTo(probationArea.id))
        assertThat(res.second, equalTo(team.id))
        assertThat(res.third, equalTo(newStaff.id))
    }

    @Test
    fun `throws exception if second find fails`() {
        val probationArea = ProbationAreaGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT

        whenever(probationAreaRepository.findByInstitutionNomisCode("LEI"))
            .thenReturn(probationArea)
        whenever(teamRepository.findByCode(team.code)).thenReturn(team)
        whenever(staffService.findStaff(probationArea.id, staffName))
            .thenReturn(null)
        whenever(staffService.create(probationArea.id, probationArea.code, team.id, staffName))
            .thenThrow(NotFoundException("Staff not found"))

        assertThrows<NotFoundException> {
            assignmentService.findAssignment(
                "LEI",
                staffName
            )
        }
    }
}

object ProbationAreaGenerator {
    val DEFAULT = Provider(
        1,
        "PA1",
        Prison(2, "LEI")
    )
}

object TeamGenerator {
    val DEFAULT = PrisonTeam(3, "${ProbationAreaGenerator.DEFAULT.code}CSN")
}

object StaffGenerator {
    val DEFAULT = PrisonStaff(
        4,
        "Bob",
        "Smith",
        "${ProbationAreaGenerator.DEFAULT.code}A999",
        ProbationAreaGenerator.DEFAULT.id
    )
}

