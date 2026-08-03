package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_IN_PRISON
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.model.SignAndSendResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SignAndSendIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `sign and send details returned with valid crn (om)`() {
        val crn = DEFAULT_PERSON.crn
        val username = UserGenerator.DEFAULT_PROBATION_USER.username
        val result = mockMvc.get("/sign-and-send/$crn/$username") { withToken() }.andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<SignAndSendResponse>()
        assertThat(result.userDetails?.forenames).isEqualTo("John Test")
        assertThat(result.userDetails?.surname).isEqualTo("Smith")
    }

    @Test
    fun `sign and send details returned with valid crn (pom)`() {
        val crn = PERSON_IN_PRISON.crn
        val username = UserGenerator.POM_USER.username
        val result = mockMvc.get("/sign-and-send/$crn/$username") { withToken() }.andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<SignAndSendResponse>()
        assertThat(result.userDetails?.forenames).isEqualTo("Jack Test")
        assertThat(result.userDetails?.surname).isEqualTo("Harry")
    }

    @Test
    fun `username not found returns 404 response`() {
        mockMvc.get("/sign-and-send/${DEFAULT_PERSON.crn}/nonexistent") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `404 when crn not found`() {
        mockMvc.get("/sign-and-send/X987654") { withToken() }
            .andExpect { status { isNotFound() } }
    }
}
