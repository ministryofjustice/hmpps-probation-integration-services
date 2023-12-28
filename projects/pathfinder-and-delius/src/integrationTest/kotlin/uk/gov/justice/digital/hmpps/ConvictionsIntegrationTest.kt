package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ConvictionEventGenerator
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ConvictionsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `API call retuns a success response using CRN`() {
        val crns = listOf(ConvictionEventGenerator.PERSON.crn)
        val detailResponse = mockMvc.perform(post("/convictions").withToken().withJson(BatchRequest(crns)))
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ConvictionsContainer>()

        Assertions.assertThat(detailResponse).isEqualTo(getConvictions())
    }

    private fun getConvictions(): ConvictionsContainer = ConvictionsContainer(
        listOf(
            PersonConviction(
                ConvictionEventGenerator.DEFAULT_EVENT.convictionEventPerson.crn,
                listOf(
                    Conviction(
                        ConvictionEventGenerator.DEFAULT_EVENT.convictionDate,
                        ConvictionEventGenerator.DISPOSAL_TYPE.description,
                        listOf(
                            Offence(ConvictionEventGenerator.OFFENCE_MAIN.description, true),
                            Offence(ConvictionEventGenerator.OFFENCE_OTHER.description, false)
                        )
                    )
                )
            )
        )
    )
}
