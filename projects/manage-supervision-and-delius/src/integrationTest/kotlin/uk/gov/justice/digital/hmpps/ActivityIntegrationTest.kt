package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.hamcrest.MatcherAssert.assertThat
import java.time.ZonedDateTime
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivity
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

        val res = mockMvc.post("/activity/${person.crn}") {
            withToken()
            json = ActivitySearchRequest(crn = person.crn)
        }
            .andExpect {
                status { isOk() }
            }
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
    fun `all person activity is returned split into past and future`() {
        val person = OVERVIEW
        val res = mockMvc.get("/activity/${person.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonActivity>()

        assertThat(res.personSummary.crn, equalTo(person.crn))

        assertThat(res.activities.size + res.futureActivities.size, equalTo(10))

        assertThat(res.activities.all { it.isInPast }, equalTo(true))
        assertThat(
            res.activities.map { it.startDateTime },
            equalTo(res.activities.map { it.startDateTime }.sortedDescending())
        )
        assertThat(res.activities.any { it.action == "Breach Enforcement Action" }, equalTo(true))

        assertThat(res.futureActivities.none { it.isInPast }, equalTo(true))

        // Compute which fixture contacts are still in the future at assertion time, since the
        // plusHours(...) timestamps can slip to the past on slow or paused runs
        val now = ZonedDateTime.now()
        val expectedFutureIds = listOf(
            ContactGenerator.FIRST_NON_APPT_CONTACT,
            ContactGenerator.FIRST_APPT_CONTACT,
            ContactGenerator.NEXT_APPT_CONTACT
        ).filter { it.startTime?.isAfter(now) == true }.map { it.id }
        assertThat(
            res.futureActivities.map { it.id }.filter { it in expectedFutureIds },
            equalTo(expectedFutureIds)
        )

        val allActivities = res.activities + res.futureActivities
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
}
