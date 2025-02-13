package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.ProbationAreaUser

interface ProbationAreaUserRepository : JpaRepository<ProbationAreaUser, Long> {
    @Query("select pau from ProbationAreaUser pau join fetch pau.id.provider where upper(pau.id.user.username) = upper(:username)")
    fun findByUsername(username: String): List<ProbationAreaUser>
}