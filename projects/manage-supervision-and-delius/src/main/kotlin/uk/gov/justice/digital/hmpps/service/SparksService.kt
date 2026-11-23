package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceDataRepository

@Service
class SparksService(private val referenceDataRepository: ReferenceDataRepository) {
    fun getSparksReferenceData() = referenceDataRepository.findByDatasetCode("SPARKS")
        .map { CodeAndDescription(it.code, it.description) }
}