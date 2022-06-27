package uk.gov.justice.digital.hmpps.integrations.delius.audit.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteraction
import java.time.ZonedDateTime

interface BusinessInteractionRepository : JpaRepository<BusinessInteraction, Long> {
    fun findByCodeAndEnabledDateIsNullOrEnabledDateIsBefore(
        code: String,
        enabledDate: ZonedDateTime = ZonedDateTime.now()
    ): BusinessInteraction?
}
