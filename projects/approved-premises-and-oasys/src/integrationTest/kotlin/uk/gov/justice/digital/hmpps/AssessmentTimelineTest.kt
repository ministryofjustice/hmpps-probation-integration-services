package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysTimelineAssessment
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class AssessmentTimelineTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `get latest layer 3 timeline assessment`() {
        val oasysTimelineAssessment = mockMvc
            .perform(get("/latest-assessment/D006296").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<OasysTimelineAssessment>()

        assertThat(oasysTimelineAssessment.initiationDate)
            .isEqualTo(ZonedDateTime.parse("2022-07-27T12:10:58+01:00").withZoneSameInstant(EuropeLondon))
    }

    @Test
    fun `should return HTTP not found when CRN does not exist`() {
        mockMvc
            .perform(get("/latest-assessment/D000000").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return HTTP not found when a layer 3 assessment does not exist`() {
        mockMvc
            .perform(get("/latest-assessment/D000001").withToken())
            .andExpect(status().isNotFound)
    }
}
