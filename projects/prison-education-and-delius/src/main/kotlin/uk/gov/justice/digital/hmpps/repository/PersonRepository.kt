package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.Person

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByPrisonerId(nomsNumber: String): Person?
}
