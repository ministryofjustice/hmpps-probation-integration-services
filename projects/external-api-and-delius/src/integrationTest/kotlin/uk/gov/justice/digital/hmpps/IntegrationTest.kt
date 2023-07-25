package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.PERSON
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Test
    fun `returns supervisions`() {
        mockMvc
            .perform(get("/case/${PERSON.crn}/supervisions").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful)
            .andExpect(
                content().json(
                    """
                    {
                        "supervisions": [
                            {
                                "number": 1,
                                "active": true,
                                "date": "2023-01-02",
                                "sentence": {
                                    "description": "ORA Suspended Sentence Order",
                                    "date": "2023-03-04",
                                    "length": 6,
                                    "lengthUnits": "Months"
                                },
                                "mainOffence": {
                                    "date": "2023-01-01",
                                    "count": 1,
                                    "code": "12345",
                                    "description": "Test offence",
                                    "mainCategory": {
                                        "code": "123",
                                        "description": "Test"
                                    },
                                    "subCategory": {
                                        "code": "45",
                                        "description": "offence"
                                    },
                                    "schedule15SexualOffence": true
                                },
                                "additionalOffences": [
                                    {
                                        "count": 3,
                                        "code": "12345",
                                        "description": "Test offence",
                                        "mainCategory": {
                                            "code": "123",
                                            "description": "Test"
                                        },
                                        "subCategory": {
                                            "code": "45",
                                            "description": "offence"
                                        },
                                        "schedule15SexualOffence": true
                                    }
                                ],
                                "courtAppearances": [
                                    {
                                        "type": "Sentence",
                                        "date": "2023-02-03T10:00:00Z",
                                        "court": "Manchester Crown Court",
                                        "plea": "Not guilty"
                                    }
                                ]
                            }
                        ]
                    }
                    """.trimIndent()
                )
            )
    }
}
