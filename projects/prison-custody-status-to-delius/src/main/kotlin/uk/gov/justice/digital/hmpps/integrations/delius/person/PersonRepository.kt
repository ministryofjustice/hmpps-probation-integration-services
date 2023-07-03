package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByNomsNumberAndSoftDeletedIsFalse(nomsNumber: String): List<Person>
}
