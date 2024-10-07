package uk.gov.justice.digital.hmpps.api.resource

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.api.model.MergeAppointment
import uk.gov.justice.digital.hmpps.service.AppointmentService
import uk.gov.justice.digital.hmpps.service.NsiService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@ExtendWith(MockitoExtension::class)
internal class ReferralResourceTest {
    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var nsiService: NsiService

    @Mock
    lateinit var appointmentService: AppointmentService

    @InjectMocks
    lateinit var referralResource: ReferralResource

    @Test
    fun `when bad request telemetry is recorded`() {
        whenever(
            appointmentService.mergeAppointment(
                any(),
                any()
            )
        ).thenThrow(ResponseStatusException(HttpStatus.BAD_REQUEST))

        val mergeAppointment = MergeAppointment(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "R1234EF",
            ZonedDateTime.now().minusDays(1),
            20,
            "Some notes",
            "DEFAULT",
            false,
            null,
            null,
            null,
            null,
            null,
        )
        val ex = assertThrows<ResponseStatusException> {
            referralResource.mergeAppointment(
                "C356471",
                mergeAppointment.referralId,
                mergeAppointment
            )
        }
        assertThat(ex.message, equalTo("400 BAD_REQUEST"))
        verify(telemetryService).trackEvent(
            "PastAppointmentWithoutOutcome",
            mapOf(
                "crn" to "C356471",
                "referralId" to mergeAppointment.referralId.toString(),
                "referralReference" to mergeAppointment.referralReference,
                "appointmentId" to mergeAppointment.id.toString(),
                "startTime" to mergeAppointment.start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                "outcome" to "null"
            )
        )
    }
}
