package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class OffenderSearchIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `noms records are returned successfully`() {
        val nomsList = listOf("AAA", "PERSON1")

        val expected = listOf(OffenderDetail(
            IDs(crn = "X000001", nomsNumber = "PERSON1"),
            offenderManagers = listOf(OffenderManager(
                StaffHuman("STAFF0U", forenames = "Test1 Forename2", surname = "Staff1", unallocated = true),
                probationArea = ProbationArea(description = "Test")
            ))
        ))
        val response = mockMvc
            .perform(post("/nomsNumbers").withToken().withJson(nomsList))
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<List<OffenderDetail>>()

        assertEquals(expected, response)
    }

}