package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.registration.Registration

interface RegistrationRepository : JpaRepository<Registration, Long> {
    @EntityGraph(
        attributePaths = [
            "person",
            "type",
            "category"
        ]
    )
    fun findByPersonCrn(crn: String): List<Registration>
}