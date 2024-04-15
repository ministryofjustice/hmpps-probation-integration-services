package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivity
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.service.toActivity
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ActivityIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `all person activity is returned`() {

        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/activity/${person.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonActivity>()

        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.activities.size, equalTo(8))
        assertThat(res.activities[0].isCommunication, equalTo(true))
        assertThat(res.activities[0].isSystemContact, equalTo(true))
        assertThat(res.activities[3].id, equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().id))
        assertThat(res.activities[3].type, equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().type))
        assertThat(
            res.activities[2].location?.officeName,
            equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().location?.officeName)
        )
        assertThat(res.activities[3].location?.postcode, equalTo("H34 7TH"))
        assertThat(res.activities[3].isAppointment, equalTo(true))
        assertThat(res.activities[2].documents.size, equalTo(2))
        assertThat(res.activities[4].isAppointment, equalTo(false))
        assertThat(res.activities[1].documents.size, equalTo(0))
        assertThat(res.activities[1].action, equalTo("Breach Enforcement Action"))
    }
}
