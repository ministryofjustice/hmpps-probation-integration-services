package uk.gov.justice.digital.hmpps.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.api.model.Team
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.provider.*
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider

@ExtendWith(MockitoExtension::class)
class TeamServiceTest {
    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var ldapService: LdapService

    @Mock
    lateinit var teamWithDistrictRepository: TeamWithDistrictRepository

    @InjectMocks
    lateinit var teamService: TeamService

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
}
