package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.UserDetails
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.UserAccessRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.details.UserDetailsRepository
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername

@Service
class UserService(
    private val userDetailsRepository: UserDetailsRepository,
    private val userAccessRepository: UserAccessRepository,
    private val ldapTemplate: LdapTemplate
) {
    fun checkUserAccess(username: String, crn: String) = if (userAccessRepository.existsByCrn(crn)) {
        userAccessRepository.findByUsernameAndCrn(username, crn)
    } else {
        throw NotFoundException("Person", "crn", crn)
    }

    fun getUserDetails(username: String) = userDetailsRepository.findByUsername(username)?.let {
        UserDetails(
            name = Name(it.forename, it.middleName, it.surname),
            username = it.username,
            staffCode = it.staff?.code,
            email = ldapTemplate.findEmailByUsername(username)
        )
    }
}
