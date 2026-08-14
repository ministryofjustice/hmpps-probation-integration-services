package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivity
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivitySearchRequest
import uk.gov.justice.digital.hmpps.client.ActivitySearchRequest
import uk.gov.justice.digital.hmpps.client.ContactSearchResponse
import uk.gov.justice.digital.hmpps.client.ContactSearchResult
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.E_SUPERVISION_ID
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.E_SUP_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.service.toActivity
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.objectMapper
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class ActivityIntegrationTest : IntegrationTestBase() {

    @Test
    fun `activity search returns results sorted in date descending order`() {
        val person = OVERVIEW
        val searchResponse = ContactSearchResponse(
            page = 0, totalResults = 5, totalPages = 1, size = 5,
            results = listOf(
                ContactSearchResult(crn = person.crn, id = ContactGenerator.PREVIOUS_APPT_CONTACT.id),
                ContactSearchResult(crn = person.crn, id = ContactGenerator.NEXT_APPT_CONTACT.id),
                ContactSearchResult(crn = person.crn, id = ContactGenerator.FIRST_APPT_CONTACT.id),
                ContactSearchResult(crn = person.crn, id = ContactGenerator.FIRST_NON_APPT_CONTACT.id),
                ContactSearchResult(crn = person.crn, id = ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.id),
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

        val res = mockMvc.post("/activity/${person.crn}") {
            withToken()
            json = ActivitySearchRequest(crn = person.crn)
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonActivity>()

        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.activities.size, equalTo(5))
        assertThat(
            res.activities.map { it.startDateTime },
            equalTo(res.activities.map { it.startDateTime }.sortedDescending())
        )

        wireMockServer.verify(
            postRequestedFor(urlPathEqualTo("/probation-search/search/activity"))
        )
    }

    @Test
    fun `all person activity is returned sorted in date descending order`() {
        val person = OVERVIEW
        val res = mockMvc.get("/activity/${person.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonActivity>()

        assertThat(res.personSummary.crn, equalTo(person.crn))

        assertThat(res.activities.size, equalTo(10))

        assertThat(
            res.activities.map { it.startDateTime },
            equalTo(res.activities.map { it.startDateTime }.sortedDescending())
        )
        assertThat(res.activities.any { it.action == "Breach Enforcement Action" }, equalTo(true))
        assertThat(res.activities.first { it.isCommunication }.editable, equalTo(false))

        val allActivities = res.activities
        val nextAppt = allActivities.single { it.id == ContactGenerator.NEXT_APPT_CONTACT.id }
        assertThat(nextAppt.isAppointment, equalTo(true))
        assertThat(nextAppt.documents.size, equalTo(3))
        assertThat(nextAppt.location?.postcode, equalTo("H34 7TH"))

        val firstAppt = allActivities.single { it.id == ContactGenerator.FIRST_APPT_CONTACT.id }
        assertThat(firstAppt.type, equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().type))
        assertThat(
            firstAppt.location?.officeName,
            equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().location?.officeName)
        )
        assertThat(firstAppt.documents.size, equalTo(0))
    }

    @Test
    fun `can retrieve e supervision uuid`() {
        val person = E_SUP_PERSON
        val res = mockMvc.get("/activity/${person.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonActivity>()

        assertThat(res.activities.size, equalTo(1))
        assertThat(res.activities.first().esupervisionId, equalTo(E_SUPERVISION_ID))
    }

    @Test
    fun `activity search forwards filter fields to probation search`() {
        val person = OVERVIEW
        val searchResponse = ContactSearchResponse(
            page = 0, totalResults = 0, totalPages = 0, size = 10,
            results = emptyList()
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

        val searchRequest = PersonActivitySearchRequest(
            keywords = "test",
            filterBySparksContacts = true,
            filterBySupervisionPackageContacts = true,
        )

        mockMvc.post("/activity/${person.crn}") {
            withToken()
            json = searchRequest
        }.andExpect { status { isOk() } }

        wireMockServer.verify(
            postRequestedFor(urlPathEqualTo("/probation-search/search/activity"))
                .withRequestBody(matchingJsonPath("$.filterBySparksContacts", WireMock.equalTo("true")))
                .withRequestBody(matchingJsonPath("$.filterBySupervisionPackageContacts", WireMock.equalTo("true")))
                .withRequestBody(matchingJsonPath("$.keywords", WireMock.equalTo("test")))
        )
    }

    @Test
    fun `v2 activity search calls probation search semantic endpoint`() {
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
            WireMock.post(urlPathEqualTo("/probation-search/search/contacts"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(searchResponse))
                )
        )

        val res = mockMvc.post("/activity/${person.crn}/v2") {
            withToken()
            json = ActivitySearchRequest(crn = person.crn)
        }
            .andExpect {
                status { isOk() }
            }
            .andReturn().response.contentAsJson<PersonActivity>()

        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.activities.size, equalTo(3))
        assertThat(res.activities[0].id, equalTo(ContactGenerator.NEXT_APPT_CONTACT.id))
        assertThat(res.activities[1].id, equalTo(ContactGenerator.FIRST_APPT_CONTACT.id))
        assertThat(res.activities[2].id, equalTo(ContactGenerator.FIRST_NON_APPT_CONTACT.id))

        wireMockServer.verify(
            postRequestedFor(urlPathEqualTo("/probation-search/search/contacts"))
        )
    }

    @Test
    fun `v2 activity search forwards filter fields to probation search semantic endpoint`() {
        val person = OVERVIEW
        val searchResponse = ContactSearchResponse(
            page = 0, totalResults = 0, totalPages = 0, size = 10,
            results = emptyList()
        )
        wireMockServer.stubFor(
            WireMock.post(urlPathEqualTo("/probation-search/search/contacts"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(searchResponse))
                )
        )

        val searchRequest = PersonActivitySearchRequest(
            keywords = "test",
            filterBySparksContacts = true,
            filterBySupervisionPackageContacts = true,
        )

        mockMvc.post("/activity/${person.crn}/v2") {
            withToken()
            json = searchRequest
        }.andExpect { status { isOk() } }

        wireMockServer.verify(
            postRequestedFor(urlPathEqualTo("/probation-search/search/contacts"))
                .withRequestBody(matchingJsonPath("$.filterBySparksContacts", WireMock.equalTo("true")))
                .withRequestBody(matchingJsonPath("$.filterBySupervisionPackageContacts", WireMock.equalTo("true")))
                .withRequestBody(matchingJsonPath("$.keywords", WireMock.equalTo("test")))
        )
    }

    @Test
    fun `preload endpoint calls probation search preload for crn`() {
        val person = OVERVIEW
        wireMockServer.stubFor(
            WireMock.get(urlPathEqualTo("/probation-search/search/contacts/preload/${person.crn}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"ok\"}")
                )
        )

        mockMvc.get("/activity/${person.crn}/preload") {
            withToken()
        }.andExpect { status { isOk() } }

        wireMockServer.verify(
            getRequestedFor(urlPathEqualTo("/probation-search/search/contacts/preload/${person.crn}"))
        )
    }
}
