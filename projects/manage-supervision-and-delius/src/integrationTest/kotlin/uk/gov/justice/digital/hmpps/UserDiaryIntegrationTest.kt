package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.appointment.UserDiary
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.FIRST_APPT_CONTACT
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.NEXT_APPT_CONTACT
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER_2
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REQUIREMENT_CONTACT_1
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserDiaryIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/user/peter-parker/locations"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @ParameterizedTest
    @ValueSource(strings = ["upcoming", "no-outcome"])
    fun `user without staff record`(uri: String) {
        val user = USER_2

        val expected = UserDiary(10, 0, 0, 0, listOf())

        val response =
            mockMvc.perform(MockMvcRequestBuilders.get("/user/${user.username}/schedule/${uri}").withToken())
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn().response.contentAsJson<UserDiary>()

        assertEquals(expected, response)
    }

    @Test
    fun `get upcoming appointments default sort ascending order`() {
        val user = USER

        val response =
            mockMvc.perform(MockMvcRequestBuilders.get("/user/${user.username}/schedule/upcoming").withToken())
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn().response.contentAsJson<UserDiary>()

        assertEquals(2, response.totalResults)
        assertEquals("Default Sentence Type", response.appointments[0].latestSentence)
        assertEquals(FIRST_APPT_CONTACT.id, response.appointments[0].id)
    }

    @Test
    fun `get upcoming appointments default sort descending order`() {
        val user = USER

        val response =
            mockMvc.perform(
                MockMvcRequestBuilders.get("/user/${user.username}/schedule/upcoming?ascending=false").withToken()
            )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn().response.contentAsJson<UserDiary>()

        assertEquals(2, response.totalResults)
        assertEquals("Default Sentence Type", response.appointments[0].latestSentence)
        assertEquals(NEXT_APPT_CONTACT.id, response.appointments[0].id)
        assertEquals(1, response.appointments[0].numberOfAdditionalSentences)
    }

    @Test
    fun `get past appointments with no outcome default sort ascending order`() {
        val user = USER

        val response =
            mockMvc.perform(MockMvcRequestBuilders.get("/user/${user.username}/schedule/no-outcome").withToken())
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn().response.contentAsJson<UserDiary>()

        assertEquals(3, response.totalResults)
        assertEquals("Default Sentence Type", response.appointments[0].latestSentence)
        assertEquals(REQUIREMENT_CONTACT_1.id, response.appointments[0].id)
        assertEquals(1, response.appointments[0].numberOfAdditionalSentences)
    }
}