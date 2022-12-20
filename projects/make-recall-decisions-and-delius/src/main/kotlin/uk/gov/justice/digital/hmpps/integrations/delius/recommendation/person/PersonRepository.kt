package uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.entity.Person

interface PersonRepository : JpaRepository<Person, Long> {

    @Query("SELECT p FROM Person p JOIN FETCH p.manager WHERE p.crn = :crn AND p.softDeleted = false")
    fun findByCrn(crn: String): Person?
}

fun PersonRepository.getPerson(crn: String): Person =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)