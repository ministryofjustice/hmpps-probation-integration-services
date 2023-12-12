package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.controller.personaldetails.model.name
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceSubTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalContactGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class PersonalDetailsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @Test
    fun `successful response`() {
        val person = CaseGenerator.DEFAULT
        mockMvc.perform(
            get("/case-data/${person.crn}/personal-details").withOAuth2Token(wireMockserver)
        )
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.crn").value(person.crn))
            .andExpect(jsonPath("$.personalCircumstances[0].type.code").value(PersonalCircumstanceTypeGenerator.DEFAULT.code))
            .andExpect(jsonPath("$.personalCircumstances[0].subType.code").value(PersonalCircumstanceSubTypeGenerator.DEFAULT.code))
            .andExpect(jsonPath("$.personalCircumstances[0].evidenced").value(PersonalCircumstanceGenerator.DEFAULT.evidenced))
            .andExpect(jsonPath("$.personalCircumstances[0].notes").value(PersonalCircumstanceGenerator.DEFAULT.notes))
            .andExpect(jsonPath("$.personalContacts[0].relationship").value(PersonalContactGenerator.DEFAULT.relationship))
            .andExpect(jsonPath("$.personalContacts[0].relationshipType.code").value(PersonalContactGenerator.DEFAULT.relationshipType.code))
            .andExpect(jsonPath("$.personalContacts[0].name.forename").value(PersonalContactGenerator.DEFAULT.name().forename))
            .andExpect(jsonPath("$.personalContacts[0].name.middleName").value(PersonalContactGenerator.DEFAULT.name().middleName))
            .andExpect(jsonPath("$.personalContacts[0].name.surname").value(PersonalContactGenerator.DEFAULT.name().surname))
            .andExpect(jsonPath("$.personalContacts[0].mobileNumber").value(PersonalContactGenerator.DEFAULT.mobileNumber))
            .andExpect(jsonPath("$.personalContacts[0].telephoneNumber").value(AddressGenerator.DEFAULT.telephoneNumber))
            .andExpect(jsonPath("$.personalContacts[0].address.addressNumber").value(AddressGenerator.DEFAULT.addressNumber))
            .andExpect(jsonPath("$.personalContacts[0].address.streetName").value(AddressGenerator.DEFAULT.streetName))
            .andExpect(jsonPath("$.personalContacts[0].address.town").value(AddressGenerator.DEFAULT.town))
    }
}
