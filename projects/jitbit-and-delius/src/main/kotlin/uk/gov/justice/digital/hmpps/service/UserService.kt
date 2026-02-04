package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.model.User
import uk.gov.justice.digital.hmpps.repository.UserRepository
import uk.gov.justice.digital.hmpps.repository.userExists

@Service
class UserService(
    val userRepository: UserRepository,
    val ldapTemplate: LdapTemplate,
) {
    fun userExistsByEmail(email: String) = UserExistsResponse(
        email = email,
        users = getUserByEmail(email)
            .filter { userRepository.existsByUserName(it) }
            .map { User(it) }
    }

    private fun getUserByEmail(email: String): List<String> = ldapTemplate.search(
        query()
            .attributes("cn")
            .searchScope(org.springframework.ldap.query.SearchScope.ONELEVEL)
            .where("mail").`is`(email),
        AttributesMapper { it["cn"]?.get()?.toString() }
    ).filterNotNull().toList()

    private fun userExists(username: String): User {
        val exists = userRepository.userExists(username)
        return User(exists, username)
    }

    data class UserExistsResponse(
        val email: String,
        val users: List<User>
    )
}