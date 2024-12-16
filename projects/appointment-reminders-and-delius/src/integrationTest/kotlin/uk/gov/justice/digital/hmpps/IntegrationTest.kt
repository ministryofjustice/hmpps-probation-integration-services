package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.model.UnpaidWorkAppointment
import uk.gov.justice.digital.hmpps.repository.UpwAppointmentRepository
import uk.gov.justice.digital.hmpps.service.UnpaidWorkAppointmentsService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import uk.gov.service.notify.NotificationClient

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var unpaidWorkAppointmentsService: UnpaidWorkAppointmentsService

    @MockitoBean
    lateinit var upwAppointmentRepository: UpwAppointmentRepository

    @MockitoBean
    lateinit var notificationClient: NotificationClient

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `returns csv report`() {
        whenever(upwAppointmentRepository.getUnpaidWorkAppointments(any(), eq("N56"))).thenReturn(
            listOf(
                object : UnpaidWorkAppointment {
                    override val firstName = "Test"
                    override val mobileNumber = "07000000000"
                    override val appointmentDate = "01/01/2000"
                    override val crn = "A123456"
                    override val eventNumbers = "1"
                    override val upwAppointmentIds = "123, 456"
                }
            )
        )

        mockMvc
            .perform(get("/upw-appointments.csv?providerCode=N56").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().contentTypeCompatibleWith("text/csv;charset=UTF-8"))
            .andExpect(
                content().string(
                    """
                    firstName,mobileNumber,appointmentDate,crn,eventNumbers,upwAppointmentIds
                    Test,07000000000,01/01/2000,A123456,1,"123, 456"
                    
                    """.trimIndent()
                )
            )
    }

    @Test
    fun `sends messages to govuk notify`() {
        whenever(upwAppointmentRepository.getUnpaidWorkAppointments(any(), eq("N56"))).thenReturn(
            listOf(
                object : UnpaidWorkAppointment {
                    override val firstName = "Test"
                    override val mobileNumber = "07000000000"
                    override val appointmentDate = "01/01/2000"
                    override val crn = "A123456"
                    override val eventNumbers = "1"
                    override val upwAppointmentIds = "123, 456"
                }
            )
        )

        unpaidWorkAppointmentsService.sendUnpaidWorkAppointmentReminders("N56")

        verify(notificationClient).sendSms(
            "cd713c1b-1b27-45a0-b493-37a34666635a",
            "07000000000",
            mapOf("FirstName" to "Test", "NextWorkSession" to "01/01/2000"),
            "A123456:01/01/2000:123, 456"
        )
        verify(telemetryService).trackEvent(
            "SentUnpaidWorkAppointmentReminder",
            mapOf("crn" to "A123456", "upwAppointmentIds" to "123, 456")
        )
    }
}
