package uk.gov.justice.digital.hmpps.integrations.delius.person

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrnAndSoftDeletedIsFalse(crn: String): Person?

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select p.id from Person p where p.id = :id")
    fun findForUpdate(id: Long): Long
}

fun PersonRepository.getByCrn(crn: String) =
    findByCrnAndSoftDeletedIsFalse(crn) ?: throw NotFoundException("Person", "crn", crn)
