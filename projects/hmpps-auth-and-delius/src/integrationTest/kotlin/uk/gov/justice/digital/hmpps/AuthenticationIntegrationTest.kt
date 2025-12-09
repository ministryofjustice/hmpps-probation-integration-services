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
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class AuthenticationIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val ldapTemplate: LdapTemplate
) {

    @Test
    fun `successful authentication`() {
        mockMvc.post("/authenticate") {
            withToken()
            contentType = MediaType.APPLICATION_JSON
            content = """{"username": "test.user", "password": "secret"}"""
        }.andExpect { status { isOk() } }
    }

    @Test
    fun `failed authentication`() {
        mockMvc.post("/authenticate") {
            withToken()
            contentType = MediaType.APPLICATION_JSON
            content = """{"username": "test.user", "password": "incorrect"}"""
        }.andExpect {
            status { isUnauthorized() }
            content {
                json(
                    """
                    {
                      "status": 401,
                      "message": "Authentication failure"
                    }
                    """.trimIndent(),
                    JsonCompareMode.STRICT
                )
            }
        }
    }

    @Test
    fun `invalid authentication request`() {
        mockMvc.post("/authenticate") {
            withToken()
            contentType = MediaType.APPLICATION_JSON
            content = """{"username": "", "password": "   "}"""
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    @DirtiesContext
    fun `successful password change`() {
        assertThat(currentPassword(), equalTo("secret"))
        mockMvc.post("/user/test.user/password") {
            withToken()
            contentType = MediaType.APPLICATION_JSON
            content = """{"password": "new"}"""
        }.andExpect { status { isOk() } }
        assertThat(currentPassword(), equalTo("new"))
    }

    @Test
    fun `attempt to change password for non-existent user`() {
        mockMvc.post("/user/some user/password") {
            withToken()
            contentType = MediaType.APPLICATION_JSON
            content = """{"password": "new"}"""
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `invalid password change request`() {
        mockMvc.post("/user/some user/password") {
            withToken()
            contentType = MediaType.APPLICATION_JSON
            content = """{}"""
        }.andExpect { status { isBadRequest() } }
    }

    private fun currentPassword() = ldapTemplate.search(
        query().where("cn").`is`("test.user"),
        AttributesMapper { String(it["userPassword"].get() as ByteArray) }
    ).toList().single()
}
