package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.data.generator.ManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.model.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.model.ProbationArea
import uk.gov.justice.digital.hmpps.model.ProbationAreaContainer
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ProbationAreaIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `API call retuns a success response`() {
        mockMvc.get("/probation-areas") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andExpectJson(getProbationAreas())
    }

    @Test
    fun `API call including non selectable returns a success response`() {
        mockMvc.get("/probation-areas?includeNonSelectable=true") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andExpectJson(getProbationAreasIncludingNonSelectable())
    }

    @Test
    fun `get probation history for multiple CRNs`() {
        val crns = listOf(ManagerGenerator.PERSON.crn, ManagerGenerator.PERSON_2.crn, "SOME_OTHER_CRN")
        mockMvc.post("/probation-area-history") {
            withToken()
            json = crns
        }
            .andExpect {
                status { is2xxSuccessful() }
                content {
                    json(
                        """
                        {
                          "MH00001": [
                            {
                              "startDate": "2000-01-01",
                              "endDate": "2002-01-01",
                              "localAdminUnit": {
                                "code": "LA1",
                                "description": "Admin Unit of LA1",
                                "probationDeliveryUnit": {
                                  "code": "PD1",
                                  "description": "Delivery Unit of PD1",
                                  "probationArea": {
                                    "code": "M01",
                                    "description": "Area 1"
                                  }
                                }
                              }
                            },
                            {
                              "startDate": "2001-01-01",
                              "endDate": "2003-01-01",
                              "localAdminUnit": {
                                "code": "LA2",
                                "description": "Admin Unit of LA2",
                                "probationDeliveryUnit": {
                                  "code": "PD2",
                                  "description": "Delivery Unit of PD2",
                                  "probationArea": {
                                    "code": "M02",
                                    "description": "Area 2"
                                  }
                                }
                              }
                            },
                            {
                              "startDate": "2002-01-01",
                              "localAdminUnit": {
                                "code": "LA3",
                                "description": "Admin Unit of LA3",
                                "probationDeliveryUnit": {
                                  "code": "PD3",
                                  "description": "Delivery Unit of PD3",
                                  "probationArea": {
                                    "code": "M03",
                                    "description": "Area 3"
                                  }
                                }
                              }
                            },
                            {
                              "startDate": "2005-01-01",
                              "endDate": "2007-01-01",
                              "localAdminUnit": {
                                "code": "LA1",
                                "description": "Admin Unit of LA1",
                                "probationDeliveryUnit": {
                                  "code": "PD1",
                                  "description": "Delivery Unit of PD1",
                                  "probationArea": {
                                    "code": "M01",
                                    "description": "Area 1"
                                  }
                                }
                              }
                            }
                          ],
                          "MH00002": [
                            {
                              "startDate": "2025-05-15",
                              "localAdminUnit": {
                                "code": "LA1",
                                "description": "Admin Unit of LA1",
                                "probationDeliveryUnit": {
                                  "code": "PD1",
                                  "description": "Delivery Unit of PD1",
                                  "probationArea": {
                                    "code": "M01",
                                    "description": "Area 1"
                                  }
                                }
                              }
                            }
                          ]
                        }
                        """.trimIndent(), JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `does not accept over 500 crns`() {
        mockMvc.post("/probation-area-history") {
            withToken()
            json = List(501) { "CRN" }
        }
            .andExpect { status { isBadRequest() } }
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
            ),
            ProbationArea(
                ManagerGenerator.PROBATION_AREA_1.code,
                ManagerGenerator.PROBATION_AREA_1.description,
                listOf(
                    LocalDeliveryUnit(
                        ManagerGenerator.LAU_1.code,
                        ManagerGenerator.LAU_1.description
                    )
                )
            ),
            ProbationArea(
                ManagerGenerator.PROBATION_AREA_2.code,
                ManagerGenerator.PROBATION_AREA_2.description,
                listOf(
                    LocalDeliveryUnit(
                        ManagerGenerator.LAU_2.code,
                        ManagerGenerator.LAU_2.description
                    )
                )
            ),
            ProbationArea(
                ManagerGenerator.PROBATION_AREA_3.code,
                ManagerGenerator.PROBATION_AREA_3.description,
                listOf(
                    LocalDeliveryUnit(
                        ManagerGenerator.LAU_3.code,
                        ManagerGenerator.LAU_3.description
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
            ),
            ProbationArea(
                ManagerGenerator.PROBATION_AREA_1.code,
                ManagerGenerator.PROBATION_AREA_1.description,
                listOf(
                    LocalDeliveryUnit(
                        ManagerGenerator.LAU_1.code,
                        ManagerGenerator.LAU_1.description
                    )
                )
            ),
            ProbationArea(
                ManagerGenerator.PROBATION_AREA_2.code,
                ManagerGenerator.PROBATION_AREA_2.description,
                listOf(
                    LocalDeliveryUnit(
                        ManagerGenerator.LAU_2.code,
                        ManagerGenerator.LAU_2.description
                    )
                )
            ),
            ProbationArea(
                ManagerGenerator.PROBATION_AREA_3.code,
                ManagerGenerator.PROBATION_AREA_3.description,
                listOf(
                    LocalDeliveryUnit(
                        ManagerGenerator.LAU_3.code,
                        ManagerGenerator.LAU_3.description
                    )
                )
            )
        )
    )
}
