package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.contact.EnforcementContactResponse
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER_2
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class EnforcementContactIntegrationTest : IntegrationTestBase() {

    @Test
    fun `user without staff record returns empty response`() {
        val response = mockMvc.get("/contact/${USER_2.username}/enforcements") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<EnforcementContactResponse>()

        assertThat(response.totalResults, equalTo(0))
        assertThat(response.enforcementContacts.size, equalTo(0))
    }

    @Test
    fun `default pagination returns all enforcement contacts`() {
        val response = mockMvc.get("/contact/${USER.username}/enforcements") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<EnforcementContactResponse>()

        assertThat(response.totalResults, equalTo(3))
        assertThat(response.enforcementContacts.size, equalTo(3))
        assertThat(response.size, equalTo(10))
        assertThat(response.page, equalTo(0))
    }

    @Test
    fun `filterDueDate flag returns only overdue and due tomorrow records`() {
        val response = mockMvc.get("/contact/${USER.username}/enforcements?filterDueDate=true") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<EnforcementContactResponse>()

        assertThat(response.totalResults, equalTo(2))
        assertThat(response.enforcementContacts.size, equalTo(2))
    }
}
