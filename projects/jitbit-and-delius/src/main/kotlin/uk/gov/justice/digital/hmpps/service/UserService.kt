package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.repository.UserRepository
import uk.gov.justice.digital.hmpps.repository.userExists

@Service
class UserService(
    val userRepository: UserRepository,
    val ldapTemplate: LdapTemplate,
) {
    fun userExistsByEmail(email: String): UserExistsResponse {
        val userResults = getUserByEmail(email)
            .filter { userRepository.userExists(it) }
        return UserExistsResponse(email, userResults)
    }

    private fun getUserByEmail(email: String): List<String> = ldapTemplate.search(
        query()
            .attributes("cn")
            .searchScope(org.springframework.ldap.query.SearchScope.ONELEVEL)
            .where("mail").`is`(email),
        AttributesMapper { it["cn"]?.get()?.toString() }
    ).filterNotNull().toList()

    data class UserExistsResponse(
        val email: String,
        val users: List<String>
    )
}