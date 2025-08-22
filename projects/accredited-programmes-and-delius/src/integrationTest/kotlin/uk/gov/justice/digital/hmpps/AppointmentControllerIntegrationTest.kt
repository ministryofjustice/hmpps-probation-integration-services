package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.model.GetAppointmentsRequest
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class AppointmentControllerIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `get appointments success`() {
        mockMvc
            .perform(
                post("/appointments/search").withToken().withJson(
                    GetAppointmentsRequest(
                        requirementIds = TestData.REQUIREMENTS.map { it.id },
                        licenceConditionIds = TestData.LICENCE_CONDITIONS.map { it.id },
                        fromDate = LocalDate.of(2030, 1, 1),
                        toDate = LocalDate.of(2030, 12, 31),
                    )
                )
            )
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    {
                      "content": [
                        {
                          "crn": "A000001",
                          "reference": "${TestData.APPOINTMENTS[0].externalReference?.takeLast(36)}",
                          "requirementId": ${TestData.REQUIREMENTS[0].id},
                          "date": "2030-01-01",
                          "startTime": "1970-01-01T01:00:00+01:00",
                          "endTime": "1970-01-01T02:00:00+01:00",
                          "staff": {
                            "name": {
                              "forename": "Forename",
                              "surname": "Surname"
                            },
                            "code": "STAFF01"
                          },
                          "team": {
                            "code": "TEAM01",
                            "description": "Test Team"
                          },
                          "notes": "Some appointment notes",
                          "sensitive": false
                        },
                        {
                          "crn": "A000001",
                          "reference": "${TestData.APPOINTMENTS[1].externalReference?.takeLast(36)}",
                          "requirementId": ${TestData.REQUIREMENTS[1].id},
                          "date": "2030-01-01",
                          "startTime": "1970-01-01T01:00:00+01:00",
                          "endTime": "1970-01-01T02:00:00+01:00",
                          "staff": {
                            "name": {
                              "forename": "Forename",
                              "surname": "Surname"
                            },
                            "code": "STAFF01"
                          },
                          "team": {
                            "code": "TEAM01",
                            "description": "Test Team"
                          },
                          "notes": "Some appointment notes",
                          "sensitive": false
                        },
                        {
                          "crn": "A000001",
                          "reference": "${TestData.APPOINTMENTS[2].externalReference?.takeLast(36)}",
                          "licenceConditionId": ${TestData.LICENCE_CONDITIONS[0].id},
                          "date": "2030-01-01",
                          "startTime": "1970-01-01T01:00:00+01:00",
                          "endTime": "1970-01-01T02:00:00+01:00",
                          "staff": {
                            "name": {
                              "forename": "Forename",
                              "surname": "Surname"
                            },
                            "code": "STAFF01"
                          },
                          "team": {
                            "code": "TEAM01",
                            "description": "Test Team"
                          },
                          "notes": "Some appointment notes",
                          "sensitive": false
                        },
                        {
                          "crn": "A000001",
                          "reference": "${TestData.APPOINTMENTS[3].externalReference?.takeLast(36)}",
                          "licenceConditionId": ${TestData.LICENCE_CONDITIONS[1].id},
                          "date": "2030-01-01",
                          "startTime": "1970-01-01T01:00:00+01:00",
                          "endTime": "1970-01-01T02:00:00+01:00",
                          "staff": {
                            "name": {
                              "forename": "Forename",
                              "surname": "Surname"
                            },
                            "code": "STAFF01"
                          },
                          "team": {
                            "code": "TEAM01",
                            "description": "Test Team"
                          },
                          "notes": "Some appointment notes",
                          "sensitive": false
                        }
                      ]
                    }
                    """.trimIndent(),
                    JsonCompareMode.STRICT,
                )
            )
    }
}
