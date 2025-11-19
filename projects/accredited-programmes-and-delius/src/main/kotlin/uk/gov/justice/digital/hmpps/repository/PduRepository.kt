package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.staff.ProbationDeliveryUnit

interface PduRepository : JpaRepository<ProbationDeliveryUnit, Long> {
    fun getByCode(code: String): ProbationDeliveryUnit?
}