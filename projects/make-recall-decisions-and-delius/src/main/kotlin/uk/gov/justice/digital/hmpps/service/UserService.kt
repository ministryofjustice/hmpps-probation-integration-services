package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.Provider
import uk.gov.justice.digital.hmpps.api.model.UserDetails
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProviderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.UserAccessRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.details.UserDetailsRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.ldap.entity.LdapUser
import uk.gov.justice.digital.hmpps.ldap.findByUsername

@Service
class UserService(
    private val userDetailsRepository: UserDetailsRepository,
    private val userAccessRepository: UserAccessRepository,
    private val providerRepository: ProviderRepository,
    private val ldapTemplate: LdapTemplate
) {
    fun checkUserAccess(username: String, crn: String) = if (userAccessRepository.existsByCrn(crn)) {
        userAccessRepository.findByUsernameAndCrn(username, crn)
    } else {
        throw NotFoundException("Person", "crn", crn)
    }

    fun getUserDetails(username: String) = userDetailsRepository.findByUsername(username)?.let { user ->
        val ldapUser = ldapTemplate.findByUsername<LdapUser>(username)
        UserDetails(
            name = Name(user.forename, user.middleName, user.surname),
            username = user.username,
            staffCode = user.staff?.code,
            email = ldapUser?.email,
            homeArea = ldapUser?.homeArea
                ?.let(providerRepository::findByCode)
                ?.let { Provider(it.code, it.description) }
        )
    }
}
