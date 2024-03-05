package uk.gov.justice.digital.hmpps.integrations.delius.event.registration

import org.springframework.data.jpa.repository.JpaRepository

interface RegistrationRepository : JpaRepository<Registration, Long> {
    fun findAllByPersonCrn(crn: String): List<Registration>
}