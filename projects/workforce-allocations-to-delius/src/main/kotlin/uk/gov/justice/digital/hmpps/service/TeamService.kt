package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.CaseloadRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamWithDistrictRepository

@Service
class TeamService(
    private val staffRepository: StaffRepository,
    private val ldapService: LdapService,
    private val teamWithDistrictRepository: TeamWithDistrictRepository,
    private val teamRepository: TeamRepository,
    private val caseloadRepository: CaseloadRepository,
    private val activeCaseService: ActiveCaseService,
) {
    fun getTeams(teamCodes: List<String>) = TeamsResponse(
        teamCodes.associateWith { teamCode ->
            val staff = staffRepository.findActiveStaffInTeam(teamCode)
            val emails = ldapService.findEmailsForStaffIn(staff)
            staff.map { it.toStaffMember(emails[it.user?.username]) }
        }
    )

    fun getActiveCases(teamCode: String): TeamActiveCasesResponse {
        val team = teamRepository.findByCode(teamCode) ?: throw NotFoundException("Team", "code", teamCode)
        val staff = staffRepository.findActiveStaffInTeam(teamCode)
        if (staff.isEmpty()) {
            return TeamActiveCasesResponse(team.code, team.description, emptyList())
        }

        val emails = ldapService.findEmailsForStaffIn(staff)
        val crnsByStaffCode =
            caseloadRepository.findAllByStaffCodeIn(staff.map { it.code }).groupBy { it.staff.code }
                .mapValues { (_, caseloads) -> caseloads.map { it.person.crn }.distinct().sorted() }
        val crnSet = crnsByStaffCode.values.flatten().toSet()

        val staffResponses = if (crnSet.isEmpty()) {
            staff.map { activeStaff ->
                ActiveCasesResponse(
                    activeStaff.code,
                    activeStaff.name(),
                    activeStaff.grade(),
                    emails[activeStaff.user?.username],
                    emptyList()
                )
            }
        } else {
            val casesByStaffCode = activeCaseService.getCasesByGroup(crnsByStaffCode)

            staff.sortedBy { it.code }.map { activeStaff ->
                ActiveCasesResponse(
                    activeStaff.code,
                    activeStaff.name(),
                    activeStaff.grade(),
                    emails[activeStaff.user?.username],
                    casesByStaffCode[activeStaff.code].orEmpty()
                )
            }
        }

        return TeamActiveCasesResponse(team.code, team.description, staffResponses)
    }

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
