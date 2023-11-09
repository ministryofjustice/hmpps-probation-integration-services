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
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.Manager
import uk.gov.justice.digital.hmpps.api.Team
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.ALLOCATED_PERSON
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.STAFF
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.TEAM
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.UNALLOCATED_PERSON
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.UNALLOCATED_STAFF
import uk.gov.justice.digital.hmpps.integrations.delius.manager.name
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CommunityManagerIntTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @ParameterizedTest
    @MethodSource("communityManagers")
    fun `returns the community manager correctly`(nomsId: String, expected: Manager) {
        val res = mockMvc
            .perform(
                get("/probation-cases/$nomsId/community-manager")
                    .withOAuth2Token(wireMockServer)
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsString

        val actual = objectMapper.readValue<Manager>(res)
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `noms id not found throws 404`() {
        mockMvc
            .perform(
                get("/probation-cases/invalid/community-manager")
                    .withOAuth2Token(wireMockServer)
            )
            .andExpect(status().isNotFound)
    }

    companion object {
        @JvmStatic
        fun communityManagers() = listOf(
            Arguments.of(ALLOCATED_PERSON.nomsId, Manager(STAFF.code, STAFF.name(), Team(TEAM.code, TEAM.description))),
            Arguments.of(
                UNALLOCATED_PERSON.nomsId,
                Manager(UNALLOCATED_STAFF.code, UNALLOCATED_STAFF.name(), Team(TEAM.code, TEAM.description))
            )
        )
    }
}
