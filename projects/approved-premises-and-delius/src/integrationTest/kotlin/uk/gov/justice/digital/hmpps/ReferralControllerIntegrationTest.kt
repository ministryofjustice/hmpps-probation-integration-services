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
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.model.ExistingReferrals
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReferralControllerIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `existing referrals for a crn are returned successfully`() {
        val person = PersonGenerator.DEFAULT
        val res = mockMvc
            .perform(get("/probation-case/${person.crn}/referrals").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<ExistingReferrals>()

        assertThat(res.referrals.size, equalTo(1))
    }
}
