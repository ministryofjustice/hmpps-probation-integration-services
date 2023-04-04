package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProbationCaseResourceTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @ParameterizedTest
    @MethodSource("existingCases")
    fun `retrieve community manager who is responsible officer`(person: Person, communityResponsible: Boolean) {
        val staff = ProviderGenerator.JOHN_SMITH

        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/probation-case/${person.crn}/responsible-officer")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful).andReturn().response.contentAsString

        val com = objectMapper.readValue<ResponsibleOfficer>(res).communityManager
        assertThat(com.code, equalTo(staff.code))
        assertThat(com.name, equalTo(Name(staff.forename, staff.surname)))
        assertThat(com.email, equalTo("john.smith@moj.gov.uk"))
        assertThat(com.responsibleOfficer, equalTo(communityResponsible))
    }

    @Test
    fun `crn not found returns 404`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/probation-case/InvalidCrn/responsible-officer")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    companion object {
        @JvmStatic
        fun existingCases() = listOf(
            Arguments.of(PersonGenerator.COMMUNITY_RESPONSIBLE, true),
            Arguments.of(PersonGenerator.COMMUNITY_NOT_RESPONSIBLE, false)
        )
    }
}
