package uk.gov.justice.digital.hmpps.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.api.model.Team
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffUserGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator.AUDIT_USER
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.CaseloadRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.MostRecentInitialAllocation
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Borough
import uk.gov.justice.digital.hmpps.integrations.delius.provider.District
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamWithDistrict
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamWithDistrictRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class TeamServiceTest {
    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var ldapService: LdapService

    @Mock
    lateinit var teamWithDistrictRepository: TeamWithDistrictRepository

    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var caseloadRepository: CaseloadRepository

    @Mock
    lateinit var personRepository: PersonRepository

    lateinit var teamService: TeamService

    @BeforeEach
    fun setUp() {
        teamService = TeamService(
            staffRepository,
            ldapService,
            teamWithDistrictRepository,
            teamRepository,
            caseloadRepository,
            personRepository,
            AUDIT_USER.username,
        )
    }

    @Test
    fun `get all teams`() {
        val provider1 = Provider(IdGenerator.getAndIncrement(), "P1", "Provider One")
        val provider2 = Provider(IdGenerator.getAndIncrement(), "P2", "Provider Two")
        val borough1 = Borough(IdGenerator.getAndIncrement(), "B1", "Borough One", provider1)
        val borough2 = Borough(IdGenerator.getAndIncrement(), "B2", "Borough Two", provider2)
        val district1 = District(IdGenerator.getAndIncrement(), "D1", "District One", borough1)
        val district2 = District(IdGenerator.getAndIncrement(), "D2", "District Two", borough1)
        val district3 = District(IdGenerator.getAndIncrement(), "D3", "District Three", borough2)
        val team1 = TeamWithDistrict(IdGenerator.getAndIncrement(), "T1", "Team One", district1)
        val team2 = TeamWithDistrict(IdGenerator.getAndIncrement(), "T2", "Team Two", district1)
        val team3 = TeamWithDistrict(IdGenerator.getAndIncrement(), "T3", "Team Three", district2)
        val team4 = TeamWithDistrict(IdGenerator.getAndIncrement(), "T4", "Team Four", district3)
        whenever(teamWithDistrictRepository.findAll()).thenReturn(listOf(team1, team2, team3, team4))

        val result = teamService.getAllTeams()

        val expected = ProbationEstateResponse(
            providers = listOf(
                ProviderWithProbationDeliveryUnits(
                    code = "P1",
                    description = "Provider One",
                    probationDeliveryUnits = listOf(
                        ProbationDeliveryUnitWithLocalAdminUnits(
                            code = "B1",
                            description = "Borough One",
                            localAdminUnits = listOf(
                                LocalAdminUnitWithTeams(
                                    code = "D1",
                                    description = "District One",
                                    teams = listOf(
                                        Team("T1", "Team One"),
                                        Team("T2", "Team Two")
                                    )
                                ),
                                LocalAdminUnitWithTeams(
                                    code = "D2",
                                    description = "District Two",
                                    teams = listOf(
                                        Team("T3", "Team Three")
                                    )
                                )
                            )
                        )
                    )
                ),
                ProviderWithProbationDeliveryUnits(
                    code = "P2",
                    description = "Provider Two",
                    probationDeliveryUnits = listOf(
                        ProbationDeliveryUnitWithLocalAdminUnits(
                            code = "B2",
                            description = "Borough Two",
                            localAdminUnits = listOf(
                                LocalAdminUnitWithTeams(
                                    code = "D3",
                                    description = "District Three",
                                    teams = listOf(
                                        Team("T4", "Team Four")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `get team active cases`() {
        val team = TeamGenerator.ALLOCATION_TEAM
        val activeStaff1 = StaffGenerator.generateStaffWithUser(
            code = "N02ABS1",
            forename = "Joe",
            surname = "Bloggs",
            teams = listOf(team),
            user = StaffUserGenerator.generate("joe.bloggs")
        )
        val activeStaff2 = StaffGenerator.generateStaffWithUser(
            code = "N02ABS2",
            forename = "Jane",
            surname = "Smith",
            teams = listOf(team),
            user = StaffUserGenerator.generate("jane.smith")
        )
        val caseloadStaff1 = StaffGenerator.generateStaff(
            code = activeStaff1.code,
            forename = activeStaff1.forename,
            surname = activeStaff1.surname,
            teams = listOf(team),
            id = activeStaff1.id
        )
        val caseloadStaff2 = StaffGenerator.generateStaff(
            code = activeStaff2.code,
            forename = activeStaff2.forename,
            surname = activeStaff2.surname,
            teams = listOf(team),
            id = activeStaff2.id
        )
        val person1 = PersonGenerator.generate("X111111")
        val person2 = PersonGenerator.generate("X222222")

        whenever(teamRepository.findByCode(team.code)).thenReturn(team)
        whenever(staffRepository.findActiveStaffInTeam(team.code)).thenReturn(listOf(activeStaff1, activeStaff2))
        whenever(ldapService.findEmailsForStaffIn(listOf(activeStaff1, activeStaff2))).thenReturn(
            mapOf(
                activeStaff1.user!!.username to "joe.bloggs@example.com",
                activeStaff2.user!!.username to "jane.smith@example.com"
            )
        )
        whenever(caseloadRepository.findAllByStaffCodeIn(listOf(activeStaff1.code, activeStaff2.code))).thenReturn(
            listOf(
                CaseloadGenerator.generate(person = person1, staff = caseloadStaff1, team = team),
                CaseloadGenerator.generate(person = person2, staff = caseloadStaff1, team = team),
                CaseloadGenerator.generate(person = person2, staff = caseloadStaff2, team = team)
            )
        )
        whenever(
            personRepository.findMostRecentInitialAllocations(setOf(person1.crn, person2.crn), AUDIT_USER.username)
        ).thenReturn(
            listOf(initialAllocation(person1.crn, LocalDateTime.of(2022, 6, 24, 0, 0)))
        )
        whenever(personRepository.findCaseTypes(setOf(person1.crn, person2.crn))).thenReturn(
            listOf(
                caseTypeByCrn(person1.crn, CaseType.CUSTODY.name),
                caseTypeByCrn(person2.crn, CaseType.COMMUNITY.name)
            )
        )
        whenever(personRepository.findAllByCrnAndSoftDeletedFalse(listOf(person1.crn, person2.crn))).thenReturn(
            listOf(person1, person2)
        )

        val response = teamService.getActiveCases(team.code)

        assertThat(response.code).isEqualTo(team.code)
        assertThat(response.description).isEqualTo(team.description)
        assertThat(response.staff).hasSize(2)
        assertThat(response.staff[0].code).isEqualTo(activeStaff1.code)
        assertThat(response.staff[0].email).isEqualTo("joe.bloggs@example.com")
        assertThat(response.staff[0].cases).hasSize(2)
        assertThat(response.staff[0].cases[0].crn).isEqualTo(person1.crn)
        assertThat(response.staff[0].cases[0].type).isEqualTo(CaseType.CUSTODY.name)
        assertThat(response.staff[0].cases[0].initialAllocationDate).isEqualTo(LocalDate.of(2022, 6, 24))
        assertThat(response.staff[0].cases[1].crn).isEqualTo(person2.crn)
        assertThat(response.staff[0].cases[1].type).isEqualTo(CaseType.COMMUNITY.name)
        assertThat(response.staff[1].code).isEqualTo(activeStaff2.code)
        assertThat(response.staff[1].cases).hasSize(1)
        assertThat(response.staff[1].cases[0].crn).isEqualTo(person2.crn)
    }

    private fun caseTypeByCrn(caseCrn: String, caseType: String) =
        object : uk.gov.justice.digital.hmpps.integrations.delius.person.CaseTypeByCrn {
            override val crn: String = caseCrn
            override val type: String = caseType
        }

    private fun initialAllocation(crnValue: String, allocatedAtValue: LocalDateTime?) =
        object : MostRecentInitialAllocation {
            override val crn: String = crnValue
            override val allocatedAt: LocalDateTime? = allocatedAtValue
        }
}
