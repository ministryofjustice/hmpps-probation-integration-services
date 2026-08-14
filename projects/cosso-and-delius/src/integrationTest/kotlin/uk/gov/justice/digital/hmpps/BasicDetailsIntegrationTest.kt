package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.model.CossoEventDocuments
import uk.gov.justice.digital.hmpps.model.DocumentCrn
import uk.gov.justice.digital.hmpps.model.PersonDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class BasicDetailsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `basic details returned with valid crn`() {
        val crn = PersonGenerator.DEFAULT_PERSON.crn
        val username = "J0nSm17h"
        val result = mockMvc.get("/basic-details/$crn/$username") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonDetails>()
        assertThat(result.addresses.size).isEqualTo(1)
        assertThat(result.name.surname).isEqualTo("Jones")
        assertThat(result.title).isEqualTo("Mr")
        assertThat(result.name.middleName).isEqualTo("Tom Billy")
        assertThat(result.addresses.get(0).status).isEqualTo("Main")
    }

    @Test
    fun `person with no home area throws bad request`() {
        val crn = PersonGenerator.PERSON_NO_MAIN_ADDRESS.crn
        val username = "NoHomeArea"
        mockMvc.get("/basic-details/$crn/$username") { withToken() }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `person with crn not found throws not found`() {
        val crn = "X123458"
        val username = "J0nSm17h"
        mockMvc.get("/basic-details/$crn") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `case endpoint returns crn for valid cosso id`() {
        val response = mockMvc.get("/case/${DocumentGenerator.DEFAULT_DOCUMENT_UUID}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<DocumentCrn>()

        assertThat(response.crn).isEqualTo(PersonGenerator.DEFAULT_PERSON.crn)
    }

    @Test
    fun `case endpoint returns not found for unknown cosso id`() {
        mockMvc.get("/case/${UUID.randomUUID()}") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `returns cosso ids for event documents`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val response = mockMvc.get("/cosso-event-documents/${person.crn}/${EventGenerator.DEFAULT_EVENT.number}") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CossoEventDocuments>()

        assertThat(response.cossoIdList)
            .containsExactlyInAnyOrder(
                DocumentGenerator.DEFAULT_DOCUMENT_UUID.toString(),
                "00000000-0000-0000-0000-000000000001",
                "00000000-0000-0000-0000-000000000003"
            )
    }

    @Test
    fun `returns empty cosso id list when event has no cosso documents`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val response =
            mockMvc.get("/cosso-event-documents/${person.crn}/${EventGenerator.NO_DOCUMENT_EVENT.number}") {
                withToken()
            }
                .andExpect { status { is2xxSuccessful() } }
                .andReturn().response.contentAsJson<CossoEventDocuments>()

        assertThat(response.cossoIdList).isEmpty()
    }

    @Test
    fun `returns cosso ids for terminated event`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val response =
            mockMvc.get("/cosso-event-documents/${person.crn}/${EventGenerator.TERMINATED_EVENT.number}") {
                withToken()
            }
                .andExpect { status { is2xxSuccessful() } }
                .andReturn().response.contentAsJson<CossoEventDocuments>()

        assertThat(response.cossoIdList)
            .containsExactly(DocumentGenerator.TERMINATED_EVENT_DOCUMENT_UUID.toString())
    }

    @Test
    fun `returns 404 when CRN is not found`() {
        mockMvc.get("/cosso-event-documents/UNKNOWN/${EventGenerator.DEFAULT_EVENT.number}") {
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
        mockMvc.get("/cosso-event-documents/${person.crn}/99") {
            withToken()
        }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Event with event number of 99 not found") }
            }
    }
}
