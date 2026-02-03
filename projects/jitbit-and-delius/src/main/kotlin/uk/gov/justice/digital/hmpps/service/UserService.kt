package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.DirectoryUser
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.User
import uk.gov.justice.digital.hmpps.repository.UserRepository
import uk.gov.justice.digital.hmpps.repository.getUserExists

@Service
class UserService(
    val userRepository: UserRepository,
    val ldapTemplate: LdapTemplate,
) {
    fun userExistsByEmail(email: String): User {
        val users = getUserByEmail(email)
        return when {
            users.isEmpty() -> User(false)
            users.size == 1 -> userExists(users[0].username)
            else -> throw IllegalStateException("Multiple users found with email: $email")
        }
    }

    private fun getUserByEmail(email: String) = ldapTemplate.find(
        query()
            .searchScope(org.springframework.ldap.query.SearchScope.ONELEVEL)
            .where("mail").`is`(email),
        DirectoryUser::class.java
    )

    private fun userExists(username: String): User =
        try {
            userRepository.getUserExists(username)
            User(true)
        } catch (e: NotFoundException) {
            User(false)
        }
}