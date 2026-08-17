package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.person.Person
import uk.gov.justice.digital.hmpps.model.CaseIdentifiers

interface PersonRepository : JpaRepository<Person, Long> {
    @EntityGraph(attributePaths = ["gender", "manager.team", "manager.staff.user", "roshRegistrations.type"])
    fun findByCrn(crn: String): Person?

    @EntityGraph(attributePaths = ["gender", "manager.team", "manager.staff.user", "roshRegistrations.type"])
    @Query("select distinct p from Person p join Caseload c on p.id = c.person.id where c.staff.id = :staffId")
    fun findByCaseloadStaffId(staffId: Long, pageable: Pageable): Page<Person>

    @EntityGraph(attributePaths = ["gender", "manager.team", "manager.staff.user", "roshRegistrations.type"])
    @Query("select distinct p from Person p join Caseload c on p.id = c.person.id where c.team.code = :teamCode")
    fun findByCaseloadTeamCode(teamCode: String, pageable: Pageable): Page<Person>

    @Query("select distinct p.crn, p.noms from Person p join Caseload c on p.id = c.person.id where c.team.code = :teamCode")
    fun findCaseIdentifiersByTeamCode(teamCode: String, pageable: Pageable): Page<CaseIdentifiers>
}