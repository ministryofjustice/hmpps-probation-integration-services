package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.entity.StaffTeam
import uk.gov.justice.digital.hmpps.integrations.delius.model.StaffName
import uk.gov.justice.digital.hmpps.integrations.delius.repository.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.StaffTeamRepository

@ExtendWith(MockitoExtension::class)
class StaffServiceTest {
    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var officerCodeGenerator: OfficerCodeGenerator

    @Mock
    lateinit var staffTeamRepository: StaffTeamRepository

    @InjectMocks
    lateinit var staffService: StaffService

    @Test
    fun `create new staff and team`() {
        val probationArea = ProbationAreaGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT
        val newStaffCode = "C12A001"
        val staffName = StaffName("forename", "surname")

        whenever(officerCodeGenerator.generateFor(probationArea.code)).thenReturn(newStaffCode)
        whenever(staffRepository.save(any(Staff::class.java))).thenAnswer { it.arguments[0] }

        val newStaff = staffService.create(probationArea, team, staffName)

        verify(staffRepository).save(any(Staff::class.java))
        assertThat(newStaff.forename, equalTo(staffName.forename))
        assertThat(newStaff.surname, equalTo(staffName.surname))
        assertThat(newStaff.probationAreaId, equalTo(probationArea.id))
        assertThat(newStaff.code, equalTo(newStaffCode))

        val staffTeamCaptor = ArgumentCaptor.forClass(StaffTeam::class.java)
        verify(staffTeamRepository).save(staffTeamCaptor.capture())
        assertThat(staffTeamCaptor.value.staffId, equalTo(newStaff.id))
        assertThat(staffTeamCaptor.value.teamId, equalTo(team.id))
    }
}
