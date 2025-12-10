package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AllocationDetailIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `get details unauthorised`() {
        mockMvc.post("/allocation/details")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `get details without request body is bad request`() {
        mockMvc.post("/allocation/details") { withToken() }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `get details without requests is bad request`() {
        mockMvc.post("/allocation/details") {
            withToken()
            json = AllocationDemandRequest(listOf())
        }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `get details for crn and staff code`() {
        val person = PersonGenerator.DEFAULT
        val staff = StaffGenerator.DEFAULT
        mockMvc.post("/allocation/details") {
            withToken()
            json = AllocationDetailRequests(listOf(AllocationDetailRequest(person.crn, staff.code)))
        }
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
