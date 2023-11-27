package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import jakarta.servlet.ServletException
import jakarta.validation.ConstraintViolationException
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.CaseIdentifier
import uk.gov.justice.digital.hmpps.api.model.ManagedCases
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.UserDetail
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.service.CaseAccess
import uk.gov.justice.digital.hmpps.service.UserAccess

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserResourceTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `correctly returns cases managed by a given user`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/users/john-smith/managed-cases")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is2xxSuccessful).andReturn().response.contentAsString

        val managedCases = objectMapper.readValue<ManagedCases>(res)
        assertThat(managedCases.managedCases.size, equalTo(2))
        assertThat(managedCases.managedCases, hasItem(CaseIdentifier(PersonGenerator.COMMUNITY_RESPONSIBLE.crn)))
        assertThat(managedCases.managedCases, hasItem(CaseIdentifier(PersonGenerator.COMMUNITY_NOT_RESPONSIBLE.crn)))
    }

    @Test
    fun `limited access controls are correctly returned`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.post("/users/${UserGenerator.LIMITED_ACCESS_USER.username.lowercase()}/access")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        listOf(
                            PersonGenerator.EXCLUSION.crn,
                            PersonGenerator.RESTRICTION.crn,
                            PersonGenerator.DEFAULT.crn,
                            PersonGenerator.RESTRICTION_EXCLUSION.crn
                        )
                    )
                )
        ).andReturn().response.contentAsString

        val userAccess = objectMapper.readValue<UserAccess>(res)
        val result: Map<String, CaseAccess> = userAccess.access.associateBy { it.crn }

        val excludedCrn = PersonGenerator.EXCLUSION.crn
        assertThat(
            result[excludedCrn],
            equalTo(
                CaseAccess(
                    excludedCrn,
                    userExcluded = true,
                    userRestricted = false,
                    exclusionMessage = PersonGenerator.EXCLUSION.exclusionMessage
                )
            )
        )

        val restrictedCrn = PersonGenerator.RESTRICTION.crn
        assertThat(
            result[restrictedCrn],
            equalTo(
                CaseAccess(
                    restrictedCrn,
                    userExcluded = false,
                    userRestricted = true,
                    restrictionMessage = PersonGenerator.RESTRICTION.restrictionMessage
                )
            )
        )

        val noLimitCrn = PersonGenerator.DEFAULT.crn
        assertThat(
            result[noLimitCrn],
            equalTo(
                CaseAccess(
                    noLimitCrn,
                    userExcluded = false,
                    userRestricted = false
                )
            )
        )

        val bothCrn = PersonGenerator.RESTRICTION_EXCLUSION.crn
        assertThat(
            result[bothCrn],
            equalTo(
                CaseAccess(
                    bothCrn,
                    userExcluded = true,
                    userRestricted = true,
                    exclusionMessage = PersonGenerator.RESTRICTION_EXCLUSION.exclusionMessage,
                    restrictionMessage = PersonGenerator.RESTRICTION_EXCLUSION.restrictionMessage
                )
            )
        )
    }

    @Test
    fun `limited access controls do not prevent legitimate access`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.post("/users/${UserGenerator.AUDIT_USER.username.lowercase()}/access")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        listOf(
                            PersonGenerator.EXCLUSION.crn,
                            PersonGenerator.RESTRICTION.crn,
                            PersonGenerator.DEFAULT.crn
                        )
                    )
                )
        ).andReturn().response.contentAsString

        val userAccess = objectMapper.readValue<UserAccess>(res)
        val result: Map<String, CaseAccess> = userAccess.access.associateBy { it.crn }

        val exclusionCrn = PersonGenerator.EXCLUSION.crn
        assertThat(
            result[exclusionCrn],
            equalTo(CaseAccess(exclusionCrn, false, false))
        )
        val restrictionCrn = PersonGenerator.RESTRICTION.crn
        assertThat(
            result[restrictionCrn],
            equalTo(CaseAccess(restrictionCrn, false, false))
        )
        val default = PersonGenerator.DEFAULT.crn
        assertThat(
            result[default],
            equalTo(CaseAccess(default, false, false))
        )
    }

    @Test
    fun `validates that between 1 and 500 crns are provided`() {
        val ex = assertThrows<ServletException> {
            mockMvc.perform(
                MockMvcRequestBuilders.post("/users/${UserGenerator.AUDIT_USER.username.lowercase()}/access")
                    .withOAuth2Token(wireMockServer)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listOf<String>()))
            ).andReturn().response.contentAsString
        }
        assertThat(ex.cause, instanceOf(ConstraintViolationException::class.java))
        assertThat(ex.cause!!.message, equalTo("userAccessCheck.crns: Please provide between 1 and 500 crns"))
    }

    @Test
    fun `user details not found returns 404`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/users/non-existent-user/details")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `user details not found returns 404 from id`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/users/829185656291/details")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `user details are correctly returned`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/users/john-smith/details")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().response.contentAsString

        val userDetail = objectMapper.readValue<UserDetail>(res)
        assertThat(userDetail, equalTo(UserDetail("john-smith", Name("John", "Smith"), "john.smith@moj.gov.uk")))
    }

    @Test
    fun `user details are correctly returned from id`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/users/${ProviderGenerator.JOHN_SMITH_USER.id}/details")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().response.contentAsString

        val userDetail = objectMapper.readValue<UserDetail>(res)
        assertThat(userDetail, equalTo(UserDetail("john-smith", Name("John", "Smith"), "john.smith@moj.gov.uk")))
    }
}
