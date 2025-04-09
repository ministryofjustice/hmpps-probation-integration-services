package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator
import uk.gov.justice.digital.hmpps.data.generator.SearchGenerator
import uk.gov.justice.digital.hmpps.data.generator.SearchGenerator.JOHN_SMITH_1
import uk.gov.justice.digital.hmpps.data.generator.SearchGenerator.JOHN_SMITH_2
import uk.gov.justice.digital.hmpps.entity.DetailPerson
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SearchIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `search must have at least one parameter`() {
        mockMvc
            .perform(post("/search/probation-cases").withToken().withJson(SearchRequest()))
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `can find matching all fields`() {
        val person = SearchGenerator.JOHN_DOE
        val request = SearchRequest(
            person.forename,
            person.surname,
            person.dateOfBirth,
            person.pncNumber,
            person.crn,
            person.nomsNumber
        )

        mockMvc
            .perform(post("/search/probation-cases").withToken().withJson(request))
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(ProbationCases(listOf(person.asProbationCase())))
    }

    @Test
    fun `can find all matching names`() {
        val request = SearchRequest("John", "Smith")

        mockMvc
            .perform(post("/search/probation-cases").withToken().withJson(request))
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(ProbationCases(listOf(JOHN_SMITH_1.asProbationCase(), JOHN_SMITH_2.asProbationCase())))
    }

    @Test
    fun `can find all matching crn`() {
        val request = SearchRequest(crn = JOHN_SMITH_2.crn)

        mockMvc
            .perform(post("/search/probation-cases").withToken().withJson(request))
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(ProbationCases(listOf(JOHN_SMITH_2.asProbationCase())))
    }

    @Test
    fun `can find all matching noms`() {
        val request = SearchRequest(nomsNumber = JOHN_SMITH_1.nomsNumber)

        mockMvc
            .perform(post("/search/probation-cases").withToken().withJson(request))
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(ProbationCases(listOf(JOHN_SMITH_1.asProbationCase())))
    }

    @Test
    fun `must provide at least one crn for crn lookup`() {
        mockMvc
            .perform(post("/search/probation-cases/crns").withToken().withJson(listOf<String>()))
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `can find all by crns`() {
        mockMvc
            .perform(
                post("/search/probation-cases/crns").withToken().withJson(listOf(JOHN_SMITH_1.crn, JOHN_SMITH_2.crn))
            )
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(ProbationCases(listOf(JOHN_SMITH_1.asProbationCase(), JOHN_SMITH_2.asProbationCase())))
    }

    private fun DetailPerson.asProbationCase(): ProbationCase {
        val staff = DetailsGenerator.STAFF
        val team = DetailsGenerator.TEAM
        val probationArea = DetailsGenerator.DEFAULT_PA
        return ProbationCase(
            forename,
            surname,
            dateOfBirth,
            crn,
            nomsNumber,
            pncNumber,
            Manager(
                name = Name(staff.forename, staff.middleName, staff.surname),
                team = Team(
                    code = team.code,
                    localDeliveryUnit = Ldu(
                        code = team.district.code,
                        name = team.district.description
                    )
                ),
                provider = Provider(probationArea.code, probationArea.description)
            )
        )
    }
}