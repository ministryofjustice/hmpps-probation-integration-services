package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.MediaType
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class AuthenticationIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var ldapTemplate: LdapTemplate

    @Test
    fun `successful authentication`() {
        mockMvc.perform(authenticate("""{"username": "test.user", "password": "secret"}"""))
            .andExpect(status().isOk)
    }

    @Test
    fun `failed authentication`() {
        mockMvc.perform(authenticate("""{"username": "test.user", "password": "incorrect"}"""))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `invalid authentication request`() {
        mockMvc.perform(authenticate("""{"username": "", "password": "   "}"""))
            .andExpect(status().isBadRequest)
    }

    @Test
    @DirtiesContext
    fun `successful password change`() {
        assertThat(currentPassword(), equalTo("secret"))
        mockMvc.perform(changePassword("test.user", """{"password": "new"}"""))
            .andExpect(status().isOk)
        assertThat(currentPassword(), equalTo("new"))
    }

    @Test
    fun `attempt to change password for non-existent user`() {
        mockMvc.perform(changePassword("some user", """{"password": "new"}"""))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `invalid password change request`() {
        mockMvc.perform(changePassword("some user", """{}"""))
            .andExpect(status().isBadRequest)
    }

    private fun authenticate(json: String) = post("/authenticate").withToken()
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)

    private fun changePassword(username: String, json: String) =
        post("/user/$username/password").withToken()
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)

    private fun currentPassword() =
        ldapTemplate.search(
            "ou=Users",
            "cn=test.user",
            AttributesMapper { String(it["userPassword"].get() as ByteArray) })
            .toList().single()
}
