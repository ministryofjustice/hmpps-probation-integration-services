package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class StaffIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Test
    fun `get staff by code`() {
        mockMvc
            .perform(get("/staff/STAFF01").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("code", equalTo("STAFF01")))
            .andExpect(jsonPath("username", equalTo("test.user")))
            .andExpect(jsonPath("name.forenames", equalTo("Test")))
            .andExpect(jsonPath("name.surname", equalTo("Staff")))
            .andExpect(jsonPath("teams[*].code", equalTo(listOf("TEAM1", "TEAM2"))))
    }

    @Test
    fun `get staff by username`() {
        mockMvc
            .perform(get("/staff?username=test.user").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("code", equalTo("STAFF01")))
            .andExpect(jsonPath("username", equalTo("test.user")))
    }

    @Test
    fun `staff by code not found`() {
        mockMvc.perform(get("/staff/DOESNOTEXIST").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("message", equalTo("Staff with code of DOESNOTEXIST not found")))
    }

    @Test
    fun `staff by username not found`() {
        mockMvc.perform(get("/staff?username=DOESNOTEXIST").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("message", equalTo("Staff with username of DOESNOTEXIST not found")))
    }

    @Test
    fun `staff by id not found`() {
        mockMvc
            .perform(get("/staff?id=-1").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("message", equalTo("Staff with staffId of -1 not found")))
    }

    @Test
    fun `get managed prisoners`() {
        mockMvc
            .perform(get("/staff/STAFF01/managedPrisonerIds").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("[*]", equalTo(listOf("PERSON1"))))
    }

    @Test
    fun `get community manager`() {
        mockMvc
            .perform(get("/case/PERSON1/communityManager").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("code", equalTo("STAFF01")))
            .andExpect(jsonPath("name.forenames", equalTo("Test")))
            .andExpect(jsonPath("name.surname", equalTo("Staff")))
            .andExpect(jsonPath("team.code", equalTo("TEAM2")))
            .andExpect(jsonPath("localAdminUnit.code", equalTo("LAU")))
            .andExpect(jsonPath("provider.code", equalTo("TST")))
            .andExpect(jsonPath("isUnallocated", equalTo(false)))
    }

    @Test
    fun `community manager not found`() {
        mockMvc
            .perform(get("/case/DOESNOTEXIST/communityManager").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("message", equalTo("Community manager for case with nomsNumber of DOESNOTEXIST not found")))
    }
}
