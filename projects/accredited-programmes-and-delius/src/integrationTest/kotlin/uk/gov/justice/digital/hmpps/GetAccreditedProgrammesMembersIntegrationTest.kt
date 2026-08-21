package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class GetAccreditedProgrammesMembersIntegrationTest(
    @Autowired private val mockMvc: MockMvc
) {
    @Test
    fun `returns unauthorized when no token is supplied`() {
        mockMvc.get("/regions/${TestData.AP_PROVIDER.code}/local-admin-units/accredited-programmes/members")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `can find accredited programmes members for a region including future end dated records`() {
        mockMvc.get("/regions/${TestData.AP_PROVIDER.code}/local-admin-units/accredited-programmes/members") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andExpect {
                jsonPath("teams.length()") { value(2) }
                jsonPath("teams[0].code") { value("N07AP1") }
                jsonPath("teams[0].members.length()") { value(2) }
                jsonPath("teams[0].members[0].code") { value("N07AP01") }
                jsonPath("teams[0].members[0].name.middleNames") { value("Middle") }
                jsonPath("teams[0].members[1].code") { value("N07AP03") }
                jsonPath("teams[0].members[1].name.middleNames") { value("TestTwo") }
                jsonPath("teams[1].code") { value("N07AP2") }
                jsonPath("teams[1].members.length()") { value(1) }
                jsonPath("teams[1].members[0].code") { value("N07AP02") }
                jsonPath("teams[1].members[0].name.middleNames") { value("Future") }
            }
    }

    @Test
    fun `does not include inactive team or inactive staff`() {
        mockMvc.get("/regions/${TestData.AP_PROVIDER.code}/local-admin-units/accredited-programmes/members") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andExpect {
                jsonPath("teams[?(@.code == 'N07AP9')]") { isEmpty() }
                jsonPath("teams[*].members[?(@.code == 'N07AP98')]") { isEmpty() }
                jsonPath("teams[*].members[?(@.code == 'N07AP99')]") { isEmpty() }
            }
    }

    @Test
    fun `returns empty teams list when region is mapped but has no AP members`() {
        mockMvc.get("/regions/N50/local-admin-units/accredited-programmes/members") { withToken() }
            .andExpect { status { isOk() } }
            .andExpect {
                jsonPath("teams.length()") { value(0) }
            }
    }

    @Test
    fun `returns 404 for unknown provider code`() {
        mockMvc.get("/regions/ZZ9/local-admin-units/accredited-programmes/members") { withToken() }
            .andExpect { status { isNotFound() } }
    }
}

