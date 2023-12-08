package uk.gov.justice.digital.hmpps.audit.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import java.time.ZonedDateTime

interface BusinessInteractionRepository : JpaRepository<BusinessInteraction, Long> {
    @Query(
        """
        select bi from BusinessInteraction bi 
        where bi.code = :code 
        and (bi.enabledDate is null or bi.enabledDate <= :enabledDate)
    """,
    )
    fun findByCode(
        code: String,
        enabledDate: ZonedDateTime = ZonedDateTime.now(),
    ): BusinessInteraction?
}
