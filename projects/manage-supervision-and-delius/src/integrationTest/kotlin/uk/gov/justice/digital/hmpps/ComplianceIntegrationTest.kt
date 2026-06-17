package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.compliance.PersonCompliance
import uk.gov.justice.digital.hmpps.data.generator.MonthsFilterGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.INACTIVE_EVENT_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.INACTIVE_ORDER_2
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.TERMINATION_REASON
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class ComplianceIntegrationTest : IntegrationTestBase() {

    @Test
    fun `compliance details are returned`() {
        val person = OVERVIEW
        val res = mockMvc.get("/compliance/${person.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonCompliance>()
        assertThat(res.personSummary.name.forename, equalTo(person.forename))
        assertThat(
            res.previousOrders.breaches,
            equalTo(2)
        )
        assertThat(res.currentSentences[1].rarCategory, equalTo("Main"))
        assertThat(res.currentSentences[1].rarDescription, equalTo("2 of 12 RAR days completed"))
        assertThat(res.currentSentences[0].rarCategory, equalTo(null))
        assertThat(res.currentSentences[1].eventNumber, equalTo("7654321"))
        assertThat(res.currentSentences[0].eventNumber, equalTo("1234567"))
        assertThat(res.currentSentences[1].activeBreach?.status, equalTo("An NSI Status"))
        assertThat(res.currentSentences[1].activeRecall?.status, equalTo("Standard"))
        assertThat(res.currentSentences[1].compliance.breachStarted, equalTo(true))
        assertThat(res.currentSentences[1].compliance.currentBreaches, equalTo(1))
        assertThat(res.currentSentences[1].compliance.priorBreachesOnCurrentOrderCount, equalTo(0))
        assertThat(res.currentSentences[1].activity.waitingForEvidenceCount, equalTo(0))
        assertThat(res.currentSentences[1].activity.compliedAppointmentsCount, equalTo(2))
        assertThat(res.currentSentences[1].activity.outcomeNotRecordedCount, equalTo(2))
        assertThat(res.currentSentences[1].activity.acceptableAbsenceCount, equalTo(0))
        assertThat(res.previousOrders.orders[3].eventNumber, equalTo(INACTIVE_EVENT_1.eventNumber))
        assertThat(res.previousOrders.orders[3].status, equalTo(TERMINATION_REASON.description))
        assertThat(res.previousOrders.lastEndedDate, equalTo(INACTIVE_ORDER_2.terminationDate))
    }

    @Test
    fun `not found status returned`() {
        mockMvc.get("/compliance/X123456") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `unauthorized status returned`() {
        mockMvc.get("/compliance/X123456")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `months filter scopes activity and breach counts to the time window`() {
        val crn = MonthsFilterGenerator.PERSON.crn

        // With months=0 (no filter) both events appear and both breaches are counted
        val unfiltered = mockMvc.get("/compliance/$crn?months=0") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonCompliance>()

        val unfilteredEventNumbers = unfiltered.currentSentences.map { it.eventNumber }
        assertThat(unfilteredEventNumbers.contains(MonthsFilterGenerator.RECENT_EVENT.eventNumber), equalTo(true))
        assertThat(unfilteredEventNumbers.contains(MonthsFilterGenerator.OLD_EVENT.eventNumber), equalTo(true))

        // Both breaches are counted — one on each sentence
        val unfilteredTotalBreaches = unfiltered.currentSentences.sumOf { it.compliance.currentBreaches }
        assertThat(unfilteredTotalBreaches, equalTo(2))

        // With months=6, both events still appear (events are never filtered)
        // but only the recent breach (2 months ago) is within the window;
        // the old breach (10 months ago) is excluded from counts
        val filtered = mockMvc.get("/compliance/$crn?months=6") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonCompliance>()

        val filteredEventNumbers = filtered.currentSentences.map { it.eventNumber }
        assertThat(filteredEventNumbers.contains(MonthsFilterGenerator.RECENT_EVENT.eventNumber), equalTo(true))
        assertThat(filteredEventNumbers.contains(MonthsFilterGenerator.OLD_EVENT.eventNumber), equalTo(true))

        val filteredTotalBreaches = filtered.currentSentences.sumOf { it.compliance.currentBreaches }
        assertThat(filteredTotalBreaches, equalTo(1))
    }
}
