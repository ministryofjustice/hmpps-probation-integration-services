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
import uk.gov.justice.digital.hmpps.data.generator.ApprovedPremisesGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class StaffControllerIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Test
    fun `approved premises key worker staff are returned successfully`() {
        val approvedPremises = ApprovedPremisesGenerator.DEFAULT
        mockMvc
            .perform(get("/approved-premises/${approvedPremises.code.code}/staff").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.numberOfElements", equalTo(3)))
            .andExpect(jsonPath("$.size", equalTo(100)))
            .andExpect(
                jsonPath(
                    "$.content[*].name.surname",
                    equalTo(listOf("Key-worker", "Not key-worker", "Unallocated"))
                )
            )
            .andExpect(jsonPath("$.content[*].keyWorker", equalTo(listOf(true, false, false))))
    }

    @Test
    fun `empty approved premises returns 200 with empty results`() {
        val approvedPremises = ApprovedPremisesGenerator.NO_STAFF
        mockMvc
            .perform(get("/approved-premises/${approvedPremises.code.code}/staff").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.numberOfElements", equalTo(0)))
            .andExpect(jsonPath("$.totalElements", equalTo(0)))
    }

    @Test
    fun `non-existent approved premises returns 404`() {
        mockMvc
            .perform(get("/approved-premises/NOTFOUND/staff").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message", equalTo("Approved Premises with code of NOTFOUND not found")))
    }

    @Test
    fun `approved premises key workers only are returned successfully`() {
        val approvedPremises = ApprovedPremisesGenerator.DEFAULT
        mockMvc.perform(
            get("/approved-premises/${approvedPremises.code.code}/staff?keyWorker=true").withOAuth2Token(
                wireMockServer
            )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.numberOfElements", equalTo(1)))
            .andExpect(jsonPath("$.content[*].name.surname", equalTo(listOf("Key-worker"))))
            .andExpect(jsonPath("$.content[*].keyWorker", equalTo(listOf(true))))
    }

    @Test
    fun `Get staff by username`() {
        val username = StaffGenerator.DEFAULT_STAFF.user!!.username
        mockMvc.perform(get("/staff/$username").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username", equalTo(username)))
            .andExpect(jsonPath("$.name.surname", equalTo(StaffGenerator.DEFAULT_STAFF.surname)))
            .andExpect(jsonPath("$.name.forename", equalTo(StaffGenerator.DEFAULT_STAFF.forename)))
            .andExpect(jsonPath("$.code", equalTo(StaffGenerator.DEFAULT_STAFF.code)))
    }
}
