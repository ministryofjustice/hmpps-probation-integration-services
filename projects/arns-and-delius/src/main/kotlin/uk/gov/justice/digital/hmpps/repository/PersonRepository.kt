package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface PersonRepository : JpaRepository<Person, Long> {
    @EntityGraph(
        attributePaths = [
            "gender",
            "events.disposal",
            "events.mainOffence",
            "events.mainOffence.offence",
        ],
    )
    fun findByCrn(crn: String): Person?
}

fun PersonRepository.getByCrn(crn: String): Person =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
