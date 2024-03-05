package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.RegistrationsRisksGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.RegistrationRepository

@ExtendWith(MockitoExtension::class)
internal class RegistrationServiceTest {

    @Mock
    lateinit var registrationRepository: RegistrationRepository

    @InjectMocks
    lateinit var registrationService: RegistrationService

    @Test
    fun `get ROSH red cases`() {
        val flags = RegistrationsRisksGenerator.generate()
        whenever(registrationRepository.findAllByPersonCrn(Mockito.anyString())).thenReturn(flags)

        registrationService.findActiveRegistrations("123")

    }

}