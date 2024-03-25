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
import uk.gov.justice.digital.hmpps.model.OffenceDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class OffenceDetailsTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `should return offence details`() {
        val offenceDetails = mockMvc
            .perform(get("/offence-details/D006296").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<OffenceDetails>()

        assertThat(offenceDetails.initiationDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-02T15:17:29Z").withZoneSameInstant(EuropeLondon))
        assertThat(offenceDetails.lastUpdatedDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-17T15:02:17Z").withZoneSameInstant(EuropeLondon))
        assertThat(offenceDetails.offence?.offenceAnalysis)
            .isEqualTo("Threatened a passer-by with a knife")
    }

    @Test
    fun `should return HTTP not found when CRN does not exist`() {
        mockMvc
            .perform(get("/offence-details/D000001").withToken())
            .andExpect(status().isNotFound)
    }
}
