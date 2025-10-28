package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.toCodeDescription
import uk.gov.justice.digital.hmpps.model.CodeDescription

@Service
class ReferenceDataService(
    private val referenceDataRepository: ReferenceDataRepository
) {
    fun getProjectTypes(): List<CodeDescription> =
        referenceDataRepository.findByDatasetCode(Dataset.UPW_PROJECT_TYPE)
            .map { it.toCodeDescription() }
}