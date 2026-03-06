package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.Name

interface PersonRepository : JpaRepository<Person, Long> {
    @Query("select p.firstName, p.secondName, p.thirdName, p.surname from Person p where p.crn = :crn")
    fun findNameByCrn(crn: String): Name?
    fun getNameByCrn(crn: String) = findNameByCrn(crn) ?: throw NotFoundException("Person", "CRN", crn)

    @Query("select p.id from Person p where p.crn = :crn")
    fun findIdByCrn(crn: String): Long?
    fun getIdByCrn(crn: String) = findIdByCrn(crn) ?: throw NotFoundException("Person", "CRN", crn)

    @EntityGraph(attributePaths = ["manager.staff.user", "manager.team.officeLocations"])
    fun findByCrn(crn: String): Person?
    fun getByCrn(crn: String) = findByCrn(crn) ?: throw NotFoundException("Person", "CRN", crn)
}
