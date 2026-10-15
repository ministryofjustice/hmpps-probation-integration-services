package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.person.Registration

interface RegistrationRepository : JpaRepository<Registration, Long> {
    @Query(
        """
            select r from Registration r
            where r.type.code in :codes
            and r.person.crn = :crn
        """
    )
    fun findByRegistrationCodes(crn: String, codes: List<String>): List<Registration>
}