package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.config.JobConfig
import uk.gov.justice.digital.hmpps.config.TrialConfig
import uk.gov.justice.digital.hmpps.model.UnpaidWorkAppointment
import uk.gov.justice.digital.hmpps.repository.UpwAppointmentRepository
import uk.gov.justice.digital.hmpps.service.UnpaidWorkAppointmentsService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationList
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UnpaidWorkIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var unpaidWorkAppointmentsService: UnpaidWorkAppointmentsService

    @Autowired
    lateinit var jobConfig: JobConfig

    @MockitoBean
    lateinit var upwAppointmentRepository: UpwAppointmentRepository

    @MockitoBean
    lateinit var notificationClient: NotificationClient

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @BeforeEach
    fun setup() {
        whenever(upwAppointmentRepository.getUnpaidWorkAppointments(any(), eq("N07"), any(), any(), any(), any()))
            .thenAnswer { invocation ->
                (1..9).map { num ->
                    object : UnpaidWorkAppointment {
                        override val firstName = "Test"
                        override val mobileNumber = "0700000000$num"
                        override val appointmentDate = "01/01/2000"
                        override val crn = "A00000$num"
                        override val eventNumbers = "1"
                        override val upwAppointmentIds = num.toString()
                    }
                }.filter {
                    val excludedCrns = invocation.getArgument<List<String>>(4)
                    excludedCrns == null || it.crn !in excludedCrns
                }
            }

        whenever(notificationClient.getNotifications(isNull(), eq("sms"), isNull(), anyOrNull())) // language=json
            .thenReturn(NotificationList("""{"notifications": [], "links": {"current": "current-url"}}"""))
    }

    @Test
    fun `sends messages to govuk notify`() {
        unpaidWorkAppointmentsService.sendUnpaidWorkAppointmentReminders(jobConfig)

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
                "upwAppointmentIds" to "1",
                "providerCode" to "N07",
                "templateIds" to "template",
                "notificationIds" to "null"
            )
        )
    }

    @Test
    fun `distributes CRNs among trial templates`() {
        val config = jobConfig.copy(
            trials = listOf(TrialConfig(listOf("trial1")), TrialConfig(listOf("trial2")), TrialConfig(listOf("trial3")))
        )
        unpaidWorkAppointmentsService.sendUnpaidWorkAppointmentReminders(config)

        // All templates are used
        verify(notificationClient, atLeastOnce()).sendSms(eq("template"), any(), any(), any())
        verify(notificationClient, atLeastOnce()).sendSms(eq("trial1"), any(), any(), any())
        verify(notificationClient, atLeastOnce()).sendSms(eq("trial2"), any(), any(), any())
        verify(notificationClient, atLeastOnce()).sendSms(eq("trial3"), any(), any(), any())

        // Assigned template is fixed (does not change between test runs)
        verify(notificationClient).sendSms(eq("trial1"), any(), any(), eq("A000001"))
    }

    @Test
    fun `do not send messages if one has already been sent for that crn today`() {
        whenever(notificationClient.getNotifications(isNull(), eq("sms"), isNull(), anyOrNull())).thenReturn(
            NotificationList(
                // language=json
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

        unpaidWorkAppointmentsService.sendUnpaidWorkAppointmentReminders(jobConfig)

        verify(notificationClient, never()).sendSms(
            "template",
            "07000000001",
            mapOf("FirstName" to "Test", "NextWorkSession" to "01/01/2000"),
            "A000001"
        )
    }
}
