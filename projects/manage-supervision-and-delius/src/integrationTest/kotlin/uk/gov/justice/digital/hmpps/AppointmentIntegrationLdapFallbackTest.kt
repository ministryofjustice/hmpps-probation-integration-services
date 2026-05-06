package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.sentence.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.StaffTeam
import uk.gov.justice.digital.hmpps.api.model.sentence.User
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator
import uk.gov.justice.digital.hmpps.service.toUser
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class AppointmentIntegrationLdapFallbackTest : IntegrationTestBase() {

    @MockitoBean
    lateinit var ldapTemplate: LdapTemplate

    @Test
    fun `get staff by team returns staff when ldap lookup fails`() {
        whenever(ldapTemplate.search(any(), any<AttributesMapper<Any?>>()))
            .thenThrow(RuntimeException("ldap down"))

        val response = mockMvc.get("/appointment/staff/team/${OffenderManagerGenerator.TEAM.code}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<StaffTeam>()

        val expected = StaffTeam(
            listOf(
                OffenderManagerGenerator.STAFF_USER_1.toUser().copy(email = null),
                User(
                    staffCode = "Unallocated",
                    username = "Unallocated",
                    nameAndRole = "Unallocated",
                    name = Name("Unallocated", null, "Unallocated"),
                    email = null
                ),
                OffenderManagerGenerator.STAFF_USER_2.toUser().copy(email = null),
            )
        )

        assertEquals(expected, response)
    }
}
