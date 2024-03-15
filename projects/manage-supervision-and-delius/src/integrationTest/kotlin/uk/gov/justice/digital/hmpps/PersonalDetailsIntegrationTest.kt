package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.personalDetails.AddressOverview
import uk.gov.justice.digital.hmpps.api.model.personalDetails.PersonalContact
import uk.gov.justice.digital.hmpps.api.model.personalDetails.PersonalDetails
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.ALIAS_1
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_CONTACT_1
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_DETAILS
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PREVIOUS_ADDRESS
import uk.gov.justice.digital.hmpps.service.toContact
import uk.gov.justice.digital.hmpps.service.toSummary
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class PersonalDetailsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `personal details are returned`() {

        val person = PERSONAL_DETAILS
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonalDetails>()
        assertThat(res.crn, equalTo(person.crn))
        assertThat(res.name, equalTo(Name("Caroline", "Louise", "Bloggs")))
        assertThat(res.preferredName, equalTo("Caz"))
        assertThat(res.preferredGender, equalTo("Female"))
        assertThat(res.religionOrBelief, equalTo("Christian"))
        assertThat(res.preferredLanguage, equalTo("Arabic"))
        assertThat(res.previousSurname, equalTo("Smith"))
        assertThat(res.sexualOrientation, equalTo("Heterosexual"))
        assertThat(res.mainAddress?.status, equalTo("Main Address"))
        assertThat(res.mainAddress?.verified, equalTo(true))
        assertThat(res.mainAddress?.type, equalTo("Address type 1"))
        assertThat(res.mainAddress?.postcode, equalTo("NE2 56A"))
        assertThat(res.otherAddressCount, equalTo(1))
        assertThat(res.previousAddressCount, equalTo(1))
        assertThat(res.contacts.size, equalTo(1))
        assertThat(res.contacts[0].contactId, equalTo(PERSONAL_CONTACT_1.id))
        assertThat(res.contacts[0].name, equalTo(Name("Sam", "Steven", "Smith")))
        assertThat(res.contacts[0].address?.postcode, equalTo("NE1 56A"))
        assertThat(res.contacts[0].relationship, equalTo("Brother"))
        assertThat(res.contacts[0].relationshipType, equalTo("Family Member"))
        assertThat(res.circumstances.circumstances.size, equalTo(2))
        assertThat(res.circumstances.lastUpdated, equalTo(LocalDate.now().minusDays(1)))
        assertThat(res.circumstances.circumstances[0].type, equalTo("Employed"))
        assertThat(res.circumstances.circumstances[0].subType, equalTo("Full-time employed (30 or more hours per week"))
        assertThat(res.circumstances.circumstances[1].type, equalTo("Owns house"))
        assertThat(res.circumstances.circumstances[1].subType, equalTo("Has children"))
        assertThat(res.disabilities.lastUpdated, equalTo(LocalDate.now().minusDays(1)))
        assertThat(res.disabilities.disabilities[0], equalTo("Some Illness"))
        assertThat(res.disabilities.disabilities[1], equalTo("Blind"))
        assertThat(res.provisions.lastUpdated, equalTo(LocalDate.now().minusDays(1)))
        assertThat(res.provisions.provisions[0], equalTo("Braille"))
        assertThat(res.provisions.provisions[1], equalTo("Lots of breaks"))
        assertThat(res.documents.size, equalTo(2))
        assertThat(res.documents[0].name, equalTo("induction.doc"))
        assertThat(res.documents[1].name, equalTo("other.doc"))
        assertThat(res.documents[0].id, equalTo("A001"))
        assertThat(res.documents[1].id, equalTo("A002"))
        assertThat(res.aliases[0].forename, equalTo(ALIAS_1.forename))
        assertThat(res.genderIdentity, equalTo("Test Gender Identity"))
        assertThat(res.selfDescribedGender, equalTo("Some gender description"))
    }

    @Test
    fun `not found status returned`() {
        mockMvc
            .perform(get("/personal-details/X123456").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(get("/personal-details/X000005"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `document can be downloaded`() {
        mockMvc.perform(get("/personal-details/X000005/document/A001").accept("application/octet-stream").withToken())
            .andExpect(MockMvcResultMatchers.request().asyncStarted())
            .andDo(MvcResult::getAsyncResult)
            .andExpect(status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/octet-stream"))
            .andExpect(
                MockMvcResultMatchers.header().string(
                    "Content-Disposition",
                    "attachment; filename=\"=?UTF-8?Q?induction.doc?=\"; filename*=UTF-8''induction.doc"
                )
            )
            .andExpect(MockMvcResultMatchers.header().doesNotExist("Custom-Alfresco-Header"))
            .andExpect(
                MockMvcResultMatchers.content()
                    .bytes(ResourceUtils.getFile("classpath:simulations/__files/document.pdf").readBytes())
            )
    }

    @Test
    fun `document can not be found`() {
        mockMvc.perform(get("/personal-details/X000005/document/A010").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `personal summary is returned`() {

        val person = PERSONAL_DETAILS
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/summary").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonSummary>()
        assertThat(res.crn, equalTo(person.crn))
        assertThat(res.pnc, equalTo(person.pnc))
        assertThat(res.dateOfBirth, equalTo(person.dateOfBirth))
        assertThat(res.name, equalTo(Name(person.forename, person.secondName, person.surname)))
    }

    @Test
    fun `personal contact is returned`() {

        val person = PERSONAL_DETAILS
        val contact = PERSONAL_CONTACT_1
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/personal-contact/${contact.id}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonalContact>()
        assertThat(res, equalTo(contact.toContact()))
    }

    @Test
    fun `personal summary not found`() {
        mockMvc
            .perform(get("/personal-details/X999999/summary").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `personal contact not found`() {
        mockMvc
            .perform(get("/personal-details/X999999/personal-contact/999999999").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `addresses are returned`() {
        val person = PERSONAL_DETAILS
        val contact = PERSONAL_CONTACT_1
        val res = mockMvc
            .perform(get("/personal-details/${person.crn}/addresses").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<AddressOverview>()
        assertThat(res.personSummary, equalTo(person.toSummary()))
        assertThat(res.mainAddress?.postcode, equalTo("NE2 56A"))
        assertThat(res.previousAddresses[0].postcode, equalTo("NE4 END"))
        assertThat(res.previousAddresses[0].to, equalTo(PREVIOUS_ADDRESS.endDate))
        assertThat(res.otherAddresses[0].status, equalTo("Another Address"))
    }

    @Test
    fun `addresses person not found`() {
        mockMvc
            .perform(get("/personal-details/X999999/addresses").withToken())
            .andExpect(status().isNotFound)
    }
}
