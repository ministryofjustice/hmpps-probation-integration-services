package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.DEFAULT_CUSTODY
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact.ContactRepository
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture

@SpringBootTest
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Autowired
    lateinit var custodyRepository: CustodyRepository

    private val sedDate = "2025-09-10"

    @Test
    fun `Custody Key Dates updated as expected`() {
        val notification = Notification(message = MessageGenerator.SENTENCE_DATE_CHANGED)

        val first = CompletableFuture.runAsync {
            channelManager.getChannel(queueName).publishAndWait(notification)
        }
        val second = CompletableFuture.runAsync {
            channelManager.getChannel(queueName).publishAndWait(notification)
        }

        CompletableFuture.allOf(first, second).join()

        verify(telemetryService, times(2)).notificationReceived(notification)

        val custodyId = custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, DEFAULT_CUSTODY.bookingRef).first()
        val custody = custodyRepository.findCustodyById(custodyId)
        verifyUpdatedKeyDates(custody)
        verifyContactCreated()

        verify(telemetryService).trackEvent(
            eq("KeyDatesUpdated"),
            check {
                assertThat(it[CustodyDateType.SENTENCE_EXPIRY_DATE.code], equalTo(sedDate))
            },
            anyMap()
        )

        verify(telemetryService).trackEvent(
            eq("KeyDatesUnchanged"),
            anyMap(),
            anyMap()
        )
    }

    @Test
    fun `Custody Key Dates updated from SENTENCE_CHANGED event`() {
        val notification = Notification(
            message = MessageGenerator.SENTENCE_CHANGED,
            attributes = MessageAttributes(eventType = "SENTENCE_CHANGED")
        )

        val custodyId =
            custodyRepository.findCustodyId(PersonGenerator.PERSON_WITH_KEYDATES_BY_CRN.id, "48340A").first()

        var custody = custodyRepository.findCustodyById(custodyId)

        //check key date is soft deleted b4 message is processed
        val led = custody.keyDate(CustodyDateType.LICENCE_EXPIRY_DATE.code)
        assertThat(led?.softDeleted, equalTo(true))

        val first = CompletableFuture.runAsync {
            channelManager.getChannel(queueName).publishAndWait(notification)
        }
        val second = CompletableFuture.runAsync {
            channelManager.getChannel(queueName).publishAndWait(notification)
        }

        CompletableFuture.allOf(first, second).join()

        verify(telemetryService, times(2)).notificationReceived(notification)


        custody = custodyRepository.findCustodyById(custodyId)
        verifyUpdatedKeyDates(custody)

        verify(telemetryService).trackEvent(
            eq("KeyDatesUpdated"),
            check {
                assertThat(it[CustodyDateType.SENTENCE_EXPIRY_DATE.code], equalTo(sedDate))
            },
            anyMap()
        )

        verify(telemetryService).trackEvent(
            eq("KeyDatesUnchanged"),
            anyMap(),
            anyMap()
        )
    }

    private fun verifyUpdatedKeyDates(custody: Custody) {
        val sed = custody.keyDate(CustodyDateType.SENTENCE_EXPIRY_DATE.code)
        val crd = custody.keyDate(CustodyDateType.AUTOMATIC_CONDITIONAL_RELEASE_DATE.code)
        val led = custody.keyDate(CustodyDateType.LICENCE_EXPIRY_DATE.code)
        val erd = custody.keyDate(CustodyDateType.EXPECTED_RELEASE_DATE.code)
        val hde = custody.keyDate(CustodyDateType.HDC_EXPECTED_DATE.code)
        val pr1 = custody.keyDate(CustodyDateType.SUSPENSION_DATE_IF_RESET.code)

        assertThat(sed?.date, equalTo(LocalDate.parse(sedDate)))
        assertThat(crd?.date, equalTo(LocalDate.parse("2022-11-26")))
        assertThat(led?.date, equalTo(LocalDate.parse("2025-09-11")))
        assertThat(erd?.date, equalTo(LocalDate.parse("2022-11-27")))
        assertThat(hde?.date, equalTo(LocalDate.parse("2022-10-28")))
        assertThat(pr1?.date, equalTo(LocalDate.parse("2024-10-05")))

        assertThat(led?.softDeleted, equalTo(false))
    }

    private fun verifyContactCreated() {
        val event = DEFAULT_CUSTODY.disposal!!.event
        val contact = contactRepository.findAll()
            .firstOrNull { it.personId == PersonGenerator.DEFAULT.id && it.eventId == event.id }
        assertNotNull(contact)
        assertThat(
            contact!!.date.truncatedTo(ChronoUnit.DAYS),
            equalTo(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS))
        )
        assertThat(contact.staffId, equalTo(3))
        assertThat(contact.teamId, equalTo(2))
        assertThat(contact.providerId, equalTo(1))
        assertThat(
            contact.notes,
            equalTo(
                """
            LED 11/09/2025
            ACR 26/11/2022
            SED 10/09/2025
            EXP 27/11/2022
            HDE 28/10/2022
            PR1 05/10/2024
                """.trimIndent()
            )
        )
    }

    private fun Custody.keyDate(code: String) = keyDates.firstOrNull { it.type.code == code }
}
