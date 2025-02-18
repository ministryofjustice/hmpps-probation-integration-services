package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.BeforeEach
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
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.model.Provider
import uk.gov.justice.digital.hmpps.model.ProviderResponse
import uk.gov.justice.digital.hmpps.model.UnpaidWorkAppointment
import uk.gov.justice.digital.hmpps.repository.UpwAppointmentRepository
import uk.gov.justice.digital.hmpps.service.UnpaidWorkAppointmentsService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
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

    @BeforeEach
    fun setup() {
        whenever(upwAppointmentRepository.getUnpaidWorkAppointments(any(), eq("N56"))).thenReturn(
            listOf(
                object : UnpaidWorkAppointment {
                    override val firstName = "Test"
                    override val mobileNumber = "07000000001"
                    override val appointmentDate = "01/01/2000"
                    override val crn = "A000001"
                    override val eventNumbers = "1"
                    override val upwAppointmentIds = "123, 456"
                },
                object : UnpaidWorkAppointment {
                    override val firstName = "Test"
                    override val mobileNumber = "07000000002"
                    override val appointmentDate = "01/01/2000"
                    override val crn = "A000002"
                    override val eventNumbers = "1"
                    override val upwAppointmentIds = "789"
                }
            )
        )
    }

    @Test
    fun `returns csv report`() {
        mockMvc
            .perform(get("/upw-appointments.csv?providerCode=N56").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().contentTypeCompatibleWith("text/csv;charset=UTF-8"))
            .andExpect(
                content().string(
                    """
                    firstName,mobileNumber,appointmentDate,crn,eventNumbers,upwAppointmentIds
                    Test,07000000001,01/01/2000,A000001,1,"123, 456"
                    Test,07000000002,01/01/2000,A000002,1,789
                    
                    """.trimIndent()
                )
            )
    }

    @Test
    fun `sends messages to govuk notify`() {
        unpaidWorkAppointmentsService.sendUnpaidWorkAppointmentReminders("N56", listOf("template"), 3)

        verify(notificationClient).sendSms(
            "template",
            "07000000001",
            mapOf("FirstName" to "Test", "NextWorkSession" to "01/01/2000"),
            "A000001"
        )
        verify(telemetryService).trackEvent(
            "UnpaidWorkAppointmentReminderSent",
            mapOf(
                "crn" to "A000001",
                "upwAppointmentIds" to "123, 456",
                "providerCode" to "N56",
                "templateIds" to "template",
                "notificationIds" to "null"
            )
        )
        verify(telemetryService).trackEvent(
            "UnpaidWorkAppointmentReminderNotSent",
            mapOf(
                "crn" to "A000002",
                "upwAppointmentIds" to "789",
                "providerCode" to "N56",
                "templateIds" to "template",
            )
        )
    }

    @Test
    fun `retrieves user's providers`() {
        mockMvc.perform(get("/users/${UserGenerator.TEST_USER.username}/providers").withToken())
            .andExpect(status().isOk)
            .andExpectJson(ProviderResponse(listOf(Provider("N07", "London"))))
    }
}
