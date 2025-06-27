package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.sentence.User
import uk.gov.justice.digital.hmpps.api.model.user.DefaultUserDetails
import uk.gov.justice.digital.hmpps.api.model.user.Provider
import uk.gov.justice.digital.hmpps.api.model.user.Team
import uk.gov.justice.digital.hmpps.api.model.user.UserProviderResponse
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.PROVIDER_2
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_USER_1
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.TEAM
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserProvidersIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/user/user1/providers"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @ParameterizedTest
    @MethodSource("providerRequests")
    fun `get user providers`(uri: String, expected: UserProviderResponse) {
        val response = mockMvc
            .perform(MockMvcRequestBuilders.get(uri).withToken())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<UserProviderResponse>()
        assertEquals(expected, response)
    }

    companion object {
        val defaultUserDetails =
            DefaultUserDetails(STAFF_USER_1.username, DEFAULT_PROVIDER.description, TEAM.description)

        @JvmStatic
        fun providerRequests(): List<Arguments> = listOf(
            Arguments.of(
                "/user/peter-parker/providers",
                UserProviderResponse(
                    defaultUserDetails,
                    listOf(
                        Provider(DEFAULT_PROVIDER.code, "Description of N01"),
                        Provider(PROVIDER_2.code, "Description of W01")
                    ),
                    listOf(
                        Team(DEFAULT_TEAM.description, DEFAULT_TEAM.code),
                        Team(OffenderManagerGenerator.TEAM.description, OffenderManagerGenerator.TEAM.code)
                    ),
                    listOf(unallocatedUser)
                )
            ),
            Arguments.of(
                "/user/peter-parker/providers?region=${DEFAULT_PROVIDER.code}&team=${TEAM.code}",
                UserProviderResponse(
                    defaultUserDetails,
                    listOf(
                        Provider(DEFAULT_PROVIDER.code, "Description of N01"),
                        Provider(PROVIDER_2.code, "Description of W01")
                    ),
                    listOf(
                        Team(DEFAULT_TEAM.description, DEFAULT_TEAM.code),
                        Team(OffenderManagerGenerator.TEAM.description, OffenderManagerGenerator.TEAM.code)
                    ),
                    listOf(
                        User(
                            STAFF_USER_1.username,
                            "${STAFF_USER_1.forename} ${STAFF_USER_1.surname} (${STAFF_USER_1.staff!!.role!!.description})"
                        ),
                        unallocatedUser
                    )
                )
            )
        )
    }
}

val unallocatedUser = User("Unallocated", "Unallocated")