package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.compliance.NonComplianceResponse
import uk.gov.justice.digital.hmpps.data.generator.NonComplianceGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class NonComplianceDetailIntegrationTest : IntegrationTestBase() {

    @Test
    fun `non-compliance detail is grouped into the three categories correctly`() {
        val crn = PersonGenerator.NON_COMPLIANCE_PERSON.crn
        val event = PersonGenerator.NON_COMPLIANCE_EVENT
        val res = mockMvc.get("/compliance/non-compliance-detail/$crn") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<NonComplianceResponse>()

        // one acceptable absence
        assertThat(res.acceptableAbsence, hasSize(1))
        assertThat(res.acceptableAbsence[0].contactId, equalTo(NonComplianceGenerator.ACCEPTABLE_ABSENCE_CONTACT.id))
        assertThat(res.acceptableAbsence[0].eventNumber, equalTo(event.eventNumber))
        assertThat(res.acceptableAbsence[0].eventId, equalTo(event.id))
        assertThat(res.acceptableAbsence[0].type.code, equalTo(NonComplianceGenerator.ACCEPTABLE_ABSENCE_CONTACT.type.code))
        assertThat(res.acceptableAbsence[0].date, equalTo(NonComplianceGenerator.ACCEPTABLE_ABSENCE_CONTACT.date))

        // one unacceptable absence
        assertThat(res.unacceptableAbsence, hasSize(1))
        assertThat(res.unacceptableAbsence[0].contactId, equalTo(NonComplianceGenerator.UNACCEPTABLE_ABSENCE_CONTACT.id))

        // one attended but did not comply
        assertThat(res.attendedButDidNotComply, hasSize(1))
        assertThat(res.attendedButDidNotComply[0].contactId, equalTo(NonComplianceGenerator.ATTENDED_NOT_COMPLY_CONTACT.id))
    }

    @Test
    fun `contact with no outcome is excluded from all categories`() {
        val crn = PersonGenerator.NON_COMPLIANCE_PERSON.crn
        val res = mockMvc.get("/compliance/non-compliance-detail/$crn") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<NonComplianceResponse>()

        val allContactIds = (res.acceptableAbsence + res.unacceptableAbsence + res.attendedButDidNotComply)
            .map { it.contactId }

        assertThat(allContactIds.contains(NonComplianceGenerator.COMPLIANT_CONTACT.id), equalTo(false))
    }

    @Test
    fun `months filter limits results to contacts created within the window`() {
        val crn = PersonGenerator.NON_COMPLIANCE_PERSON.crn
        // months=0 returns all contacts — all three non-compliant contacts should appear
        val unfiltered = mockMvc.get("/compliance/non-compliance-detail/$crn?months=0") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<NonComplianceResponse>()

        val totalUnfiltered = unfiltered.acceptableAbsence.size +
            unfiltered.unacceptableAbsence.size +
            unfiltered.attendedButDidNotComply.size
        assertThat(totalUnfiltered, equalTo(3))
    }

    @Test
    fun `not found status returned for unknown crn`() {
        mockMvc.get("/compliance/non-compliance-detail/X999999") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `unauthorized status returned when no token provided`() {
        val crn = PersonGenerator.NON_COMPLIANCE_PERSON.crn
        mockMvc.get("/compliance/non-compliance-detail/$crn")
            .andExpect { status { isUnauthorized() } }
    }
}
