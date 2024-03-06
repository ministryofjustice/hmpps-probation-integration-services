package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.Colour
import uk.gov.justice.digital.hmpps.api.model.RiskItem
import uk.gov.justice.digital.hmpps.api.model.RiskSummary
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

        val expected = RiskSummary(rosh = RiskItem("Rosh", Colour.RED),
            alerts = RiskItem("Alerts", Colour.RED),
            safeguarding = RiskItem("Safeguarding", Colour.RED),
            information = RiskItem("Information", Colour.RED),
            publicProtection = RiskItem("Public Protection", Colour.RED)
        )

        assertEquals(expected, response)
    }

    @Test
    fun `no case data available`() {
        whenever(registrationRepository.findAllByPersonCrn(Mockito.anyString()))
            .thenReturn(listOf(RegistrationsRisksGenerator.REGISTRATION_NO_REFERENCE_DATA))

        val response = registrationService.findActiveRegistrations(CRN)

        val expected = RiskSummary()

        assertEquals(expected, response)
    }
}