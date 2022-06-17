package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.entity.User
import uk.gov.justice.digital.hmpps.integrations.delius.repository.UserRepository

@Service
class UserService(private val userRepository: UserRepository) {
    fun findServiceUser(): User {
        val principal = SecurityContextHolder.getContext().authentication.principal
        val name = if (principal is UserDetails) {
            principal.username
        } else throw IllegalAccessException("Unable to get username from principal")
        return userRepository.findUserByUsername(name)
            ?: throw IllegalAccessException("Service User does not exist in the database")
    }
}