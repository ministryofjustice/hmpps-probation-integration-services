package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.DEFAULT_CUSTODY
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact.ContactRepository
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.CustodyDateChanged
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture

@SpringBootTest
internal class IntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}")
    private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val contactRepository: ContactRepository,
    private val custodyRepository: CustodyRepository
) {

    @MockitoBean
    lateinit var featureFlags: FeatureFlags

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    private val sedDate = "2025-09-10"

    @Test
    fun `Custody Key Dates updated as expected`() {
        whenever(featureFlags.enabled("sds-plus-flag-enabled")).thenReturn(false)
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
        assertThat(custody.disposal?.sdsPlus, equalTo(null))
        verifyUpdatedKeyDates(custody)
        verifyContactCreated()

        assertNull(custody.keyDate(CustodyDateType.PRESUMPTIVE_EM_END_DATE.code))
        assertNull(custody.keyDate(CustodyDateType.FINAL_THIRD_START_DATE.code))

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
        whenever(featureFlags.enabled("sds-plus-flag-enabled")).thenReturn(false)
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
        val emed = custody.keyDate(CustodyDateType.PRESUMPTIVE_EM_END_DATE.code)
        val fthrd = custody.keyDate(CustodyDateType.FINAL_THIRD_START_DATE.code)

        assertThat(sed?.date, equalTo(LocalDate.parse(sedDate)))
        assertThat(crd?.date, equalTo(LocalDate.parse("2022-11-26")))
        assertThat(led?.date, equalTo(LocalDate.parse("2025-09-11")))
        assertThat(erd?.date, equalTo(LocalDate.parse("2022-11-27")))
        assertThat(hde?.date, equalTo(LocalDate.parse("2022-10-28")))
        assertThat(pr1?.date, equalTo(LocalDate.parse("2024-10-05")))
        assertNull(emed)
        assertNull(fthrd)

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

    @Test
    fun `PSSED key date is added when disposal type has pss requirement`() {
        val noms = PersonGenerator.PSS_PERSON.nomsId
        val notification = Notification(
            message = MessageGenerator.SENTENCE_DATE_CHANGED,
            attributes = MessageAttributes(eventType = "SENTENCE_DATES-CHANGED")
        )
        // Override to use PSS person's booking
        val pssNotification = notification.copy(
            message = ResourceLoader.message<CustodyDateChanged>("sentence-date-changed-pss")
        )
        channelManager.getChannel(queueName).publishAndWait(pssNotification)

        val custodyId = custodyRepository.findCustodyId(PersonGenerator.PSS_PERSON.id, "68340A").first()
        val custody = custodyRepository.findCustodyById(custodyId)
        val pssed = custody.keyDates.firstOrNull { it.type.code == "PSSED" }
        assertNotNull(pssed)
        assertThat(pssed!!.date, equalTo(LocalDate.parse("2026-06-15")))
    }

    @Test
    fun `EMED and FTHRD dates created and disposal updated when feature flag enabled`() {
        whenever(featureFlags.enabled("sds-plus-flag-enabled")).thenReturn(true)
        val notification = Notification(message = MessageGenerator.SENTENCE_DATE_CHANGED_SDS)
        channelManager.getChannel(queueName).publishAndWait(notification)
        val custodyId = custodyRepository.findCustodyId(PersonGenerator.SDS_PLUS_PERSON.id, "78340A").first()
        val custody = custodyRepository.findCustodyById(custodyId)
        assertThat(custody.disposal?.sdsPlus, equalTo(true))
        assertThat(custody.disposal?.lastModifiedUserId, equalTo(UserGenerator.AUDIT_USER.id))
        assertThat(custody.disposal?.version, equalTo(1L))
        assertNotNull(custody.disposal?.lastModifiedDate)
        assertThat(
            custody.keyDate(CustodyDateType.PRESUMPTIVE_EM_END_DATE.code)?.date,
            equalTo(LocalDate.parse("2025-05-11"))
        )
        assertThat(
            custody.keyDate(CustodyDateType.FINAL_THIRD_START_DATE.code)?.date,
            equalTo(LocalDate.parse("2025-05-11"))
        )
    }
}
