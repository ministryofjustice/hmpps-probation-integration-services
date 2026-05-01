package uk.gov.justice.digital.hmpps

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.ldap.NameNotFoundException
import org.springframework.ldap.core.AttributesMapper
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.personalDetails.ProbationPractitioner
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import org.springframework.ldap.core.LdapTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.mockito.kotlin.any

class GetProbationPractitionerTest : IntegrationTestBase() {

    @MockitoBean
    lateinit var ldapTemplate: LdapTemplate

    @Test
    fun `can retrieve PP details`() {
        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenReturn(listOf("peter.parker@moj.gov.uk"))

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
                    "peter.parker@moj.gov.uk",
                    ProbationPractitioner.Provider("N01", "Description of N01"),
                    ProbationPractitioner.Team("N07T02", "OMU B"),
                    false,
                    "peter-parker"
                )
            )
        )
    }

    @Test
    fun `returns null email when ldap user not found`() {
        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenThrow(NameNotFoundException("missing"))

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
                    null,
                    ProbationPractitioner.Provider("N01", "Description of N01"),
                    ProbationPractitioner.Team("N07T02", "OMU B"),
                    false,
                    "peter-parker"
                )
            )
        )
    }

    @Test
    fun `returns null email when ldap lookup fails unexpectedly`() {
        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenThrow(RuntimeException("ldap down"))

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
                    null,
                    ProbationPractitioner.Provider("N01", "Description of N01"),
                    ProbationPractitioner.Team("N07T02", "OMU B"),
                    false,
                    "peter-parker"
                )
            )
        )
    }
}