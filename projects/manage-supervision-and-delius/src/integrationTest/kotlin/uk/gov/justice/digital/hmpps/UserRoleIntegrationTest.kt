package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.user.UserDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class UserRoleIntegrationTest : IntegrationTestBase() {

    @Test
    fun `unauthorized status returned`() {
        mockMvc.get("/caseload/user/peter-parker")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `get roles for user`() {
        val response = mockMvc.get("/user/peter-parker") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserDetails>()

        assertEquals(listOf("APBT001", "MAABT001"), response.roles)
    }
}