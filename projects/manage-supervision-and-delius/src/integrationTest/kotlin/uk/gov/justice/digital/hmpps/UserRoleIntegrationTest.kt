package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.user.UserDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserRoleIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/caseload/user/peter-parker"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `get roles for user`() {
        val response = mockMvc
            .perform(MockMvcRequestBuilders.get("/user/peter-parker").withToken())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<UserDetails>()

        assertEquals(listOf("APBT001", "MAABT001"), response.roles)
    }
}