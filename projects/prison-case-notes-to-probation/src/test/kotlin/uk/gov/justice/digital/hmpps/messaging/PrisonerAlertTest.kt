package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.HttpStatusCodeException
import uk.gov.justice.digital.hmpps.data.generator.AlertMessageGenerator.ALERT_CREATED
import uk.gov.justice.digital.hmpps.data.generator.PrisonCaseNoteGenerator
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonerAlertClient
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class PrisonerAlertTest {
    @Mock
    private lateinit var alertApi: PrisonerAlertClient

    @Mock
    private lateinit var deliusService: DeliusService

    @Mock
    private lateinit var telemetryService: TelemetryService

    @InjectMocks
    private lateinit var prisonerAlert: PrisonerAlert

    @Test
    fun `Exception logged but not thrown when alert not found`() {
        whenever(alertApi.getAlert(any())).thenThrow(HttpServerErrorException(HttpStatus.NOT_FOUND))
        val domainEvent = ALERT_CREATED.message.copy(detailUrl = "http://localhost:8080/test")
        prisonerAlert.handle(domainEvent)

        verify(telemetryService).trackEvent("AlertNotFound", mapOf("detailUrl" to domainEvent.detailUrl!!))
    }

    @Test
    fun `Exception other than not found thrown`() {
        whenever(alertApi.getAlert(any())).thenThrow(HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
        val domainEvent = ALERT_CREATED.message.copy(detailUrl = "http://localhost:8080/test")
        val exception = assertThrows<HttpStatusCodeException> { prisonerAlert.handle(domainEvent) }
        assertThat(exception.statusCode, equalTo(HttpStatus.INTERNAL_SERVER_ERROR))
    }

    @Test
    fun `Delius service exception thrown`() {
        whenever(deliusService.mergeCaseNote(any())).thenThrow(IllegalStateException("Something went wrong"))
        whenever(alertApi.getAlert(any())).thenReturn(PrisonCaseNoteGenerator.CREATED_ALERT)
        val domainEvent = ALERT_CREATED.message.copy(detailUrl = "http://localhost:8080/test")
        val exception = assertThrows<IllegalStateException> { prisonerAlert.handle(domainEvent) }
        assertThat(exception.message, equalTo("Something went wrong"))
    }

    @Test
    fun `Message ignored if offender not found`() {
        whenever(deliusService.mergeCaseNote(any())).thenThrow(OffenderNotFoundException("Offender not found"))
        whenever(alertApi.getAlert(any())).thenReturn(PrisonCaseNoteGenerator.CREATED_ALERT)
        val domainEvent = ALERT_CREATED.message.copy(detailUrl = "http://localhost:8080/test")
        prisonerAlert.handle(domainEvent)

        verify(telemetryService).trackEvent(eq("AlertMergeFailed"), any(), any())
    }
}