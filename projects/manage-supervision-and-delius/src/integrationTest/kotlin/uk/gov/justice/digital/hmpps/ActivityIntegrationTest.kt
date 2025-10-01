package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivity
import uk.gov.justice.digital.hmpps.client.ActivitySearchRequest
import uk.gov.justice.digital.hmpps.client.ContactSearchResponse
import uk.gov.justice.digital.hmpps.client.ContactSearchResult
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.service.toActivity
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.objectMapper
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class ActivityIntegrationTest : IntegrationTestBase() {

    @Test
    fun `activity search calls probation offender activity search`() {

        val person = OVERVIEW
        val searchResponse = ContactSearchResponse(
            page = 0, totalResults = 10, totalPages = 11, size = 3,
            results = listOf(
                ContactSearchResult(
                    crn = person.crn,
                    id = ContactGenerator.FIRST_APPT_CONTACT.id
                ),
                ContactSearchResult(
                    crn = person.crn,
                    id = ContactGenerator.FIRST_NON_APPT_CONTACT.id
                ),
                ContactSearchResult(
                    crn = person.crn,
                    id = ContactGenerator.NEXT_APPT_CONTACT.id
                )
            )
        )
        wireMockServer.stubFor(
            WireMock.post(urlPathEqualTo("/probation-search/search/activity"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(searchResponse))
                )
        )

        val res = mockMvc
            .perform(
                post("/activity/${person.crn}").withToken()
                    .withJson(
                        ActivitySearchRequest(crn = person.crn)
                    )
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonActivity>()

        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.activities.size, equalTo(3))
        assertThat(res.activities[0].id, equalTo(ContactGenerator.FIRST_APPT_CONTACT.id))
        assertThat(
            res.activities[0].location?.officeName,
            equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().location?.officeName)
        )
        assertThat(res.activities[1].id, equalTo(ContactGenerator.FIRST_NON_APPT_CONTACT.id))
        assertThat(res.activities[2].id, equalTo(ContactGenerator.NEXT_APPT_CONTACT.id))
    }

    @Test
    fun `all person activity is returned`() {

        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/activity/${person.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonActivity>()

        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.activities.size, equalTo(9))
        assertThat(res.activities[0].isCommunication, equalTo(false))
        assertThat(res.activities[0].isSystemContact, equalTo(false))
        assertThat(res.activities[1].id, equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().id))
        assertThat(res.activities[1].type, equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().type))
        assertThat(
            res.activities[2].location?.officeName,
            equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().location?.officeName)
        )
        assertThat(res.activities[3].location?.postcode, equalTo("H34 7TH"))
        assertThat(res.activities[3].isAppointment, equalTo(true))
        assertThat(res.activities[0].documents.size, equalTo(3))
        assertThat(res.activities[4].isAppointment, equalTo(true))
        assertThat(res.activities[1].documents.size, equalTo(0))
        assertThat(res.activities[6].action, equalTo("Breach Enforcement Action"))
    }
}
