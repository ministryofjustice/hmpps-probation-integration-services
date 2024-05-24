package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.Provider

interface PrisonProbationAreaRepository : JpaRepository<Provider, Long> {
    fun findByInstitutionNomisCode(nomisCode: String): Provider?
}
