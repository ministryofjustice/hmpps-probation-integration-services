package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.DetailedOffence

interface DetailedOffenceRepository : JpaRepository<DetailedOffence, Long> {
    fun findByCode(code: String): DetailedOffence?
}
