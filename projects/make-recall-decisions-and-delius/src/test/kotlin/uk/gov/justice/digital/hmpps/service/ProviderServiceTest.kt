package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProviderRepository

@ExtendWith(MockitoExtension::class)
internal class ProviderServiceTest {
    @Mock
    lateinit var providerRepository: ProviderRepository

    @InjectMocks
    lateinit var providerService: ProviderService

    @Test
    fun `get provider by code`() {
        val entity = Provider(1, "ABC", "Description")
        whenever(providerRepository.findByCode("ABC")).thenReturn(entity)

        val result = providerService.getProviderByCode("ABC")!!
        assertThat(result.code, equalTo("ABC"))
        assertThat(result.name, equalTo("Description"))
    }

    @Test
    fun `get all providers`() {
        val entity = Provider(1, "ABC", "Description")
        whenever(providerRepository.findActive()).thenReturn(listOf(entity))

        val result = providerService.getProviders()
        assertThat(result, hasSize(1))
        assertThat(result[0].code, equalTo("ABC"))
        assertThat(result[0].name, equalTo("Description"))
    }
}
