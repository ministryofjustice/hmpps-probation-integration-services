package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.Colour
import uk.gov.justice.digital.hmpps.data.generator.RegistrationsRisksGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.RegistrationRepository

@ExtendWith(MockitoExtension::class)
internal class RegistrationServiceTest {

    @Mock
    lateinit var registrationRepository: RegistrationRepository

    @InjectMocks
    lateinit var registrationService: RegistrationService

    @Test
    fun `get all high priority cases`() {
        val flags = RegistrationsRisksGenerator.generate()
        whenever(registrationRepository.findAllByPersonCrn(Mockito.anyString())).thenReturn(flags)

        val response = registrationService.findActiveRegistrations("123")

        assertThat(response.rosh?.description , equalTo("RoSH"))
        assertThat(response.rosh?.colour?.name, equalTo(Colour.RED.name))
    }

}