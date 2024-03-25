package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.AllocationImpact
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.api.model.toStaffMember
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ImpactIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `get impact unauthorised`() {
        mockMvc.perform(get("/allocation-demand/impact?crn=N542873&staff=N012DT"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get impact no matching crn`() {
        mockMvc.perform(get("/allocation-demand/impact?crn=N542873&staff=N012DT").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get impact no matching staff`() {
        mockMvc.perform(get("/allocation-demand/impact?crn=${PersonGenerator.DEFAULT.crn}&staff=N01DTT1").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get impact no crn provided`() {
        mockMvc.perform(get("/allocation-demand/impact?staff=N01DTT1").withToken())
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `get impact no staff code provided`() {
        mockMvc.perform(get("/allocation-demand/impact?crn=${PersonGenerator.DEFAULT.crn}").withToken())
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `get impact returns person and staff`() {
        val person = PersonGenerator.DEFAULT
        val staff = StaffGenerator.STAFF_WITH_USER.toStaffMember("example@example.com")

        val impact = mockMvc
            .perform(get("/allocation-demand/impact?crn=${PersonGenerator.DEFAULT.crn}&staff=${staff.code}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<AllocationImpact>()

        assertThat(impact.crn, equalTo(person.crn))
        assertThat(impact.name, equalTo(person.name()))
        assertThat(impact.staff, equalTo(staff))
    }
}
