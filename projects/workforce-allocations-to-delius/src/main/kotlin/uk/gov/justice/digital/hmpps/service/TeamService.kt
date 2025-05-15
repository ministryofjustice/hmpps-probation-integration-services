package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamWithDistrictRepository

@Service
class TeamService(
    private val staffRepository: StaffRepository,
    private val ldapService: LdapService,
    private val teamWithDistrictRepository: TeamWithDistrictRepository
) {
    fun getTeams(teamCodes: List<String>) = TeamsResponse(
        teamCodes.associateWith { teamCode ->
            val staff = staffRepository.findActiveStaffInTeam(teamCode)
            val emails = ldapService.findEmailsForStaffIn(staff)
            staff.map { it.toStaffMember(emails[it.user?.username]) }
        }
    )

    fun getAllTeams(): ProbationEstateResponse = ProbationEstateResponse(
        teamWithDistrictRepository.findAll()
            .groupBy { it.district.borough.probationArea }
            .mapNotNull { (provider, providerTeams) ->
                ProviderWithProbationDeliveryUnits(
                    provider.code,
                    provider.description,
                    providerTeams.groupBy { it.district.borough }.map { (borough, boroughTeams) ->
                        ProbationDeliveryUnitWithLocalAdminUnits(
                            borough.code,
                            borough.description,
                            boroughTeams.groupBy { it.district }.map { (district, districtTeams) ->
                                LocalAdminUnitWithTeams(
                                    district.code,
                                    district.description,
                                    districtTeams.map { team -> Team(team.code, team.description) })
                            }
                        )
                    }
                )
            }
    )
}
