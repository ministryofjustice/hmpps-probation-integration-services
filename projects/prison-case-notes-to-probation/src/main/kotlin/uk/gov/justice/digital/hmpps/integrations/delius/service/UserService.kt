package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.audit.User
import uk.gov.justice.digital.hmpps.integrations.delius.repository.UserRepository

@Service
class UserService(private val userRepository: UserRepository) {
    fun findUser(username: String): User? {
        return userRepository.findUserByUsername(username)
    }
}
