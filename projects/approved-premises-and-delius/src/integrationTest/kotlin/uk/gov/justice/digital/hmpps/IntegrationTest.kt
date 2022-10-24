package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ApprovedPremisesGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var wireMockServer: WireMockServer

    @Test
    fun `approved premises key worker staff are returned successfully`() {
        val approvedPremises = ApprovedPremisesGenerator.DEFAULT
        mockMvc
            .perform(get("/approved-premises/${approvedPremises.code.code}/staff").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.numberOfElements", equalTo(4)))
            .andExpect(
                jsonPath(
                    "$.content[*].name.surname",
                    equalTo(
                        listOf(
                            "Key-worker (team 1)",
                            "Key-worker (team 2)",
                            "Key-worker (team 3)",
                            "Normal AP staff (not key-worker)"
                        )
                    )
                )
            )
            .andExpect(
                jsonPath(
                    "$.content[*].keyWorker",
                    equalTo(
                        listOf(
                            true,
                            true,
                            true,
                            false
                        )
                    )
                )
            )
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
}
