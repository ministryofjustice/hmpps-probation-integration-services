package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.mockito.Mockito.atLeastOnce
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.util.concurrent.TimeoutException

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `message is logged to telemetry`() {
        // Given a message
        val notification = Notification(message = MessageGenerator.EXAMPLE)

        // When it is received
        try {
            channelManager.getChannel(queueName).publishAndWait(notification)
        } catch (_: TimeoutException) {
            // Note: Remove this try/catch when the MessageListener logic has been implemented
        }

        // Then it is logged to telemetry
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
    }

    @Test
    fun `personal details 404`() {
        mockMvc
            .perform(get("/case/DOESNOTEXIST/personal-details").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `personal details success`() {
        mockMvc
            .perform(get("/case/A000001/personal-details").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(
                content().json(
                    """
                    {
                      "crn": "A000001",
                      "name": {
                        "forename": "Forename",
                        "middleNames": "MiddleName",
                        "surname": "Surname"
                      },
                      "dateOfBirth": "${LocalDate.now().minusYears(45).minusMonths(6)}",
                      "age": "45 years, 6 months",
                      "sex": {
                        "code": "M",
                        "description": "Male"
                      },
                      "ethnicity": {
                        "code": "A9",
                        "description": "Asian or Asian British: Other"
                      },
                      "probationPractitioner": {
                        "name": {
                          "forename": "Forename",
                          "surname": "Surname"
                        },
                        "email": "test@example.com"
                      },
                      "probationDeliveryUnit": {
                        "code": "PDU1",
                        "description": "Test PDU"
                      }
                    }
                    """.trimIndent(),
                    JsonCompareMode.STRICT,
                )
            )
    }
}
