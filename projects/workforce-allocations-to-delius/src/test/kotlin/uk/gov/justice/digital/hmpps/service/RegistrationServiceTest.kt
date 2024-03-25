package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.Colour
import uk.gov.justice.digital.hmpps.api.model.RiskItem
import uk.gov.justice.digital.hmpps.api.model.RiskSummary
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.registration.RegistrationRepository

@ExtendWith(MockitoExtension::class)
internal class RegistrationServiceTest {

    @Mock
    lateinit var registrationRepository: RegistrationRepository

    @InjectMocks
    lateinit var registrationService: RegistrationService

    @Test
    fun `get all high priority cases`() {
        val person = PersonGenerator.DEFAULT
        val flags = RegistrationGenerator.generateRegistrations()
        whenever(registrationRepository.findAllByPersonCrnAndRegisterTypeFlagCodeIn(eq(person.crn), anyList()))
            .thenReturn(flags)

        val response = registrationService.findActiveRegistrations(person.crn)

        val expected = RiskSummary(
            selfHarm = RiskItem("Rosh", Colour.RED),
            alerts = RiskItem("Alerts", Colour.RED),
            safeguarding = RiskItem("Safeguarding", Colour.RED),
            information = RiskItem("Information", Colour.RED),
            publicProtection = RiskItem("Public Protection", Colour.RED)
        )

        assertEquals(expected, response)
    }
}