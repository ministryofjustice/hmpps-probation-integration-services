package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.ldap.NameNotFoundException
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.support.LdapNameBuilder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class IntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val ldapTemplate: LdapTemplate
) {

    @Test
    @Order(1)
    fun `can add role`() {
        mockMvc.put("/user/test.user/role") { withToken() }
            .andExpect { status { is2xxSuccessful() } }

        val role = ldapTemplate.lookupContext(LdapNameBuilder.newInstance("cn=IMSBT001,cn=test.user").build())

        assertThat(role.dn.toString(), equalTo("cn=IMSBT001,cn=test.user"))
    }

    @Test
    @Order(2)
    fun `can remove role`() {
        mockMvc.delete("/user/test.user/role") { withToken() }
            .andExpect { status { is2xxSuccessful() } }

        assertThrows<NameNotFoundException> {
            ldapTemplate.lookupContext(LdapNameBuilder.newInstance("cn=IMSBT001,cn=test.user").build())
        }
    }
}
