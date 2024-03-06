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

    val CRN = "123"
    @Test
    fun `get all high priority cases`() {
        val flags = RegistrationsRisksGenerator.generateRegistrations()
        whenever(registrationRepository.findAllByPersonCrn(Mockito.anyString())).thenReturn(flags)

        val response = registrationService.findActiveRegistrations(CRN)

        assertThat(response.rosh?.description , equalTo("Rosh"))
        assertThat(response.rosh?.colour?.name, equalTo(Colour.RED.name))

        assertThat(response.alerts?.description, equalTo("Alerts"))
        assertThat(response.alerts?.colour?.name, equalTo(Colour.RED.name))

        assertThat(response.safeguarding?.description, equalTo("Safeguarding"))
        assertThat(response.safeguarding?.colour?.name, equalTo(Colour.RED.name))

        assertThat(response.information?.description, equalTo("Information"))
        assertThat(response.information?.colour?.name, equalTo(Colour.RED.name))

        assertThat(response.publicProtection?.description, equalTo("Public Protection"))
        assertThat(response.publicProtection?.colour?.name, equalTo(Colour.RED.name))
    }

    @Test
    fun `no case data available`() {
        whenever(registrationRepository.findAllByPersonCrn(Mockito.anyString()))
            .thenReturn(listOf(RegistrationsRisksGenerator.REGISTRATION_NO_REFERENCE_DATA))

        val response = registrationService.findActiveRegistrations(CRN)

        assertThat(response.rosh?.description , equalTo(null))
        assertThat(response.rosh?.colour?.name, equalTo(null))

        assertThat(response.alerts?.description, equalTo(null))
        assertThat(response.alerts?.colour?.name, equalTo(null))

        assertThat(response.safeguarding?.description, equalTo(null))
        assertThat(response.safeguarding?.colour?.name, equalTo(null))

        assertThat(response.information?.description, equalTo(null))
        assertThat(response.information?.colour?.name, equalTo(null))

        assertThat(response.publicProtection?.description, equalTo(null))
        assertThat(response.publicProtection?.colour?.name, equalTo(null))
    }
}