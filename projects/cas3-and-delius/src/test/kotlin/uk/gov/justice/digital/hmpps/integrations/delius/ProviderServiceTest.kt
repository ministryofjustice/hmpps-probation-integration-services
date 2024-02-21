package uk.gov.justice.digital.hmpps.integrations.delius

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.generateTeam
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.By
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ProviderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getByCode

@ExtendWith(MockitoExtension::class)
class ProviderServiceTest {
    @Mock
    internal lateinit var providerRepository: ProviderRepository

    @Mock
    internal lateinit var staffRepository: StaffRepository

    @InjectMocks
    internal lateinit var providerService: ProviderService

    @Test
    fun `exception thrown if staff not in team`() {
        val provider = ProviderGenerator.generateProvider("N01")
        val staff = ProviderGenerator.generateStaff(
            "N01UAT1", listOf(
                generateTeam("N01UAT")
            )
        )

        whenever(providerRepository.findByCode(provider.code)).thenReturn(provider)
        whenever(staffRepository.findByCode(staff.code)).thenReturn(staff)

        assertThrows<IllegalStateException> { providerService.findManagerIds(By(staff.code, provider.code)) }
    }
}