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
import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.KeyDateGenerator
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ConvictionsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `API call retuns a success response using NOMS`() {
        val noms = ConvictionEventGenerator.PERSON.nomsNumber
        mockMvc
            .perform(get("/convictions/$noms?type=NOMS").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(getConvictions(true))
    }

    @Test
    fun `API call retuns a success response using CRN`() {
        val crn = ConvictionEventGenerator.PERSON.crn
        mockMvc
            .perform(get("/convictions/$crn?type=CRN").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(getConvictions(true))
    }

    @Test
    fun `API call retuns only active convictions success response using CRN`() {
        val crn = ConvictionEventGenerator.PERSON.crn
        mockMvc
            .perform(get("/convictions/$crn?type=CRN&activeOnly=true").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(getConvictions())
    }

    @Test
    fun `API call retuns only active convictions success response using NOMS`() {
        val noms = ConvictionEventGenerator.PERSON.nomsNumber
        mockMvc
            .perform(get("/convictions/$noms?type=NOMS&activeOnly=true").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(getConvictions())
    }

    private fun getConvictions(withInActive: Boolean = false): ConvictionsContainer {
        val activeConviction =
            Conviction(
                ConvictionEventGenerator.DEFAULT_EVENT.id,
                ConvictionEventGenerator.DEFAULT_EVENT.convictionDate,
                ConvictionEventGenerator.DEFAULT_EVENT.referralDate,
                ConvictionEventGenerator.DISPOSAL_TYPE.description,
                CourtAppearanceGenerator.DEFAULT_OUTCOME.description,
                listOf(
                    Offence(
                        ConvictionEventGenerator.MAIN_OFFENCE.id,
                        ConvictionEventGenerator.OFFENCE_MAIN_TYPE.description,
                        ConvictionEventGenerator.OFFENCE_MAIN_TYPE.mainCategoryDescription,
                        true
                    ),
                    Offence(
                        ConvictionEventGenerator.OTHER_OFFENCE.id,
                        ConvictionEventGenerator.ADDITIONAL_OFFENCE_TYPE.description,
                        ConvictionEventGenerator.ADDITIONAL_OFFENCE_TYPE.mainCategoryDescription,
                        false
                    )
                ),
                Sentence(
                    ConvictionEventGenerator.DISPOSAL.id,
                    ConvictionEventGenerator.DISPOSAL.startDate,
                    null,
                    Custody(
                        KeyDateGenerator.CUSTODY_1.prisonerNumber,
                        CustodyStatus(
                            KeyDateGenerator.CUSTODY_1.status.code,
                            KeyDateGenerator.CUSTODY_1.status.description
                        ),
                        listOf(
                            KeyDate(
                                KeyDateGenerator.KEYDATE.type.code,
                                KeyDateGenerator.KEYDATE.type.description,
                                KeyDateGenerator.KEYDATE.date
                            )
                        )
                    )
                )
            )

        val inactiveConviction = Conviction(
            ConvictionEventGenerator.INACTIVE_EVENT.id,
            ConvictionEventGenerator.INACTIVE_EVENT.convictionDate,
            ConvictionEventGenerator.INACTIVE_EVENT.referralDate,
            "unknown",
            "unknown",
            listOf(),
            null
        )
        return if (withInActive) {
            ConvictionsContainer(listOf(activeConviction, inactiveConviction))
        } else {
            ConvictionsContainer(listOf(activeConviction))
        }
    }
}
