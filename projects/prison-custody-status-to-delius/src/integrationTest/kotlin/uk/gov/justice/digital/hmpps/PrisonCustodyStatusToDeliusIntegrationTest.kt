package uk.gov.justice.digital.hmpps

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.RELEASE_FROM_CUSTODY
import uk.gov.justice.digital.hmpps.integrations.delius.custody.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.history.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode.LOCATION_CHANGE
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode.STATUS_CHANGE
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseRepository
import uk.gov.justice.digital.hmpps.jms.convertSendAndWait
import uk.gov.justice.digital.hmpps.listener.nomsNumber
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@SpringBootTest
@ActiveProfiles("integration-test")
internal class PrisonCustodyStatusToDeliusIntegrationTest {

    @Value("\${spring.jms.template.default-destination}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var releaseRepository: ReleaseRepository

    @Autowired
    private lateinit var custodyRepository: CustodyRepository

    @Autowired
    private lateinit var custodyHistoryRepository: CustodyHistoryRepository

    @Autowired
    private lateinit var contactRepository: ContactRepository

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `release a prisoner`() {
        // given a prisoner who is in custody
        assertTrue(custodyRepository.findAll().single().isInCustody())

        // when they are released
        val message = MessageGenerator.PRISONER_RELEASED
        jmsTemplate.convertSendAndWait(queueName, message)

        // then they are no longer in custody
        assertFalse(custodyRepository.findAll().single().isInCustody())

        // and the release information is recorded correctly
        val release = releaseRepository.findAll().single()
        assert(release.createdDatetime.closeTo(ZonedDateTime.now()))
        assertThat(release.date.withZoneSameInstant(EuropeLondon), equalTo(message.occurredAt))
        assertThat(release.person.nomsNumber, equalTo(message.additionalInformation.nomsNumber()))

        // and the history is recorded correctly
        val custodyHistory = custodyHistoryRepository.findAll()
        assertThat(custodyHistory, hasSize(2))
        assertThat(custodyHistory.map { it.type.code }, hasItems(STATUS_CHANGE.code, LOCATION_CHANGE.code))
        assertThat(custodyHistory.map { it.detail }, hasItems("Released on Licence", "Test institution"))

        // and a contact is recorded
        val contact = contactRepository.findAll().single()
        assertThat(contact.type.code, equalTo(RELEASE_FROM_CUSTODY.code))
        assertThat(contact.notes, equalTo("Release Type: description of ADL"))

        // and telemetry is updated
        verify(telemetryService).trackEvent(
            "PrisonerReleased",
            mapOf(
                "nomsNumber" to "A0001AA",
                "institution" to "WSI",
                "reason" to "RELEASED",
                "details" to "Movement reason code NCS"
            )
        )
    }

    @Test
    fun `recall a prisoner`() {
        val message = MessageGenerator.PRISONER_RECEIVED

        jmsTemplate.convertSendAndWait(queueName, message)

        verify(telemetryService).trackEvent(
            "PrisonerRecalled",
            mapOf(
                "nomsNumber" to "A0001AA",
                "institution" to "WSI",
                "reason" to "ADMISSION",
                "details" to "ACTIVE IN:ADM-L"
            )
        )
    }
}
