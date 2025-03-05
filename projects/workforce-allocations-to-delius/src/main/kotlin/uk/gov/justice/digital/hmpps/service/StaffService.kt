package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getCaseType
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.getWithUserByCode
import java.time.LocalDate

@Service
class StaffService(
    private val staffRepository: StaffRepository,
    private val ldapService: LdapService,
    private val personRepository: PersonRepository
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
        val cases = personRepository.findAllByCrnAndSoftDeletedFalse(crns).map {
            Case(
                it.crn,
                it.name(),
                personRepository.getCaseType(it.crn).name
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

    fun getTeams(code: String) = StaffTeamsResponse(
        staffRepository.findStaffWithTeamsByCode(code)?.teams?.map { team ->
            TeamWithLocalAdminUnit(
                code = team.code,
                description = team.description,
                localAdminUnit = LocalAdminUnit(
                    code = team.district.code,
                    description = team.district.description,
                    probationDeliveryUnit = ProbationDeliveryUnit(
                        code = team.district.borough.code,
                        description = team.district.borough.description,
                        provider = Provider(
                            code = team.district.borough.probationArea.code,
                            description = team.district.borough.probationArea.description,
                        )
                    )
                ),
            )
        } ?: throw NotFoundException("Staff", "code", code)
    )
}
