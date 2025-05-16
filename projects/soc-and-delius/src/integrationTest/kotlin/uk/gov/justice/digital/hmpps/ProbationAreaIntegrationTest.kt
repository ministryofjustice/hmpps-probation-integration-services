package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.model.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.model.ProbationArea
import uk.gov.justice.digital.hmpps.model.ProbationAreaContainer
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ProbationAreaIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `API call retuns a success response`() {
        mockMvc
            .perform(get("/probation-areas").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(getProbationAreas())
    }

    @Test
    fun `API call including non selectable retuns a success response`() {
        mockMvc
            .perform(get("/probation-areas?includeNonSelectable=true").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(getProbationAreasIncludingNonSelectable())
    }

    @Test
    fun `get probation history for multiple CRNs`() {
        val crns = listOf(ManagerGenerator.PERSON.crn, ManagerGenerator.PERSON_2.crn, "SOME_OTHER_CRN")
        mockMvc.perform(get("/probation-area-history").withToken().withJson(crns))
            .andExpect(status().is2xxSuccessful)
            .andExpect(
                content().json(
                    """
                   {
                     "MH00001": [
                       {
                         "startDate": "2000-01-01",
                         "endDate": "2001-01-01",
                         "probationArea": {
                           "code": "M01",
                           "description": "Area 1"
                         }
                       },
                       {
                         "startDate": "2001-01-01",
                         "endDate": "2002-01-01",
                         "probationArea": {
                           "code": "M02",
                           "description": "Area 2"
                         }
                       },
                       {
                         "startDate": "2002-01-01",
                         "probationArea": {
                           "code": "M03",
                           "description": "Area 3"
                         }
                       }
                     ],
                     "MH00002": [
                       {
                         "startDate": "2025-05-15",
                         "probationArea": {
                           "code": "M01",
                           "description": "Area 1"
                         }
                       }
                     ]
                   }
                    """.trimIndent()
                )
            )
    }

    @Test
    fun `does not accept over 500 crns`() {
        mockMvc
            .perform(get("/probation-area-history").withToken().withJson(List(501) { "CRN" }))
            .andExpect(status().isBadRequest)
    }

    private fun getProbationAreas(): ProbationAreaContainer = ProbationAreaContainer(
        listOf(
            ProbationArea(
                ProbationAreaGenerator.DEFAULT_PA.code,
                ProbationAreaGenerator.DEFAULT_PA.description,
                listOf(
                    LocalDeliveryUnit(
                        ProbationAreaGenerator.DEFAULT_LDU.code,
                        ProbationAreaGenerator.DEFAULT_LDU.description
                    ),
                    LocalDeliveryUnit(
                        ProbationAreaGenerator.DEFAULT_LDU2.code,
                        ProbationAreaGenerator.DEFAULT_LDU2.description
                    )
                )
            )
        )
    )

    private fun getProbationAreasIncludingNonSelectable(): ProbationAreaContainer = ProbationAreaContainer(
        listOf(
            ProbationArea(
                ProbationAreaGenerator.DEFAULT_PA.code,
                ProbationAreaGenerator.DEFAULT_PA.description,
                listOf(
                    LocalDeliveryUnit(
                        ProbationAreaGenerator.DEFAULT_LDU.code,
                        ProbationAreaGenerator.DEFAULT_LDU.description
                    ),
                    LocalDeliveryUnit(
                        ProbationAreaGenerator.DEFAULT_LDU2.code,
                        ProbationAreaGenerator.DEFAULT_LDU2.description
                    )
                )
            ),
            ProbationArea(
                ProbationAreaGenerator.NON_SELECTABLE_PA.code,
                ProbationAreaGenerator.NON_SELECTABLE_PA.description,
                listOf(
                    LocalDeliveryUnit(
                        ProbationAreaGenerator.NON_SELECTABLE_LDU.code,
                        ProbationAreaGenerator.NON_SELECTABLE_LDU.description
                    )
                )
            )
        )
    )
}
