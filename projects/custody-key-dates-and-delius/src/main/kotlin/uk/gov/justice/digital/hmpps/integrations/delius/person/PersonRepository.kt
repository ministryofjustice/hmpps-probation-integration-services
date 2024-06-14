package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(nomsId: String): Person?
    fun findByCrnAndNomsIdIsNotNullAndSoftDeletedIsFalse(crn: String): Person?
}

fun PersonRepository.getPersonWithNomsId(crn: String) = findByCrnAndNomsIdIsNotNullAndSoftDeletedIsFalse(crn)
    ?: throw NotFoundException("Person or NomsId", "crn", crn)
