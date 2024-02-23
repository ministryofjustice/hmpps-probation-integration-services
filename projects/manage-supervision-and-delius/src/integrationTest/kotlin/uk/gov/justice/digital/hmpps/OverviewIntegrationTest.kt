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
import uk.gov.justice.digital.hmpps.api.model.overview.Overview
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.ADDITIONAL_OFFENCE_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.ADDITIONAL_OFFENCE_2
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.MAIN_OFFENCE
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class OverviewIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `overview details are returned`() {
        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/overview/${person.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<Overview>()
        assertThat(res.personalDetails.name.forename, equalTo(person.forename))
        assertThat(res.personalDetails.disabilities[0].description, equalTo(OVERVIEW.disabilities.elementAt(0).type.description))
        assertThat(res.personalDetails.disabilities[1].description, equalTo(OVERVIEW.disabilities.elementAt(1).type.description))
        assertThat(res.personalDetails.provisions[0].description, equalTo(OVERVIEW.provisions.elementAt(0).type.description))
        assertThat(res.personalDetails.provisions[1].description, equalTo(OVERVIEW.provisions.elementAt(1).type.description))
        assertThat(res.personalDetails.personalCircumstances[0].type, equalTo(OVERVIEW.personalCircumstances.elementAt(0).type.description))
        assertThat(res.personalDetails.personalCircumstances[1].type, equalTo(OVERVIEW.personalCircumstances.elementAt(1).type.description))
        assertThat(res.schedule.nextAppointment?.date, equalTo(ContactGenerator.FIRST_APPT_CONTACT.date.toLocalDateTime()))
        assertThat(res.schedule.nextAppointment?.description, equalTo(ContactGenerator.FIRST_APPT_CONTACT.type.description))
        assertThat(res.sentences.size, equalTo(2))
//        assertThat(res.sentences[0].mainOffence?.description, equalTo(MAIN_OFFENCE.offence.description))
//        assertThat(res.sentences[0].additionalOffences[1].description,  equalTo(ADDITIONAL_OFFENCE_1.offence.description))
//        assertThat(res.sentences[0].additionalOffences[0].description,  equalTo(ADDITIONAL_OFFENCE_2.offence.description))
//        assertThat(res.sentences[0].rar?.completed,  equalTo(1))
//        assertThat(res.sentences[0].rar?.scheduled,  equalTo(1))
//        assertThat(res.sentences[0].rar?.totalDays,  equalTo(2))
//        assertThat(res.previousOrders.count, equalTo(2))
//        assertThat(res.previousOrders.breaches, equalTo(2))
    }
}
