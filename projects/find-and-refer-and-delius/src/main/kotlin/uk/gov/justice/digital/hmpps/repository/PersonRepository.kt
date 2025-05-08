package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface PersonRepository : JpaRepository<Person, Long> {

    @EntityGraph(attributePaths = ["gender", "ethnicity"])
    fun findByCrn(crn: String): Person?

    @EntityGraph(attributePaths = ["gender", "ethnicity"])
    fun findByNomsNumber(nomsNumber: String): Person?
}

fun PersonRepository.getPersonByCrn(crn: String) = findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
fun PersonRepository.getPersonByNoms(nomsNumber: String) =
    findByNomsNumber(nomsNumber) ?: throw NotFoundException("Person", "prisoner number", nomsNumber)