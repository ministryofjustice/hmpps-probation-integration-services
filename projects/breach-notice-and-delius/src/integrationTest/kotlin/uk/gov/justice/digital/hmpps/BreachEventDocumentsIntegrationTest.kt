package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.BREACH_NOTICE_ID
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.EVENT_LEVEL_BREACH_NOTICE_ID
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.TERMINATED_EVENT_BREACH_NOTICE_ID
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.UPW_BREACH_NOTICE_ID
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.DEFAULT_EVENT
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.NO_DOCUMENT_EVENT
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.TERMINATED_EVENT
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.model.BreachNoticeDocuments
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

internal class BreachEventDocumentsIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `returns breach ids from event, contact and upw appointment documents`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val response = mockMvc.get("/breach-event-documents/${person.crn}/${DEFAULT_EVENT.number}") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<BreachNoticeDocuments>()

        assertThat(response.breachIdList)
            .containsExactlyInAnyOrder(
                BREACH_NOTICE_ID.toString(),
                EVENT_LEVEL_BREACH_NOTICE_ID.toString(),
                UPW_BREACH_NOTICE_ID.toString()
            )
    }

    @Test
    fun `returns empty breach id list when event has no breach documents`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val response = mockMvc.get("/breach-event-documents/${person.crn}/${NO_DOCUMENT_EVENT.number}") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<BreachNoticeDocuments>()

        assertThat(response.breachIdList).isEmpty()
    }

    @Test
    fun `returns breach ids for terminated event`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val response = mockMvc.get("/breach-event-documents/${person.crn}/${TERMINATED_EVENT.number}") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<BreachNoticeDocuments>()

        assertThat(response.breachIdList)
            .containsExactly(TERMINATED_EVENT_BREACH_NOTICE_ID.toString())
    }

    @Test
    fun `returns 404 when CRN is not found`() {
        mockMvc.get("/breach-event-documents/UNKNOWN/${DEFAULT_EVENT.number}") {
            withToken()
        }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Person with crn of UNKNOWN not found") }
            }
    }

    @Test
    fun `returns 404 when event number is not found for a valid CRN`() {
        val person = PersonGenerator.DEFAULT_PERSON
        mockMvc.get("/breach-event-documents/${person.crn}/99") {
            withToken()
        }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Event with event number of 99 not found") }
            }
    }
}
