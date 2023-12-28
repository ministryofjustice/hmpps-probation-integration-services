package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.ldap.NameNotFoundException
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.support.LdapNameBuilder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.model.DeliusRole
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class UserRoleIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var ldapTemplate: LdapTemplate

    @Order(1)
    @Test
    fun `successfully updates ldap role`() {
        mockMvc.perform(put("/users/john-smith/roles/pf_std_probation").withToken())
            .andExpect(status().is2xxSuccessful)

        val res = ldapTemplate.lookupContext(
            LdapNameBuilder.newInstance("ou=Users")
                .add("cn", "john-smith")
                .add("cn", DeliusRole.CTRBT001.name)
                .build()
        )
        assertThat(res.dn.toString(), equalTo("cn=CTRBT001,cn=john-smith,ou=Users"))
    }

    @Order(2)
    @Test
    fun `successfully removes ldap role`() {
        mockMvc.perform(delete("/users/john-smith/roles/pf_std_probation").withToken())
            .andExpect(status().is2xxSuccessful)

        val res = assertThrows<NameNotFoundException> {
            ldapTemplate.lookupContext(
                LdapNameBuilder.newInstance("ou=Users")
                    .add("cn", "john-smith")
                    .add("cn", DeliusRole.CTRBT001.name)
                    .build()
            )
        }
        assertThat(
            res.message,
            equalTo("[LDAP: error code 32 - Unable to perform the search because base entry 'cn=CTRBT001,cn=john-smith,ou=Users,dc=moj,dc=com' does not exist in the server.]")
        )
    }
}
