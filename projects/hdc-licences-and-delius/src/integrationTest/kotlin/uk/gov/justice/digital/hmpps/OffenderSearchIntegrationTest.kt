package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class OffenderSearchIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `unauthorized status returned`() {
        val nomsList = listOf("AAA", "BBB")
        mockMvc
            .post("/nomsNumbers") { json = nomsList }
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `noms records are returned successfully`() {
        val nomsList = listOf("PERSON2", "PERSON3", "PERSON1", "PERSON1")

        val expected = listOf(
            OffenderDetail(
                IDs(crn = "X000001", nomsNumber = "PERSON1"),
                offenderManagers = listOf(
                    OffenderManager(
                        StaffHuman("STAFF01", forenames = "Test", surname = "Staff", unallocated = false),
                        probationArea = ProbationArea(description = "Test"),
                        active = true
                    )
                )
            ),
            OffenderDetail(
                IDs(crn = "X000002", nomsNumber = "PERSON2"),
                offenderManagers = listOf(
                    OffenderManager(
                        StaffHuman(
                            "STAFF0U",
                            forenames = "Test1 Forename1",
                            surname = "Staff1",
                            unallocated = true
                        ),
                        probationArea = ProbationArea(description = "Test"),
                        active = true
                    )
                )
            )
        )
        val response = mockMvc
            .post("/nomsNumbers") {
                withToken()
                json = nomsList
            }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<List<OffenderDetail>>()

        assertEquals(expected, response)
    }
}