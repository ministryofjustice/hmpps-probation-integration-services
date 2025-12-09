package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    fun findByCodeAndDatasetCode(code: String, datasetCode: String): ReferenceData?
}

fun ReferenceDataRepository.getByCode(knownValue: ReferenceData.KnownValue) =
    knownValue.let { (code, datasetCode) -> findByCodeAndDatasetCode(code, datasetCode).orNotFoundBy("code", code) }

fun ReferenceDataRepository.domainEventType(code: String) =
    getByCode(ReferenceData.KnownValue(code, "DOMAIN EVENT TYPE"))
