package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class StaffIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `get staff by code`() {
        mockMvc
            .get("/staff/STAFF01") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("code") { equalTo("STAFF01") }
                jsonPath("username") { equalTo("test.user") }
                jsonPath("name.forenames") { equalTo("Test") }
                jsonPath("name.surname") { equalTo("Staff") }
                jsonPath("teams[*].code") { equalTo(listOf("TEAM01", "TEAM02")) }
            }
    }

    @Test
    fun `get staff by username`() {
        mockMvc
            .get("/staff?username=test.user") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("code") { equalTo("STAFF01") }
                jsonPath("username") { equalTo("test.user") }
            }
    }

    @Test
    fun `staff by code not found`() {
        mockMvc.get("/staff/DOESNOTEXIST") { withToken() }
            .andExpect {
                status { isNotFound() }
                jsonPath("message") { equalTo("Staff with code of DOESNOTEXIST not found") }
            }
    }

    @Test
    fun `staff by username not found`() {
        mockMvc.get("/staff?username=DOESNOTEXIST") { withToken() }
            .andExpect {
                status { isNotFound() }
                jsonPath("message") { equalTo("Staff with username of DOESNOTEXIST not found") }
            }
    }

    @Test
    fun `staff by id not found`() {
        mockMvc
            .get("/staff?id=-1") { withToken() }
            .andExpect {
                status { isNotFound() }
                jsonPath("message") { equalTo("Staff with staffId of -1 not found") }
            }
    }

    @Test
    fun `get managed prisoners`() {
        mockMvc
            .get("/staff/STAFF01/managedPrisonerIds") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("[*]") { equalTo(listOf("PERSON1")) }
            }
    }

    @Test
    fun `get community manager`() {
        mockMvc
            .get("/case/PERSON1/communityManager") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("code") { equalTo("STAFF01") }
                jsonPath("name.forenames") { equalTo("Test") }
                jsonPath("name.surname") { equalTo("Staff") }
                jsonPath("team.code") { equalTo("TEAM02") }
                jsonPath("localAdminUnit.code") { equalTo("LAU") }
                jsonPath("provider.code") { equalTo("TST") }
                jsonPath("isUnallocated") { equalTo(false) }
            }
    }

    @Test
    fun `community manager not found`() {
        mockMvc
            .get("/case/DOESNOTEXIST/communityManager") { withToken() }
            .andExpect {
                status { isNotFound() }
                jsonPath("message") { equalTo("Community manager for case with nomsNumber of DOESNOTEXIST not found") }
            }
    }
}
