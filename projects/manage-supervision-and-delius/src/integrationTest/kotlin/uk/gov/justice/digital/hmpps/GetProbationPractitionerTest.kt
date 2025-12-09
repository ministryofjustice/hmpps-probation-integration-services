package uk.gov.justice.digital.hmpps

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.personalDetails.ProbationPractitioner
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class GetProbationPractitionerTest : IntegrationTestBase() {

    @Test
    fun `can retrieve PP details`() {
        val person = PersonGenerator.OVERVIEW
        val res = mockMvc.get("/case/${person.crn}/probation-practitioner") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<ProbationPractitioner>()

        assertThat(
            res,
            equalTo(
                ProbationPractitioner(
                    "N01PEPA",
                    ProbationPractitioner.Name("Peter", "Parker"),
                    ProbationPractitioner.Provider("N01", "Description of N01"),
                    ProbationPractitioner.Team("N07T02", "OMU B"),
                    false,
                    "peter-parker"
                )
            )
        )
    }
}