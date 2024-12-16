package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class KeyDateControllerTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var custodyRepository: CustodyRepository

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    @Order(1)
    fun `API call in dry run `() {
        val noms = PersonGenerator.DEFAULT.nomsId

        mockMvc
            .perform(post("/update-custody-dates?dryRun=true").withToken().withJson(listOf(noms)))
            .andExpect(status().is2xxSuccessful)

        channelManager.getChannel(queueName).waitUntilEmpty()

        verify(telemetryService, timeout(5000)).trackEvent(
            eq("KeyDatesDryRun"),
            org.mockito.kotlin.check {
                assertThat(it[CustodyDateType.SENTENCE_EXPIRY_DATE.code], Matchers.equalTo("2025-09-10"))
            },
            anyMap()
        )

        val custodyId = custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, "38339A").first()
        val custody = custodyRepository.findCustodyById(custodyId)
        val originalSedDate = LocalDate.parse("2024-08-10")
        custody.keyDates.filter { it.type.code == CustodyDateType.SENTENCE_EXPIRY_DATE.code }.forEach {
            assertThat(it.date, equalTo(originalSedDate))
        }
    }

    @Test
    @Order(2)
    fun `API call in update mode`() {
        val noms = PersonGenerator.PERSON_WITH_KEYDATES.nomsId

        mockMvc
            .perform(post("/update-custody-dates?dryRun=false").withToken().withJson(listOf(noms)))
            .andExpect(status().is2xxSuccessful)

        channelManager.getChannel(queueName).waitUntilEmpty()

        verify(telemetryService, timeout(5000)).trackEvent(
            eq("KeyDatesUpdated"),
            org.mockito.kotlin.check {
                assertThat(it[CustodyDateType.SENTENCE_EXPIRY_DATE.code], Matchers.equalTo("2024-01-16"))
            },
            anyMap()
        )

        val custodyId = custodyRepository.findCustodyId(PersonGenerator.PERSON_WITH_KEYDATES.id, "38340A").first()
        val custody = custodyRepository.findCustodyById(custodyId)
        verifyUpdatedKeyDates(custody)
    }

    @Test
    @Order(3)
    fun `API call in update mode for all nomsIds`() {
        mockMvc
            .perform(post("/update-custody-dates?dryRun=false").withToken())
            .andExpect(status().is2xxSuccessful)

        channelManager.getChannel(queueName).waitUntilEmpty()

        verify(telemetryService, timeout(5000)).trackEvent(
            eq("KeyDatesUpdated"),
            org.mockito.kotlin.check {
                assertThat(it[CustodyDateType.SENTENCE_EXPIRY_DATE.code], equalTo("2025-09-10"))
            },
            anyMap()
        )

        val custodyId =
            custodyRepository.findCustodyId(PersonGenerator.PERSON_WITH_KEYDATES_BY_CRN.id, "48340A").first()
        val custody = custodyRepository.findCustodyById(custodyId)
        val sed = custody.keyDate(CustodyDateType.SENTENCE_EXPIRY_DATE.code)
        assertThat(sed?.date, Matchers.equalTo(LocalDate.parse("2025-09-10")))
    }

    private fun verifyUpdatedKeyDates(custody: Custody) {
        val sed = custody.keyDate(CustodyDateType.SENTENCE_EXPIRY_DATE.code)
        val crd = custody.keyDate(CustodyDateType.AUTOMATIC_CONDITIONAL_RELEASE_DATE.code)
        val led = custody.keyDate(CustodyDateType.LICENCE_EXPIRY_DATE.code)
        val erd = custody.keyDate(CustodyDateType.EXPECTED_RELEASE_DATE.code)
        val hde = custody.keyDate(CustodyDateType.HDC_EXPECTED_DATE.code)

        assertThat(sed?.date, Matchers.equalTo(LocalDate.parse("2024-01-16")))
        assertThat(crd?.date, Matchers.equalTo(LocalDate.parse("2024-01-17")))
        assertThat(led?.date, Matchers.equalTo(LocalDate.parse("2024-01-18")))
        assertThat(erd?.date, Matchers.equalTo(LocalDate.parse("2024-01-19")))
        assertThat(hde?.date, Matchers.equalTo(LocalDate.parse("2024-01-20")))
    }

    private fun Custody.keyDate(code: String) = keyDates.firstOrNull { it.type.code == code }
}
