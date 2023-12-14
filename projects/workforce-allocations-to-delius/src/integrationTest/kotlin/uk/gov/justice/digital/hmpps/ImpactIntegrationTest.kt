package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.AllocationImpact
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.api.model.toStaffMember
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ImpactIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @Test
    fun `get impact unauthorised`() {
        mockMvc.perform(
            get("/allocation-demand/impact?crn=N542873&staff=N012DT")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `get impact no matching crn`() {
        mockMvc.perform(
            get("/allocation-demand/impact?crn=N542873&staff=N012DT").withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `get impact no matching staff`() {
        mockMvc.perform(
            get("/allocation-demand/impact?crn=${PersonGenerator.DEFAULT.crn}&staff=N01DTT1").withOAuth2Token(
                wireMockserver
            )
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `get impact no crn provided`() {
        mockMvc.perform(
            get("/allocation-demand/impact?staff=N01DTT1").withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `get impact no staff code provided`() {
        mockMvc.perform(
            get("/allocation-demand/impact?crn=${PersonGenerator.DEFAULT.crn}").withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `get impact returns person and staff`() {
        val person = PersonGenerator.DEFAULT
        val staff = StaffGenerator.STAFF_WITH_USER.toStaffMember("example@example.com")

        val res = mockMvc.perform(
            get("/allocation-demand/impact?crn=${PersonGenerator.DEFAULT.crn}&staff=${staff.code}")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsString

        val impact = objectMapper.readValue<AllocationImpact>(res)

        assertThat(impact.crn, equalTo(person.crn))
        assertThat(impact.name, equalTo(person.name()))
        assertThat(impact.staff, equalTo(staff))
    }
}
