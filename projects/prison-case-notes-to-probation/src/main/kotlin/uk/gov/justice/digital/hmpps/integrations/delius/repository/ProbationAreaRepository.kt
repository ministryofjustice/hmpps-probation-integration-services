package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ProbationArea

interface ProbationAreaRepository : JpaRepository<ProbationArea, Long> {
    fun findByInstitutionNomisCode(nomisCode: String): ProbationArea?
}