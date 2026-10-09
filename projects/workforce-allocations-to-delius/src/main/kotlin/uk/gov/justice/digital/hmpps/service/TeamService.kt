package uk.gov.justice.digital.hmpps.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.CaseloadRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
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
    private val personRepository: PersonRepository,
    @Value("\${delius.db.username}") private val dbUsername: String,
) {
    companion object {
        private const val ORACLE_IN_CLAUSE_BATCH_SIZE = 999
        private const val CASE_TYPE_BATCH_SIZE = 50
    }

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
        val caseloadsByStaffCode = caseloadRepository.findAllByStaffCodeIn(staff.map { it.code }).groupBy { it.staff.code }
        val crnSet = caseloadsByStaffCode.values.flatten().map { it.person.crn }.toSet()

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
            val initialAllocationDates = crnSet.chunkedForOracleInClause()
                .flatMap { personRepository.findMostRecentInitialAllocations(it, dbUsername) }
                .associate { it.crn to it.allocatedAt?.toLocalDate() }
            val caseTypes = crnSet.chunkedForCaseTypeInClause()
                .flatMap { personRepository.findCaseTypes(it) }
                .associate { it.crn to it.type }
            val peopleByCrn = crnSet.chunkedForOracleInClause()
                .flatMap { personRepository.findAllByCrnAndSoftDeletedFalse(it.toList()) }
                .associateBy { it.crn }

            staff.sortedBy { it.code }.map { activeStaff ->
                val cases = caseloadsByStaffCode[activeStaff.code]
                    .orEmpty()
                    .map { it.person.crn }
                    .distinct()
                    .sorted()
                    .mapNotNull { crn ->
                        peopleByCrn[crn]?.let {
                            Case(
                                crn,
                                it.name(),
                                caseTypes[crn] ?: CaseType.UNKNOWN.name,
                                initialAllocationDates[crn]
                            )
                        }
                    }

                ActiveCasesResponse(
                    activeStaff.code,
                    activeStaff.name(),
                    activeStaff.grade(),
                    emails[activeStaff.user?.username],
                    cases
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

    private fun Set<String>.chunkedForOracleInClause() =
        toList().chunked(ORACLE_IN_CLAUSE_BATCH_SIZE).map { it.toSet() }

    private fun Set<String>.chunkedForCaseTypeInClause() =
        toList().chunked(CASE_TYPE_BATCH_SIZE).map { it.toSet() }
}
