package uk.gov.justice.digital.hmpps.api.controller

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.user.*
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.CASELOAD_PERSON_1
import uk.gov.justice.digital.hmpps.service.UserService
import uk.gov.justice.digital.hmpps.service.toStaffCase
import uk.gov.justice.digital.hmpps.service.toTeamCase

@ExtendWith(MockitoExtension::class)
internal class CaseloadControllerTest {

    @Mock
    lateinit var userService: UserService

    @InjectMocks
    lateinit var controller: CaseloadController

    @Test
    fun `calls get user case load function `() {
        val username = "username"
        val expectedResponse = StaffCaseload(
            totalPages = 1,
            totalElements = 1,
            provider = USER.staff?.provider?.description,
            caseload = listOf(CASELOAD_PERSON_1.toStaffCase()),
            staff = Name(forename = USER.staff!!.forename, surname = USER.staff!!.surname)
        )
        whenever(userService.getUserCaseload(username, PageRequest.of(0, 10))).thenReturn(expectedResponse)
        val res = controller.getUserCaseload(username, 0, 10)
        assertThat(res, equalTo(expectedResponse))
    }

    @Test
    fun `calls get user teams function `() {
        val username = "username"
        val expectedResponse = UserTeam(
            provider = USER.staff?.provider?.description,
            teams = listOf(Team(description = "desc", code = "code"))
        )
        whenever(userService.getUserTeams(username)).thenReturn(expectedResponse)
        val res = controller.getUserTeams(username)
        assertThat(res, equalTo(expectedResponse))
    }

    @Test
    fun `calls get team caseload function `() {
        val teamCode = "teamCode"
        val expectedResponse = TeamCaseload(
            totalPages = 1,
            totalElements = 1,
            provider = USER.staff?.provider?.description,
            caseload = listOf(CASELOAD_PERSON_1.toTeamCase()),
            team = Team(description = "desc", code = "code")
        )
        whenever(userService.getTeamCaseload(teamCode, PageRequest.of(0, 10))).thenReturn(expectedResponse)
        val res = controller.getTeamCaseload(teamCode, 0, 10)
        assertThat(res, equalTo(expectedResponse))
    }

    @Test
    fun `calls get staff in teams function `() {
        val teamCode = "teamCode"
        val expectedResponse = TeamStaff(
            provider = USER.staff?.provider?.description,
            staff = listOf(Staff(code = "code", name = Name(forename = "forename", surname = "surname")))
        )
        whenever(userService.getTeamStaff(teamCode)).thenReturn(expectedResponse)
        val res = controller.getTeamStaff(teamCode)
        assertThat(res, equalTo(expectedResponse))
    }
}