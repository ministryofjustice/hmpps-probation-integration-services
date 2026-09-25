package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.model.CodedValue
import uk.gov.justice.digital.hmpps.repository.PduRepository
import uk.gov.justice.digital.hmpps.repository.ProviderRepository

@Service
class ReferenceDataService(
    private val providerRepository: ProviderRepository,
    private val pduRepository: PduRepository,
) {
    fun regions(): List<CodedValue> = providerRepository.findBySelectableTrueOrderByCode()
        .map { it.toCodedValue() }

    fun pdus(regionCode: String): List<CodedValue> =
        pduRepository.findByProviderCodeAndSelectableTrueAndProviderSelectableTrue(regionCode)
            .map { it.toCodedValue() }
}
