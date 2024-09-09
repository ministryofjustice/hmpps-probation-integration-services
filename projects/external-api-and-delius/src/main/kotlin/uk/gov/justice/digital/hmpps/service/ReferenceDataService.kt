package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integration.delius.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.model.ProbationReferenceData
import uk.gov.justice.digital.hmpps.model.RefData

@Service
class ReferenceDataService(
    private val registrationRepository: RegistrationRepository,
) {
    fun getReferenceData(): ProbationReferenceData =
        ProbationReferenceData(
            registrationRepository.getReferenceData()
                .groupByTo(LinkedHashMap(), { it.codeSet.trim() }, { RefData(it.code.trim(), it.description) })
        )
}