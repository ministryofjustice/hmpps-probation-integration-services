package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.User
import uk.gov.justice.digital.hmpps.repository.UserRepository
import uk.gov.justice.digital.hmpps.repository.getUserExists

@Service
class UserService(
    val userRepository: UserRepository,
    val ldapTemplate: LdapTemplate,
) {
    fun userExistsByEmail(email: String): UserExistsResponse {
        val users = getUserByEmail(email)
        val returnList: List<User> = when {
            users.isEmpty() -> emptyList()
            users.size == 1 -> listOf(userExists(users[0]))
            else -> users.map { userExists(it) }
        }
        return UserExistsResponse(email, returnList)
    }

    private fun getUserByEmail(email: String): List<String> = ldapTemplate.search(
        query()
            .attributes("cn")
            .searchScope(org.springframework.ldap.query.SearchScope.ONELEVEL)
            .where("mail").`is`(email),
        AttributesMapper { it["cn"]?.get()?.toString() }
    ).filterNotNull().toList()

    private fun userExists(username: String): User =
        try {
            userRepository.getUserExists(username)
            User(true, username)
        } catch (e: NotFoundException) {
            User(false, username)
        }

    data class UserExistsResponse(
        val email: String,
        val users: List<User>
    )
}