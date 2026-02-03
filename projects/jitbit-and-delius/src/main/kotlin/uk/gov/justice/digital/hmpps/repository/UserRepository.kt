package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.User
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface UserRepository : JpaRepository<User, Long> {
    fun findByUserName(userName: String): User?
}

fun UserRepository.getUserExists(username: String): User = findByUserName(username) ?: throw NotFoundException(username)