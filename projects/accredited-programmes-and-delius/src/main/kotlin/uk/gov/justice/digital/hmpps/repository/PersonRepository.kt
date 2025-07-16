package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.Person

interface PersonRepository : JpaRepository<Person, Long> {
    @EntityGraph(
        attributePaths = [
            "gender",
            "ethnicity",
            "manager.staff.user",
            "manager.team.localAdminUnit.probationDeliveryUnit"
        ]
    )
    fun findByCrn(crn: String): Person?
}