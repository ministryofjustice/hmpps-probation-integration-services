package uk.gov.justice.digital.hmpps.service

import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.ldap.query.SearchScope
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.LdapUser
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.ldap.byUsername
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import uk.gov.justice.digital.hmpps.model.UserDetails
import uk.gov.justice.digital.hmpps.user.AuditUserService
import javax.naming.Name

@Service
class UserService(
    private val ldapTemplate: LdapTemplate,
    private val auditUserService: AuditUserService,
) {
    fun getUserDetails(username: String) = ldapTemplate.findByUsername<LdapUser>(username)?.toUserDetails()

    fun getUsersByEmail(email: String) =
        ldapTemplate.find(
            query()
                .base("ou=Users")
                .searchScope(SearchScope.ONELEVEL)
                .where("mail").`is`(email),
            LdapUser::class.java,
        )?.map { it.toUserDetails() } ?: emptyList()

    fun changePassword(
        username: String,
        password: String,
    ) = try {
        val context = ldapTemplate.searchForContext(query().byUsername(username))
        context.setAttributeValue("userPassword", password)
        ldapTemplate.modifyAttributes(context)
    } catch (e: IncorrectResultSizeDataAccessException) {
        if (e.actualSize == 0) throw NotFoundException("User", "username", username)
        throw e
    }

    private fun getUserRoles(name: Name): List<String> =
        ldapTemplate.search(
            query()
                .base(name)
                .searchScope(SearchScope.ONELEVEL)
                .filter("(|(objectclass=NDRole)(objectclass=NDRoleAssociation))"),
            AttributesMapper { it["cn"].get().toString() },
        )

    private fun LdapUser.toUserDetails() =
        UserDetails(
            userId = auditUserService.findUser(username)?.id ?: throw NotFoundException("User entity", "username", username),
            username = username,
            firstName = forename,
            surname = surname,
            email = email,
            enabled = enabled,
            roles = getUserRoles(dn),
        )
}
