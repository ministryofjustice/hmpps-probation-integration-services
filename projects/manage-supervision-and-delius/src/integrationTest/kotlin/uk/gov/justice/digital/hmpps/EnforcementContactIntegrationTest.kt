package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.contact.EnforcementContactResponse
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER_2
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDateTime

class EnforcementContactIntegrationTest : IntegrationTestBase() {

    @Test
    fun `user without staff record returns empty response`() {
        val response = mockMvc.get("/contact/${USER_2.username}/enforcements?months=0") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<EnforcementContactResponse>()

        assertThat(response.totalResults, equalTo(0))
        assertThat(response.enforcementContacts.size, equalTo(0))
    }

    @Test
    fun `default pagination returns all enforcement contacts`() {
        val response = mockMvc.get("/contact/${USER.username}/enforcements?months=0") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<EnforcementContactResponse>()

        assertThat(response.totalResults, equalTo(3))
        assertThat(response.enforcementContacts.size, equalTo(3))
        assertThat(response.enforcementContacts[0].lastModifiedDate, notNullValue())
        assertThat(response.size, equalTo(10))
        assertThat(response.page, equalTo(0))
    }

    @Test
    fun `filterDueDate flag returns only overdue and due tomorrow records`() {
        val response = mockMvc.get("/contact/${USER.username}/enforcements?filterDueDate=true&months=0") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<EnforcementContactResponse>()

        assertThat(response.totalResults, equalTo(2))
        assertThat(response.enforcementContacts.size, equalTo(2))
    }

    @Test
    fun `months filter excludes records with old last modified date`() {
        val allResults = mockMvc.get("/contact/${USER.username}/enforcements?months=0") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<EnforcementContactResponse>()

        val target = allResults.enforcementContacts.first()
        val originalLastModified = target.lastModifiedDate.toLocalDateTime()
        val staleLastModified = LocalDateTime.now().minusMonths(10)

        transactionTemplate.executeWithoutResult {
            entityManager.createNativeQuery(
                "update enforcement set last_updated_datetime = :lastUpdated where contact_id = :contactId"
            )
                .setParameter("lastUpdated", staleLastModified)
                .setParameter("contactId", target.id)
                .executeUpdate()
        }

        try {
            val filtered = mockMvc.get("/contact/${USER.username}/enforcements?months=6") { withToken() }
                .andExpect { status { isOk() } }
                .andReturn().response.contentAsJson<EnforcementContactResponse>()

            assertThat(filtered.totalResults, equalTo(allResults.totalResults - 1))
            assertThat(filtered.enforcementContacts.any { it.id == target.id }, equalTo(false))
        } finally {
            transactionTemplate.executeWithoutResult {
                entityManager.createNativeQuery(
                    "update enforcement set last_updated_datetime = :lastUpdated where contact_id = :contactId"
                )
                    .setParameter("lastUpdated", originalLastModified)
                    .setParameter("contactId", target.id)
                    .executeUpdate()
            }
        }
    }
}
