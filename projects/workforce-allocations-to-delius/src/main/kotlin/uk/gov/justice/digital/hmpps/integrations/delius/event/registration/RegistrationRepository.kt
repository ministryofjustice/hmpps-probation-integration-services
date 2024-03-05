

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.Registration

interface RegistrationRepository : JpaRepository<Registration, Long> {
    fun findAllByPersonCrn(crn: String): List<Registration>
}
