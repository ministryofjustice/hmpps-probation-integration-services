package uk.gov.justice.digital.hmpps.user

import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {
    fun findUser(username: String): User? {
        return userRepository.findUserByUsername(username)
    }
}
