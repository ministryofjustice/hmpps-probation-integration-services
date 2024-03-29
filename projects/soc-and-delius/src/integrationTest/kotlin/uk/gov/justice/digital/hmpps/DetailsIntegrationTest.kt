package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ConvictionEventGenerator
import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator
import uk.gov.justice.digital.hmpps.data.generator.KeyDateGenerator
import uk.gov.justice.digital.hmpps.data.generator.NSIGenerator
import uk.gov.justice.digital.hmpps.model.Detail
import uk.gov.justice.digital.hmpps.model.KeyDate
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.name
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class DetailsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `API call retuns a success response using NOMS`() {
        val noms = DetailsGenerator.PERSON.nomsNumber
        mockMvc
            .perform(get("/detail/$noms?type=NOMS").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(getDetail())
    }

    @Test
    fun `API call retuns a success response using CRN`() {
        val crn = DetailsGenerator.PERSON.crn
        mockMvc
            .perform(get("/detail/$crn?type=CRN").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(getDetail())
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
        ConvictionEventGenerator.OFFENCE_MAIN_TYPE.description,
        DetailsGenerator.PERSON.religion?.description,
        listOf(
            KeyDate(
                KeyDateGenerator.SED_KEYDATE.code,
                KeyDateGenerator.SED_KEYDATE.description,
                KeyDateGenerator.KEYDATE.date
            )
        ),
        DetailsGenerator.RELEASE.date,
        DetailsGenerator.RELEASE.releaseType.description,
        DetailsGenerator.INSTITUTION.name,
        DetailsGenerator.RECALL.date,
        DetailsGenerator.RECALL.reason.description,
        NSIGenerator.RECALL_NSI.referralDate,
        NSIGenerator.BREACH_NSI.referralDate
    )
}
