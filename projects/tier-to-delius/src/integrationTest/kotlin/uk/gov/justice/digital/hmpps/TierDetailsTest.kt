package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class TierDetailsTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `successful response`() {
        mockMvc.get("/tier-details/F001022") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.gender") { value(CaseEntityGenerator.DEFAULT.gender.description) }
                jsonPath("$.currentTier") { value(CaseEntityGenerator.DEFAULT.tier?.code) }
                jsonPath("$.rsrscore") { value(RsrScoreHistoryGenerator.HISTORY.last().score) }
                jsonPath("$.ogrsscore") { value(OgrsAssessmentGenerator.DEFAULT.score) }
                jsonPath("$.previousEnforcementActivity") { value(true) }
                jsonPath("$.registrations[0].code") { value(RegistrationGenerator.DEFAULT.type.code) }
                jsonPath("$.registrations[0].description") { value(RegistrationGenerator.DEFAULT.type.description) }
                jsonPath("$.registrations[0].level") { value(RegistrationGenerator.DEFAULT.level?.code) }
                jsonPath("$.convictions[0].breached") { isBoolean() }
                jsonPath("$.convictions[0].sentenceTypeCode") { value(DisposalTypeGenerator.DEFAULT.sentenceType) }
                jsonPath("$.convictions[0].requirements[0].mainCategoryTypeCode") { value("MAIN") }
            }
    }

    @Test
    fun `can retrieve person details`() {
        mockMvc.get("/person/A000001") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.crn") { value("A000001") }
                jsonPath("$.name.forenames") { value("Test") }
                jsonPath("$.name.surname") { value("Person") }
                jsonPath("$.age") { value(18) }
            }
    }

    @Test
    fun `can retrieve all crns`() {
        val crns = mockMvc.get("/probation-cases") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<List<String>>()

        assertThat(crns, containsInAnyOrder("F001022"))
    }
}
