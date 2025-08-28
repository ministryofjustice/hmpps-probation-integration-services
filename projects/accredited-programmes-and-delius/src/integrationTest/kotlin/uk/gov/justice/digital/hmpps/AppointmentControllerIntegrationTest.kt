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
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
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
    fun `exception when end date before start date`() {
        mockMvc
            .perform(
                post("/appointments/search").withToken().withJson(
                    GetAppointmentsRequest(
                        requirementIds = listOf(1L),
                        licenceConditionIds = listOf(2L),
                        fromDate = LocalDate.of(2030, 12, 31),
                        toDate = LocalDate.of(2030, 12, 1),
                    )
                )
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `exception when no requirement or licence condition ids provided`() {
        mockMvc
            .perform(
                post("/appointments/search").withToken().withJson(
                    GetAppointmentsRequest(
                        requirementIds = emptyList(),
                        licenceConditionIds = emptyList(),
                        fromDate = LocalDate.of(2030, 1, 1),
                        toDate = LocalDate.of(2030, 12, 31),
                    )
                )
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `exception when providing over 500 requirement ids`() {
        mockMvc
            .perform(
                post("/appointments/search").withToken().withJson(
                    GetAppointmentsRequest(
                        requirementIds = (1..1001).map { it.toLong() },
                        licenceConditionIds = emptyList(),
                        fromDate = LocalDate.of(2030, 1, 1),
                        toDate = LocalDate.of(2030, 12, 31),
                    )
                )
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `exception when providing over 500 licence condition ids`() {
        mockMvc
            .perform(
                post("/appointments/search").withToken().withJson(
                    GetAppointmentsRequest(
                        requirementIds = emptyList(),
                        licenceConditionIds = (1..1001).map { it.toLong() },
                        fromDate = LocalDate.of(2030, 1, 1),
                        toDate = LocalDate.of(2030, 12, 31),
                    )
                )
            ).andExpect(status().isBadRequest)
    }

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
                      "content": {
                        "A000001": [
                            {
                              "crn": "A000001",
                              "reference": "${TestData.APPOINTMENTS[0].externalReference?.takeLast(36)}",
                              "requirementId": ${TestData.REQUIREMENTS[0].id},
                              "date": "${TestData.APPOINTMENTS[0].date}",
                              "startTime": "${ZonedDateTimeDeserializer.formatter.format(TestData.APPOINTMENTS[0].startTime).substring(0, 25)}",
                              "endTime": "${ZonedDateTimeDeserializer.formatter.format(TestData.APPOINTMENTS[0].endTime).substring(0, 25)}",
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
                              "date": "${TestData.APPOINTMENTS[2].date}",
                              "startTime": "${ZonedDateTimeDeserializer.formatter.format(TestData.APPOINTMENTS[2].startTime).substring(0, 25)}",
                              "endTime": "${ZonedDateTimeDeserializer.formatter.format(TestData.APPOINTMENTS[2].endTime).substring(0, 25)}",
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
                              "date": "${TestData.APPOINTMENTS[1].date}",
                              "startTime": "${ZonedDateTimeDeserializer.formatter.format(TestData.APPOINTMENTS[1].startTime).substring(0, 25)}",
                              "endTime": "${ZonedDateTimeDeserializer.formatter.format(TestData.APPOINTMENTS[1].endTime).substring(0, 25)}",
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
                              "date": "${TestData.APPOINTMENTS[3].date}",
                              "startTime": "${ZonedDateTimeDeserializer.formatter.format(TestData.APPOINTMENTS[3].startTime).substring(0, 25)}",
                              "endTime": "${ZonedDateTimeDeserializer.formatter.format(TestData.APPOINTMENTS[3].endTime).substring(0, 25)}",
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
                    }
                    """.trimIndent(),
                    JsonCompareMode.STRICT,
                )
            )
    }
}
