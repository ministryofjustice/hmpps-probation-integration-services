package uk.gov.justice.digital.hmpps.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.entity.staff.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.entity.staff.Provider
import uk.gov.justice.digital.hmpps.model.CodedValue
import uk.gov.justice.digital.hmpps.repository.PduRepository
import uk.gov.justice.digital.hmpps.repository.ProviderRepository

@ExtendWith(MockitoExtension::class)
internal class ReferenceDataServiceTest {

    @Mock
    lateinit var providerRepository: ProviderRepository

    @Mock
    lateinit var pduRepository: PduRepository

    @InjectMocks
    lateinit var referenceDataService: ReferenceDataService

    @Test
    fun `regions returns only selectable providers mapped to coded values`() {
        val providers = listOf(
            Provider(id = 1, code = "N01", description = "N01 Provider", selectable = true),
            Provider(id = 2, code = "N02", description = "N02 Provider", selectable = true)
        )
        given(providerRepository.findBySelectableTrueOrderByCode()).willReturn(providers)

        val result = referenceDataService.regions()

        assertThat(result).containsExactly(
            CodedValue("N01", "N01 Provider"),
            CodedValue("N02", "N02 Provider")
        )
    }

    @Test
    fun `regions returns empty list when no providers are selectable`() {
        given(providerRepository.findBySelectableTrueOrderByCode()).willReturn(emptyList())

        val result = referenceDataService.regions()

        assertThat(result).isEmpty()
    }

    @Test
    fun `pdus delegates to repository using region code and maps results to coded values`() {
        val provider = Provider(id = 1, code = "N01", description = "N01 Provider", selectable = true)
        val pdus = listOf(
            ProbationDeliveryUnit(1, "PDU001", "Central Borough", provider, true),
            ProbationDeliveryUnit(2, "PDU002", "East Borough", provider, true)
        )
        given(
            pduRepository.findByProviderCodeAndSelectableTrueAndProviderSelectableTrue("N01")
        ).willReturn(pdus)

        val result = referenceDataService.pdus("N01")

        assertThat(result).containsExactly(
            CodedValue("PDU001", "Central Borough"),
            CodedValue("PDU002", "East Borough")
        )
        verify(pduRepository).findByProviderCodeAndSelectableTrueAndProviderSelectableTrue("N01")
    }

    @Test
    fun `pdus returns empty list when repository finds no active pdus for region`() {
        given(
            pduRepository.findByProviderCodeAndSelectableTrueAndProviderSelectableTrue("UNKNOWN")
        ).willReturn(emptyList())

        val result = referenceDataService.pdus("UNKNOWN")

        assertThat(result).isEmpty()
    }
}

