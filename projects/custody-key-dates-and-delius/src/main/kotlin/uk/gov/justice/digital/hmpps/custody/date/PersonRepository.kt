package uk.gov.justice.digital.hmpps.custody.date

import org.springframework.data.jpa.repository.JpaRepository

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByNomsIdAndSoftDeletedIsFalse(nomsId: String): Person?
}
