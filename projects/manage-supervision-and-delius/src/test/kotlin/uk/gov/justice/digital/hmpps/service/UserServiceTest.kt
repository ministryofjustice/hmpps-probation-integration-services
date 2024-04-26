package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_STAFF
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.STAFF_1
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.CASELOAD_PERSON_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.CaseloadRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.UserRepository

@ExtendWith(MockitoExtension::class)
internal class UserServiceTest {

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var caseloadRepository: CaseloadRepository

    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var teamRepository: TeamRepository

    @InjectMocks
    lateinit var service: UserService

    @Test
    fun `calls get person activity function`() {
        val username = "username"
        whenever(userRepository.findByUsername(username)).thenReturn(USER)
        whenever(caseloadRepository.findByStaffCode(USER.staff!!.code)).thenReturn(listOf(CASELOAD_PERSON_1))
        val res = service.getUserCaseload(username)
        assertThat(
            res.provider, equalTo(DEFAULT_PROVIDER.description)
        )
    }

    @Test
    fun `calls get user teams function`() {
        val username = "username"
        whenever(userRepository.findByUsername(username)).thenReturn(USER)
        whenever(staffRepository.findTeamsByStaffCode(USER.staff!!.code)).thenReturn(listOf(DEFAULT_TEAM))
        val res = service.getUserTeams(username)
        assertThat(
            res.provider, equalTo(DEFAULT_PROVIDER.description)
        )
        assertThat(
            res.teams[0].description, equalTo(DEFAULT_TEAM.description)
        )
        assertThat(
            res.teams[0].code, equalTo(DEFAULT_TEAM.code)
        )
    }

    @Test
    fun `calls get team staff function`() {
        val teamCode = DEFAULT_TEAM.code
        whenever(teamRepository.findProviderByTeamCode(teamCode)).thenReturn(DEFAULT_PROVIDER.description)
        whenever(teamRepository.findStaffByTeamCode(teamCode)).thenReturn(listOf(DEFAULT_STAFF, STAFF_1))
        val res = service.getTeamStaff(teamCode)
        assertThat(
            res.provider, equalTo(DEFAULT_PROVIDER.description)
        )
        assertThat(
            res.staff[0].code.trim(), equalTo(DEFAULT_STAFF.code.trim())
        )
        assertThat(
            res.staff[1].code, equalTo(STAFF_1.code)
        )
    }

    @Test
    fun `calls get team caseload function`() {
        val teamCode = DEFAULT_TEAM.code
        whenever(teamRepository.findByTeamCode(teamCode)).thenReturn(DEFAULT_TEAM)
        whenever(caseloadRepository.findByTeamCode(teamCode)).thenReturn(listOf(CASELOAD_PERSON_1))
        val res = service.getTeamCaseload(teamCode)
        assertThat(res.provider, equalTo(DEFAULT_PROVIDER.description))
        assertThat(res.caseload[0].staff.code, equalTo(CASELOAD_PERSON_1.staff.code))
        assertThat(res.team.code, equalTo(DEFAULT_TEAM.code))
    }

    @Test
    fun `calls get caseload for staff function`() {
        val staffCode = DEFAULT_STAFF.code
        whenever(staffRepository.findByStaffCode(staffCode)).thenReturn(DEFAULT_STAFF)
        whenever(caseloadRepository.findByStaffCode(staffCode)).thenReturn(listOf(CASELOAD_PERSON_1))
        val res = service.getStaffCaseload(staffCode)
        assertThat(
            res.provider, equalTo(DEFAULT_PROVIDER.description)
        )
        assertThat(
            res.caseload[0].crn, equalTo(OVERVIEW.crn)
        )
        assertThat(
            res.caseload[0].caseName.forename, equalTo(OVERVIEW.forename)
        )
    }
}