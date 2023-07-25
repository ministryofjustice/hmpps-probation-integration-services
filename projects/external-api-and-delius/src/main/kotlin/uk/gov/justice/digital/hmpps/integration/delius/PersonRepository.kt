package uk.gov.justice.digital.hmpps.integration.delius

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrn(crn: String): Person?
}

fun PersonRepository.getByCrn(crn: String) = findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
