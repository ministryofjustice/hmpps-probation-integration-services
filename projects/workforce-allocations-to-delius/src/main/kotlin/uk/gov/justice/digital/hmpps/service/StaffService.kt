package uk.gov.justice.digital.hmpps.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getCaseType
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithTeams
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithTeamsRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.getWithUserByCode
import java.time.LocalDate

@Service
class StaffService(
    private val staffRepository: StaffRepository,
    private val staffWithTeamsRepository: StaffWithTeamsRepository,
    private val ldapService: LdapService,
    private val personRepository: PersonRepository,
    @Value($$"${delius.db.username}") private val dbUsername: String,
) {
    fun getOfficerView(code: String): OfficerView {
        val staff = staffRepository.getWithUserByCode(code)
        return OfficerView(
            code,
            staff.name(),
            staff.grade(),
            ldapService.findEmailForStaff(staff),
            staffRepository.getSentencesDueCountByStaffId(staff.id, LocalDate.now().plusWeeks(4)),
            staffRepository.getKeyDateCountByCodeAndStaffId(staff.id, "EXP", LocalDate.now().plusWeeks(4)),
            staffRepository.getParoleReportsDueCountByStaffId(staff.id, LocalDate.now().plusWeeks(4))
        )
    }

    fun getActiveCases(code: String, crns: List<String>): ActiveCasesResponse {
        val staff = staffRepository.getWithUserByCode(code)
        val initialAllocationDates =
            personRepository.findMostRecentInitialAllocations(crns.toSet(), dbUsername)
                .associate { it.crn to it.allocatedAt?.toLocalDate() }
        val cases = personRepository.findAllByCrnAndSoftDeletedFalse(crns).map {
            Case(
                it.crn,
                it.name(),
                personRepository.getCaseType(it.crn).name,
                initialAllocationDates[it.crn]
            )
        }
        return ActiveCasesResponse(
            code,
            staff.name(),
            staff.grade(),
            ldapService.findEmailForStaff(staff),
            cases
        )
    }

    fun getTeams(code: String) =
        staffWithTeamsRepository.findStaffWithTeamsByCode(code)?.toResponse()
            ?: throw NotFoundException("Staff", "code", code)

    fun getTeamsByUsername(username: String) =
        staffWithTeamsRepository.findStaffWithTeamsByUsername(username)?.toResponse()
            ?: throw NotFoundException("Staff", "username", username)

    private fun StaffWithTeams.toResponse(): StaffTeamsResponse {
        if (endDate != null || user?.endDate != null) throw AccessDeniedException("User or staff expired")
        return StaffTeamsResponse(
            datasets = user?.datasets?.map { Provider(it.code, it.description) },
            teams = teams.map {
                TeamWithLocalAdminUnit(
                    code = it.code,
                    description = it.description,
                    localAdminUnit = LocalAdminUnit(
                        code = it.district.code,
                        description = it.district.description,
                        probationDeliveryUnit = ProbationDeliveryUnit(
                            code = it.district.borough.code,
                            description = it.district.borough.description,
                            provider = Provider(
                                code = it.district.borough.probationArea.code,
                                description = it.district.borough.probationArea.description,
                            )
                        )
                    ),
                )
            }
        )
    }
}
