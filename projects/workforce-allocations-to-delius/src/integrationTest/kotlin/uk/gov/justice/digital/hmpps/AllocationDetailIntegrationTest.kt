package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AllocationDetailIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `get details unauthorised`() {
        mockMvc.perform(post("/allocation/details"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get details without request body is bad request`() {
        mockMvc.perform(post("/allocation/details").withToken())
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `get details without requests is bad request`() {
        mockMvc.perform(post("/allocation/details").withToken().withJson(AllocationDetailRequests(listOf())))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `get details for crn and staff code`() {
        val person = PersonGenerator.DEFAULT
        val staff = StaffGenerator.DEFAULT
        mockMvc
            .perform(
                post("/allocation/details").withToken()
                    .withJson(AllocationDetailRequests(listOf(AllocationDetailRequest(person.crn, staff.code))))
            )
            .andExpectJson(
                AllocationDetails(
                    listOf(
                        AllocationImpact(
                            person.crn,
                            person.name(),
                            staff.toStaffMember()
                        )
                    )
                )
            )
    }
}
