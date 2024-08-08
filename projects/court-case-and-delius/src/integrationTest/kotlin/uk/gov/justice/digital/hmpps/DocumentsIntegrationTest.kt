package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.OffenderDocuments
import uk.gov.justice.digital.hmpps.api.resource.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class DocumentsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `call documents grouped by CRN`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        val response = mockMvc
            .perform(get("/probation-case/$crn/documents/grouped").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<OffenderDocuments>()

        assertThat(response.documents.size, equalTo(0))
        assertThat(response.convictions.size, equalTo(1))
    }

    @Test
    fun `call documents grouped by CRN invalid filter`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        val response = mockMvc
            .perform(get("/probation-case/$crn/documents/grouped?type=INVALID").withToken())
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(response.developerMessage, equalTo("type of INVALID was not valid"))
    }
}