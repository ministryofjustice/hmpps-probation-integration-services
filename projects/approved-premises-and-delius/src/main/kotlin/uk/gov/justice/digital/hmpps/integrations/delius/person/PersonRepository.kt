package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrnAndSoftDeletedIsFalse(crn: String): Person?
}

fun PersonRepository.getByCrn(crn: String) = findByCrnAndSoftDeletedIsFalse(crn) ?: throw NotFoundException("Person", "crn", crn)
