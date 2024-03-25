package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.toProvider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProviderRepository

@Service
class ProviderService(private val providerRepository: ProviderRepository) {
    fun getProviders() = providerRepository.findActive().map { it.toProvider() }
    fun getProviderByCode(code: String) = providerRepository.findByCode(code)?.toProvider()
}
