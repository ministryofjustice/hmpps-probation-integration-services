package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.User

interface UserRepository : JpaRepository<User, Long> {
    fun existsByUserName(userName: String): Boolean
}

fun UserRepository.getUserExists(username: String): Boolean = existsByUserName(username)