package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.DEFAULT_DOCUMENT_UUID
import uk.gov.justice.digital.hmpps.model.RequirementsResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class RequirementsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `can get requirements and breach reasons`() {
        val uuid = DEFAULT_DOCUMENT_UUID.toString()
        val response = mockMvc.get("/requirements/${uuid}") { withToken()}
            .andExpect { status { isOk()} }
            .andReturn().response.contentAsJson<RequirementsResponse>()
        assertThat(response.requirements.size).isEqualTo(1)
        assertThat(response.breachReasons.size).isEqualTo(1)
        assertThat(response.requirements.get(0).type.code).isEqualTo("Probation")
        assertThat(response.breachReasons.get(0).code).isEqualTo("Absent")
    }

    @Test
    fun `returns 404 if cant find requirements`() {
        val uuid = "12345678-1234-1234-1234-123456789012"
        mockMvc.get("/requirements/${uuid}") { withToken()}
            .andExpect { status { isNotFound() } }
    }
}