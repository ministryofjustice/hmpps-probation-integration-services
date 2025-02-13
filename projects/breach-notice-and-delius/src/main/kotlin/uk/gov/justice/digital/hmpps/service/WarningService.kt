package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.WarningTypeRepository
import uk.gov.justice.digital.hmpps.model.CodedDescription
import uk.gov.justice.digital.hmpps.model.WarningTypes

@Service
class WarningService(private val warningTypeRepository: WarningTypeRepository) {
    fun getWarningTypes(): WarningTypes = WarningTypes(
        warningTypeRepository.findAllBySelectableTrue()
            .map { CodedDescription(it.code, it.description) }
            .sortedBy { it.description }
    )
}