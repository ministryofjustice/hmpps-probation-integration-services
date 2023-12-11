package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.DEFAULT_CUSTODY
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact.ContactRepository
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.time.Duration
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

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Autowired
    lateinit var custodyRepository: CustodyRepository

    @Test
    fun `Custody Key Dates updated as expected`() {
        val notification = Notification(message = MessageGenerator.SENTENCE_DATE_CHANGED)

        val first = CompletableFuture.runAsync {
            channelManager.getChannel(queueName).publishAndWait(notification, Duration.ofMinutes(3))
        }
        val second = CompletableFuture.runAsync {
            channelManager.getChannel(queueName).publishAndWait(notification, Duration.ofMinutes(3))
        }

        CompletableFuture.allOf(first, second).join()

        verify(telemetryService, times(2)).notificationReceived(notification)

        val custodyId = custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, DEFAULT_CUSTODY.bookingRef).first()
        val custody = custodyRepository.findCustodyById(custodyId)
        verifyUpdatedKeyDates(custody)
        verifyDeletedKeyDate(custody)
        verifyContactCreated()

        verify(telemetryService).trackEvent(
            eq("KeyDatesUpdated"),
            check {
                assertThat(it[CustodyDateType.SENTENCE_EXPIRY_DATE.code], equalTo("2025-09-10"))
                assertThat(it[CustodyDateType.PAROLE_ELIGIBILITY_DATE.code], equalTo("deleted"))
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

        assertThat(sed?.date, equalTo(LocalDate.parse("2025-09-10")))
        assertThat(crd?.date, equalTo(LocalDate.parse("2022-11-26")))
        assertThat(led?.date, equalTo(LocalDate.parse("2025-09-11")))
        assertThat(erd?.date, equalTo(LocalDate.parse("2022-11-27")))
        assertThat(hde?.date, equalTo(LocalDate.parse("2022-10-28")))
    }

    private fun verifyDeletedKeyDate(custody: Custody) {
        assertNull(custody.keyDate(CustodyDateType.PAROLE_ELIGIBILITY_DATE.code))
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
            Removed PED 26/10/2022
                """.trimIndent()
            )
        )
    }

    private fun Custody.keyDate(code: String) = keyDates.firstOrNull { it.type.code == code }
}
