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
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
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
        whenever(upwAppointmentRepository.getUnpaidWorkAppointments(any(), eq("N56"), any(), any())).thenReturn(
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

    @Test
    fun `retrieves data quality stats`() {
        mockMvc.perform(get("/data-quality/${ProviderGenerator.LONDON.code}/stats").withToken())
            .andExpect(status().isOk)
            .andExpect(content().json("""{"missing":1,"invalid":2}""", JsonCompareMode.STRICT))
    }

    @Test
    fun `retrieves cases with an invalid mobile number`() {
        mockMvc.perform(get("/data-quality/${ProviderGenerator.LONDON.code}/invalid-mobile-numbers").withToken())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    {
                      "content": [
                        {
                          "name": "Test Person",
                          "crn": "A000004",
                          "mobileNumber": "07000000004 invalid",
                          "manager": {
                            "name": "Test Staff",
                            "email":"test@example.com"
                          },
                          "probationDeliveryUnit": "Croydon"
                        },
                        {
                          "name": "Test Person",
                          "crn": "A000005",
                          "mobileNumber": "070000005",
                          "manager": {
                            "name": "Test Staff",
                            "email":"test@example.com"
                          },
                          "probationDeliveryUnit": "Croydon"
                        }
                      ],
                      "page": {
                        "size": 10,
                        "number": 0,
                        "totalElements": 2,
                        "totalPages": 1
                      }
                    }
                    """.trimIndent(), JsonCompareMode.STRICT
                )
            )
    }

    @Test
    fun `retrieves cases with a missing mobile number`() {
        mockMvc.perform(get("/data-quality/${ProviderGenerator.LONDON.code}/missing-mobile-numbers").withToken())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    {
                      "content": [
                        {
                          "name": "Test Person",
                          "crn": "A000006",
                          "manager": {
                            "name": "Test Staff",
                            "email":"test@example.com"
                          },
                          "probationDeliveryUnit": "Croydon"
                        }
                      ],
                      "page": {
                        "size": 10,
                        "number": 0,
                        "totalElements": 1,
                        "totalPages": 1
                      }
                    }
                    """.trimIndent(), JsonCompareMode.STRICT
                )
            )
    }
}
