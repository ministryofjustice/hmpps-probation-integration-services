package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.sentence.MainOffence
import uk.gov.justice.digital.hmpps.api.model.sentence.Offence
import uk.gov.justice.digital.hmpps.api.model.sentence.SentenceOverview
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SenetenceIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `get most recent active event`() {
        val person = PersonGenerator.OVERVIEW
        val response = mockMvc
            .perform(MockMvcRequestBuilders.get("/sentence/${person.crn}").withToken())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<SentenceOverview>()

        val expected = SentenceOverview(MainOffence(Offence("Another Murder", 1),
                                                    LocalDate.of(2024, 3, 11),
                                                "overview",
                                                    emptyList()))

        assertEquals(expected, response)
    }
    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/sentence/X123456"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

}