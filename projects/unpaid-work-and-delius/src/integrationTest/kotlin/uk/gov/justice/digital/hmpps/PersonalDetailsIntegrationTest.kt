package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.controller.personaldetails.model.name
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class PersonalDetailsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `successful response`() {
        val person = CaseGenerator.DEFAULT
        mockMvc.get("/case-data/${person.crn}/personal-details") {
            withToken()
        }.andExpect {
            status { is2xxSuccessful() }

            jsonPath("$.crn") { value(person.crn) }

            jsonPath("$.personalCircumstances[0].type.code") { value(PersonalCircumstanceTypeGenerator.DEFAULT.code) }
            jsonPath("$.personalCircumstances[0].subType.code") { value(PersonalCircumstanceSubTypeGenerator.DEFAULT.code) }
            jsonPath("$.personalCircumstances[0].evidenced") { value(PersonalCircumstanceGenerator.DEFAULT.evidenced) }
            jsonPath("$.personalCircumstances[0].notes") { value(PersonalCircumstanceGenerator.DEFAULT.notes) }

            jsonPath("$.personalContacts[0].relationship") { value(PersonalContactGenerator.DEFAULT.relationship) }
            jsonPath("$.personalContacts[0].relationshipType.code") { value(PersonalContactGenerator.DEFAULT.relationshipType.code) }
            jsonPath("$.personalContacts[0].name.forename") { value(PersonalContactGenerator.DEFAULT.name().forename) }
            jsonPath("$.personalContacts[0].name.middleName") { value(PersonalContactGenerator.DEFAULT.name().middleName) }
            jsonPath("$.personalContacts[0].name.surname") { value(PersonalContactGenerator.DEFAULT.name().surname) }
            jsonPath("$.personalContacts[0].mobileNumber") { value(PersonalContactGenerator.DEFAULT.mobileNumber) }
            jsonPath("$.personalContacts[0].telephoneNumber") { value(AddressGenerator.DEFAULT.telephoneNumber) }
            jsonPath("$.personalContacts[0].address.addressNumber") { value(AddressGenerator.DEFAULT.addressNumber) }
            jsonPath("$.personalContacts[0].address.streetName") { value(AddressGenerator.DEFAULT.streetName) }
            jsonPath("$.personalContacts[0].address.town") { value(AddressGenerator.DEFAULT.town) }
        }
    }
}
