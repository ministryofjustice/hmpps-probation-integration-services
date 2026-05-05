package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.contact.ContactOutcomes
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.UpdateContactOutcomeGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class ContactOutcomesIntegrationTest : IntegrationTestBase() {

    @Test
    fun `returns empty outcomes list when contact type code does not exist`() {
        val response = mockMvc.get("/contact/types/INVALID/outcomes") {
            withToken()
        }.andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<ContactOutcomes>()

        assertThat(response.outcomes, hasSize(0))
    }

    @Test
    fun `returns outcomes for a valid contact type`() {
        val typeCode = ContactGenerator.APPT_CT_1.code // "C089"

        val response = mockMvc.get("/contact/types/$typeCode/outcomes") {
            withToken()
        }.andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<ContactOutcomes>()

        assertThat(response.outcomes, hasSize(1))
        assertThat(response.outcomes[0].code, equalTo(ContactGenerator.ACCEPTABLE_ABSENCE.code))
        assertThat(response.outcomes[0].description, equalTo(ContactGenerator.ACCEPTABLE_ABSENCE.description))
    }

    @Test
    fun `returns empty outcomes list when contact type has no outcomes`() {
        // EMAIL_POP_CT ("CMOB") has no outcomes wired in test data
        val typeCode = ContactGenerator.EMAIL_POP_CT.code

        val response = mockMvc.get("/contact/types/$typeCode/outcomes") {
            withToken()
        }.andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<ContactOutcomes>()

        assertThat(response.outcomes, hasSize(0))
    }

    @Test
    fun `returns outcomes and linked enforcement actions for a valid contact type`() {
        val typeCode = UpdateContactOutcomeGenerator.CONTACT_TYPE.code // "CCON"

        val response = mockMvc.get("/contact/types/$typeCode/outcomes") {
            withToken()
        }.andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<ContactOutcomes>()

        assertThat(response.outcomes, hasSize(1))
        assertThat(response.outcomes[0].code, equalTo(UpdateContactOutcomeGenerator.OUTCOME.code))
        assertThat(response.outcomes[0].description, equalTo(UpdateContactOutcomeGenerator.OUTCOME.description))
        assertThat(response.outcomes[0].enforcementActions, hasSize(1))
        assertThat(
            response.outcomes[0].enforcementActions[0].code,
            equalTo(UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.code)
        )
        assertThat(
            response.outcomes[0].enforcementActions[0].description,
            equalTo(UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.description)
        )
        assertThat(
            response.outcomes[0].enforcementActions[0].defaultResponsePeriodDays,
            equalTo(UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.responseByPeriod)
        )
    }
}

