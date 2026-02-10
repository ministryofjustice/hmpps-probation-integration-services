package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.model.OffenceDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest
class OffencesIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {
    @Test
    fun `can get offences by crn and event number`() {
        val crn = DEFAULT_PERSON.crn
        val event = 1
        val response = mockMvc.get("/case/$crn/event/$event/offences") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<OffenceDetails>()
        assertThat(response.mainOffence.date).isEqualTo(LocalDate.now())
        assertThat(response.mainOffence.mainCategory.code.trim()).isEqualTo("1")
        assertThat(response.mainOffence.subCategory.code).isEqualTo("1A")
    }

    @Test
    fun `crn and event number with no offences returns not found`() {
        val crn = "X012771"
        val event = 2
        mockMvc.get("/case/$crn/event/$event/offences") { withToken() }
            .andExpect { status { isNotFound() } }
    }
}
