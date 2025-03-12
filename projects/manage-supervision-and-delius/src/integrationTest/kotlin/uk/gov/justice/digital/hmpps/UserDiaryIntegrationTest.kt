package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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

    @Test
    fun `get upcoming appointments default sort ascending order`() {
        val user = USER

        val response = mockMvc.perform(MockMvcRequestBuilders.get("/user/${user.username}/upcoming").withToken())
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
            mockMvc.perform(MockMvcRequestBuilders.get("/user/${user.username}/upcoming?ascending=false").withToken())
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn().response.contentAsJson<UserDiary>()

        assertEquals(2, response.totalResults)
        assertEquals("Default Sentence Type", response.appointments[0].latestSentence)
        assertEquals(NEXT_APPT_CONTACT.id, response.appointments[0].id)
    }
}