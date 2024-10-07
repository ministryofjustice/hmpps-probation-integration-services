package uk.gov.justice.digital.hmpps.integrations.delius

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.By
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ProviderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.TeamRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
class ProviderServiceTest {
    @Mock
    internal lateinit var providerRepository: ProviderRepository

    @Mock
    internal lateinit var staffRepository: StaffRepository

    @Mock
    internal lateinit var teamRepository: TeamRepository

    @Mock
    internal lateinit var telemetryService: TelemetryService

    @InjectMocks
    internal lateinit var providerService: ProviderService

    @Test
    fun `log to telemetry if staff not in team`() {
        val provider = ProviderGenerator.generateProvider("N01")
        val expectedTeam = ProviderGenerator.generateTeam(provider.homelessPreventionTeamCode())
        val actualTeam = ProviderGenerator.generateTeam("N01UAT")
        val staff = ProviderGenerator.generateStaff("N01UAT1", listOf(actualTeam))

        whenever(providerRepository.findByCode(provider.code)).thenReturn(provider)
        whenever(teamRepository.findByCode(expectedTeam.code)).thenReturn(expectedTeam)
        whenever(staffRepository.findByCode(staff.code)).thenReturn(staff)

        providerService.findManagerIds(By(staff.code, provider.code))

        verify(telemetryService).trackEvent(
            "StaffNotInTeam",
            mapOf("staffCode" to "N01UAT1", "reason" to "Officer not in team `N01HPT`"),
            mapOf()
        )
    }
}