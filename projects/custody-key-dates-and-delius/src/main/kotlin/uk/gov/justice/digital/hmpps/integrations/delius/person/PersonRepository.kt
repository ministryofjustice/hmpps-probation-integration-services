package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(nomsId: String): Person?

    @Query("select p.nomsId from Person p where p.crn = :crn and p.softDeleted = false")
    fun findNomsIdByCrn(crn: String): String?
}

