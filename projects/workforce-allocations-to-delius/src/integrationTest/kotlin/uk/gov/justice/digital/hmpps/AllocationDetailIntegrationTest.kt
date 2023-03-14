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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.AllocationDetailRequest
import uk.gov.justice.digital.hmpps.api.model.AllocationDetailRequests
import uk.gov.justice.digital.hmpps.api.model.AllocationDetails
import uk.gov.justice.digital.hmpps.api.model.AllocationImpact
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.api.model.toStaffMember
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AllocationDetailIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @Test
    fun `get details unauthorised`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/allocation/details")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `get details without request body is bad request`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/allocation/details")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `get details without requests is bad request`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/allocation/details")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        AllocationDetailRequests(listOf())
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `get details for crn and staff code`() {
        val person = PersonGenerator.DEFAULT
        val staff = StaffGenerator.DEFAULT
        val res = mockMvc.perform(
            MockMvcRequestBuilders.post("/allocation/details")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        AllocationDetailRequests(
                            listOf(
                                AllocationDetailRequest(person.crn, staff.code)
                            )
                        )
                    )
                )
        ).andReturn().response.contentAsString

        val details = objectMapper.readValue<AllocationDetails>(res)
        assertThat(details.cases.first(), equalTo(AllocationImpact(person.crn, person.name(), staff.toStaffMember())))
    }
}
