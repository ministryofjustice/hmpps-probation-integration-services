package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.CaseAccessList
import uk.gov.justice.digital.hmpps.api.model.User
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.ExclusionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.RestrictionRepository

@Service
class UserService(
    private val personRepository: PersonRepository,
    private val exclusionRepository: ExclusionRepository,
    private val restrictionRepository: RestrictionRepository,
    private val staffRepository: StaffRepository,
    private val ldapService: LdapService,
) {
    fun getAllAccessLimitations(crn: String, staffCodesFilter: List<String>? = null): CaseAccessList {
        val person = personRepository.findByCrnAndSoftDeletedFalse(crn) ?: throw NotFoundException("Person", "crn", crn)
        val exclusions = exclusionRepository.findByPersonId(person.id).map { it.user.username }
        val restrictions = restrictionRepository.findByPersonId(person.id).map { it.user.username }
        val staffCodes = staffRepository.findStaffForUsernamesIn(exclusions + restrictions)
            .associate { it.user?.username to it.code }
        return CaseAccessList(
            crn = crn,
            exclusionMessage = person.exclusionMessage,
            restrictionMessage = person.restrictionMessage,
            excludedFrom = exclusions.map { User(it, staffCodes[it]) }
                .filter { staffCodesFilter == null || it.staffCode in staffCodesFilter },
            restrictedTo = restrictions.map { User(it, staffCodes[it]) }
                .filter { staffCodesFilter == null || it.staffCode in staffCodesFilter },
        )
    }

    fun findAllUsersWithRole(role: String = "MAABT001"): List<User> {
        val usernames = ldapService.findAllUsersWithRole(role)
        val staff = staffRepository.findStaffForUsernamesIn(usernames)
        return staff.map { User(it.user!!.username, it.code) }
    }
}

