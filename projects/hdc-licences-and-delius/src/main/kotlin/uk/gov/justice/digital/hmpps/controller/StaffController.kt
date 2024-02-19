package uk.gov.justice.digital.hmpps.controller

import org.springframework.ldap.core.LdapTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.entity.CommunityManagerEntity
import uk.gov.justice.digital.hmpps.entity.StaffEntity
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername
import uk.gov.justice.digital.hmpps.model.CommunityManager
import uk.gov.justice.digital.hmpps.model.LocalAdminUnit
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.model.Provider
import uk.gov.justice.digital.hmpps.model.Staff
import uk.gov.justice.digital.hmpps.model.Team
import uk.gov.justice.digital.hmpps.model.TeamDetails
import uk.gov.justice.digital.hmpps.repository.CommunityManagerRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.repository.StaffRepository

@RestController
@PreAuthorize("hasRole('PROBATION_API__HDC__STAFF')")
class StaffController(
    private val staffRepository: StaffRepository,
    private val communityManagerRepository: CommunityManagerRepository,
    private val personRepository: PersonRepository,
    private val ldapTemplate: LdapTemplate
) {
    @GetMapping("/staff/{code}")
    fun getStaffByCode(@PathVariable code: String) = staffRepository.findByCode(code)?.toModel()
        ?: throw NotFoundException("Staff", "code", code)

    @GetMapping("/staff", params = ["username"])
    fun getStaffByUsername(@RequestParam username: String) = staffRepository.findByUserUsername(username)?.toModel()
        ?: throw NotFoundException("Staff", "username", username)

    @GetMapping("/staff", params = ["id"])
    @Deprecated("Use `/staff/{code}` or `/staff?username={username}`")
    fun getStaffById(@RequestParam id: Long) = staffRepository.findStaffById(id)?.toModel()
        ?: throw NotFoundException("Staff", "staffId", id)

    @GetMapping("/staff/{code}/managedPrisonerIds")
    fun getManagedPrisonersByStaffCode(@PathVariable code: String) =
        personRepository.findManagedPrisonerIdentifiersByStaffCode(code)

    @GetMapping("/managedPrisonerIds", params = ["staffId"])
    @Deprecated("Use `/staff/{code}/managedPrisonerIds`")
    fun getManagedPrisonersByStaffId(@RequestParam staffId: Long) =
        personRepository.findManagedPrisonerIdentifiersByStaffId(staffId)

    @GetMapping("/case/{nomsNumber}/communityManager")
    fun getCommunityManager(@PathVariable nomsNumber: String) =
        communityManagerRepository.findCommunityManager(nomsNumber)?.toModel()
            ?: throw NotFoundException("Community manager for case", "nomsNumber", nomsNumber)

    private fun StaffEntity.toModel() = Staff(
        code = code,
        staffId = id,
        name = Name(forenames(), surname),
        teams = teams.map { team ->
            TeamDetails(
                code = team.code,
                description = team.description,
                telephone = team.telephone,
                emailAddress = team.emailAddress,
                probationDeliveryUnit = ProbationDeliveryUnit(
                    team.district.borough.code,
                    team.district.borough.description
                ),
                localAdminUnit = LocalAdminUnit(team.district.code, team.district.description)
            )
        },
        username = user?.username,
        email = user?.username?.let { username -> ldapTemplate.findEmailByUsername(username) }
    )

    private fun CommunityManagerEntity.toModel() = CommunityManager(
        code = staff.code,
        staffId = staff.id,
        name = Name(staff.forenames(), staff.surname),
        team = Team(team.code, team.description),
        localAdminUnit = LocalAdminUnit(team.district.code, team.district.description),
        provider = Provider(team.probationArea.code, team.probationArea.description),
        isUnallocated = staff.code.endsWith("U")
    )
}
