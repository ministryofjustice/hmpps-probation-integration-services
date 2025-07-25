package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.api.model.user.DefaultUserDetails
import uk.gov.justice.digital.hmpps.api.model.user.UserProviderResponse
import uk.gov.justice.digital.hmpps.aspect.DeliusUserAspect
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_STAFF
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.STAFF_1
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.PROVIDER_2
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.PROVIDER_3
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_USER_1
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.TEAM_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.CASELOAD_PERSON_1
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.StaffAndRole
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.StaffUserRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.*
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class UserServiceTest {

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var caseloadRepository: CaseloadRepository

    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var staffUserRepository: StaffUserRepository

    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var userAccessService: UserAccessService

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var probationAreaUserRepository: ProbationAreaUserRepository

    @Mock
    lateinit var userAspect: DeliusUserAspect

    @Mock
    lateinit var ldapTemplate: LdapTemplate

    @InjectMocks
    lateinit var service: UserService

    @Test
    fun `calls get person activity function`() {
        val username = "username"
        whenever(userAccessService.userAccessFor(anyString(), anyList())).thenReturn(UserAccess(emptyList()))
        whenever(userRepository.findByUsername(username)).thenReturn(USER)
        whenever(caseloadRepository.findByStaffId(USER.staff!!.id, Pageable.ofSize(1))).thenReturn(
            PageImpl(
                listOf(
                    CASELOAD_PERSON_1
                )
            )
        )
        val res = service.getUserCaseload(username, Pageable.ofSize(1))
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
        whenever(caseloadRepository.findByTeamCode(teamCode, Pageable.ofSize(1))).thenReturn(
            PageImpl(
                listOf(
                    CASELOAD_PERSON_1
                )
            )
        )
        val res = service.getTeamCaseload(teamCode, Pageable.ofSize(1))
        assertThat(res.provider, equalTo(DEFAULT_PROVIDER.description))
        assertThat(res.caseload[0].staff.code, equalTo(CASELOAD_PERSON_1.staff.code))
        assertThat(res.team.code, equalTo(DEFAULT_TEAM.code))
    }

    @Test
    fun `get user providers without query parameters`() {

        val probationAreaUsers = listOf(
            OffenderManagerGenerator.PAU_USER_RECORD1,
            OffenderManagerGenerator.PAU_USER_RECORD2,
            OffenderManagerGenerator.PAU_USER_RECORD3,
            OffenderManagerGenerator.PAU_USER_RECORD4,
        )

        val staffRole = StaffRole("username", "surname", "forename", "role")

        val teams = listOf(
            Team(1, "t01", "team1", listOf(DEFAULT_STAFF, STAFF_1), DEFAULT_PROVIDER, LocalDate.now()),
            Team(2, "t02", "team2", listOf(DEFAULT_STAFF, STAFF_1), DEFAULT_PROVIDER, LocalDate.now()),
            DEFAULT_TEAM
        )
        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenReturn(listOf(OffenderManagerGenerator.PAU_USER_RECORD1.id.provider.code))
            .thenReturn(listOf("7"))

        whenever(probationAreaUserRepository.findByUsername(STAFF_USER_1.username)).thenReturn(probationAreaUsers)
        whenever(teamRepository.findTeamById(7)).thenReturn(DEFAULT_TEAM)
        whenever(teamRepository.findByProviderCode(OffenderManagerGenerator.PAU_USER_RECORD1.id.provider.code)).thenReturn(
            teams
        )
        whenever(staffUserRepository.findStaffByTeam(DEFAULT_TEAM.code)).thenReturn(listOf(staffRole))

        val expected = UserProviderResponse(
            DefaultUserDetails(STAFF_USER_1.username, DEFAULT_PROVIDER.description, DEFAULT_TEAM.description),
            probationAreaUsers.map { it.toProvider() },
            teams.map { it.toTeam() },
            listOf(staffRole.toUser())
        )
        val response = service.getProvidersForUser(STAFF_USER_1.username)

        assertEquals(expected, response)
    }

    @Test
    fun `get user providers without query parameters no ldap team`() {
        val probationAreaUsers = listOf(
            OffenderManagerGenerator.PAU_USER_RECORD1,
            OffenderManagerGenerator.PAU_USER_RECORD2,
            OffenderManagerGenerator.PAU_USER_RECORD3,
            OffenderManagerGenerator.PAU_USER_RECORD4,
        )

        val staffRole = StaffRole("username", "surname", "forename", "role")

        val teams = listOf(
            Team(1, "t01", "team1", listOf(DEFAULT_STAFF, STAFF_1), DEFAULT_PROVIDER, LocalDate.now()),
            Team(2, "t02", "team2", listOf(DEFAULT_STAFF, STAFF_1), DEFAULT_PROVIDER, LocalDate.now()),
            DEFAULT_TEAM
        )

        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenReturn(listOf(OffenderManagerGenerator.PAU_USER_RECORD1.id.provider.code))

        whenever(probationAreaUserRepository.findByUsername(STAFF_USER_1.username)).thenReturn(probationAreaUsers)
        whenever(teamRepository.findByProviderCode(OffenderManagerGenerator.PAU_USER_RECORD1.id.provider.code)).thenReturn(
            teams
        )
        whenever(
            teamRepository.findTeamsByUsernameAndProviderCode(
                STAFF_USER_1.username,
                OffenderManagerGenerator.PAU_USER_RECORD1.id.provider.code
            )
        ).thenReturn(teams)
        whenever(staffUserRepository.findStaffByTeam(teams[0].code)).thenReturn(listOf(staffRole))

        val expected = UserProviderResponse(
            DefaultUserDetails(STAFF_USER_1.username, DEFAULT_PROVIDER.description, teams[0].description),
            probationAreaUsers.map { it.toProvider() },
            teams.map { it.toTeam() },
            listOf(staffRole.toUser())
        )
        val response = service.getProvidersForUser(STAFF_USER_1.username)

        assertEquals(expected, response)
    }

    @Test
    fun `get user providers with region and team query parameters`() {

        val probationAreaUsers = listOf(
            OffenderManagerGenerator.PAU_USER_RECORD1
        )

        val staffRole = StaffRole("username", "surname", "forename", "role")
        val teams = listOf(
            Team(1, "t01", "team1", listOf(DEFAULT_STAFF, STAFF_1), DEFAULT_PROVIDER, LocalDate.now())
        )

        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenReturn(listOf(OffenderManagerGenerator.PAU_USER_RECORD1.id.provider.code))
            .thenReturn(listOf("7"))

        whenever(probationAreaUserRepository.findByUsername(STAFF_USER_1.username)).thenReturn(probationAreaUsers)
        whenever(teamRepository.findByProviderCode(PROVIDER_2.code)).thenReturn(teams)
        whenever(staffUserRepository.findStaffByTeam(TEAM_1.code)).thenReturn(listOf(staffRole))

        whenever(teamRepository.findTeamById(7)).thenReturn(DEFAULT_TEAM)
        val expected = UserProviderResponse(
            DefaultUserDetails(STAFF_USER_1.username, DEFAULT_PROVIDER.description, DEFAULT_TEAM.description),
            probationAreaUsers.map { it.toProvider() },
            teams.map { it.toTeam() },
            listOf(staffRole.toUser())
        )
        val response = service.getProvidersForUser(STAFF_USER_1.username, PROVIDER_2.code, TEAM_1.code)

        assertEquals(expected, response)
    }

    @Test
    fun `get user providers with region query parameter`() {

        val probationAreaUsers = listOf(
            OffenderManagerGenerator.PAU_USER_RECORD1
        )

        val staffRole = StaffRole("username", "surname", "forename", "role")
        val teams = listOf(
            Team(1, "t01", "team1", listOf(DEFAULT_STAFF, STAFF_1), DEFAULT_PROVIDER, LocalDate.now())
        )

        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenReturn(listOf(OffenderManagerGenerator.PAU_USER_RECORD1.id.provider.code))

        whenever(probationAreaUserRepository.findByUsername(STAFF_USER_1.username)).thenReturn(probationAreaUsers)
        whenever(teamRepository.findByProviderCode(PROVIDER_3.code)).thenReturn(teams)
        whenever(staffUserRepository.findStaffByTeam("t01")).thenReturn(listOf(staffRole))
        whenever(
            teamRepository.findTeamsByUsernameAndProviderCode(
                STAFF_USER_1.username,
                OffenderManagerGenerator.PAU_USER_RECORD1.id.provider.code
            )
        ).thenReturn(emptyList())

        val expected = UserProviderResponse(
            DefaultUserDetails(STAFF_USER_1.username, DEFAULT_PROVIDER.description, null),
            probationAreaUsers.map { it.toProvider() },
            teams.map { it.toTeam() },
            listOf(staffRole.toUser())
        )
        val response = service.getProvidersForUser(STAFF_USER_1.username, PROVIDER_3.code)

        assertEquals(expected, response)
    }

    data class StaffRole(
        val _username: String,
        val _surname: String,
        val _forename: String,
        val _role: String,
    ) : StaffAndRole {
        override val username: String
            get() = _username
        override val surname: String
            get() = _surname
        override val forename: String
            get() = _forename
        override val role: String
            get() = _role
    }
}