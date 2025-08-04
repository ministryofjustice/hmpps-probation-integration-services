package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
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
import uk.gov.service.notify.NotificationList
import java.time.ZonedDateTime

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
        whenever(notificationClient.getNotifications(isNull(), eq("sms"), isNull(), anyOrNull())).thenReturn(
            NotificationList(
                """
                    {
                      "notifications": [],
                      "links": {
                        "current": "current-url"
                      }
                    }
                """.trimIndent()
            )
        )

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
    fun `do not send messages if one has already been sent for that crn today`() {
        whenever(notificationClient.getNotifications(isNull(), eq("sms"), isNull(), anyOrNull())).thenReturn(
            NotificationList(
                """
                    {
                      "notifications": [
                      {
                        "id": "1dbd55a3-71d8-44ea-9168-e5e7b04217ce",
                        "reference": "A000001",
                        "phoneNumber": "07000000001",
                        "type": "sms",
                        "template": {
                            "id": "2c15f36f-d2e4-480a-8330-42cf2ad73071",
                            "version": 1,
                            "uri": "template-uri"
                        },
                        "body": "Notification sent to 07000000001",
                        "status": "sent",
                        "created_at": "${ZonedDateTime.now()}",
                        "sent_at": "${ZonedDateTime.now()}"
                      },
                      {
                        "id": "72e13b0a-6579-4ea5-b4b5-afd432f672e4",
                        "reference": "A000002",
                        "phoneNumber": "07000000002",
                        "type": "sms",
                        "template": {
                            "id": "2c15f36f-d2e4-480a-8330-42cf2ad73071",
                            "version": 1,
                            "uri": "template-uri"
                        },
                        "body": "Notification sent to 07000000002",
                        "status": "sent",
                        "created_at": "${ZonedDateTime.now().minusDays(1)}",
                        "sent_at": "${ZonedDateTime.now().minusDays(1)}"
                      }
                      ],
                      "links": {
                        "current": "current-url"
                      }
                    }
                """.trimIndent()
            )
        )

        unpaidWorkAppointmentsService.sendUnpaidWorkAppointmentReminders("N56", listOf("template"), 3)

        verify(notificationClient, never()).sendSms(
            "template",
            "07000000001",
            mapOf("FirstName" to "Test", "NextWorkSession" to "01/01/2000"),
            "A000001"
        )
        verify(telemetryService).trackEvent(
            "UnpaidWorkAppointmentReminderNotSent",
            mapOf(
                "crn" to "A000001",
                "upwAppointmentIds" to "123, 456",
                "providerCode" to "N56",
                "templateIds" to "template"
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
    fun `retrieves count of cases with an invalid mobile number`() {
        mockMvc.perform(get("/data-quality/${ProviderGenerator.LONDON.code}/invalid-mobile-numbers/count").withToken())
            .andExpect(status().isOk)
            .andExpect(content().string("2"))
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

    @Test
    fun `retrieves cases with duplicate mobile numbers`() {
        mockMvc.perform(get("/data-quality/${ProviderGenerator.LONDON.code}/duplicate-mobile-numbers").withToken())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    {
                      "content": [
                        {
                          "name": "Test Person",
                          "crn": "A000002",
                          "mobileNumber": "07000000002",
                          "manager": {
                            "name": "Test Staff",
                            "email": "test@example.com"
                          },
                          "probationDeliveryUnit": "Croydon"
                        },
                        {
                          "name": "Test Person",
                          "crn": "A000003",
                          "mobileNumber": "07000000002",
                          "manager": {
                            "name": "Test Staff",
                            "email": "test@example.com"
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
}
