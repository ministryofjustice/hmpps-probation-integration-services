package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.UserDetail
import uk.gov.justice.digital.hmpps.integrations.ldap.entity.LdapUserDetails
import uk.gov.justice.digital.hmpps.ldap.findByUsername

@Service
class UserService(private val userAccessService: UserAccessService, private val ldapTemplate: LdapTemplate) {
    fun userDetails(username: String): UserDetail? =
        ldapTemplate.findByUsername<LdapUserDetails>(username)?.let {
            UserDetail(it.username, Name(it.forename, it.surname), it.email)
        }

    fun userAccessFor(
        username: String,
        crns: List<String>,
    ) = userAccessService.userAccessFor(username, crns)
}
