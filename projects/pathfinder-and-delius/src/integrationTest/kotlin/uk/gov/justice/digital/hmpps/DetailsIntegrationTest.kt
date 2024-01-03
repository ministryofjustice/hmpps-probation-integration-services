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
import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator
import uk.gov.justice.digital.hmpps.data.generator.KeyDateGenerator
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class DetailsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `API call retuns a success response using list of CRNs`() {
        val crns = listOf(DetailsGenerator.PERSON.crn)

        val detailResponse = mockMvc
            .perform(post("/detail").withToken().withJson(BatchRequest(crns)))
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<Detail>>()

        Assertions.assertThat(detailResponse).isEqualTo(listOf(getDetail()))
    }

    private fun getDetail(): Detail = Detail(
        DetailsGenerator.PERSON.name(),
        DetailsGenerator.PERSON.dateOfBirth,
        DetailsGenerator.PERSON.crn,
        DetailsGenerator.PERSON.nomsNumber,
        DetailsGenerator.PERSON.pncNumber,
        DetailsGenerator.DISTRICT.description,
        DetailsGenerator.DEFAULT_PA.description,
        Name(DetailsGenerator.STAFF.forename, DetailsGenerator.STAFF.middleName, DetailsGenerator.STAFF.surname),
        ConvictionEventGenerator.OFFENCE_MAIN.description,
        DetailsGenerator.PERSON.religion?.description,
        listOf(
            KeyDate(
                KeyDateGenerator.SED_KEYDATE.code,
                KeyDateGenerator.SED_KEYDATE.description,
                KeyDateGenerator.KEYDATE.date
            )
        )
    )
}
