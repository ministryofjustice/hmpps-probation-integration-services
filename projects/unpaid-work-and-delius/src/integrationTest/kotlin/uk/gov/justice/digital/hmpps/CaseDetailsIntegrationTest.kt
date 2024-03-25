package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.controller.casedetails.model.name
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class CaseDetailsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `successful response`() {
        val person = CaseGenerator.DEFAULT
        val event = EventGenerator.DEFAULT

        mockMvc.perform(get("/case-data/${person.crn}/${event.id}").withToken())
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
            .andExpect(jsonPath("$.aliases[0].name.forename").value(AliasGenerator.DEFAULT.forename))
            .andExpect(jsonPath("$.aliases[0].name.middleName").value("${AliasGenerator.DEFAULT.secondName} ${AliasGenerator.DEFAULT.thirdName}"))
            .andExpect(jsonPath("$.aliases[0].name.surname").value(AliasGenerator.DEFAULT.surname))
            .andExpect(jsonPath("$.mainAddress.addressNumber").value(CaseAddressGenerator.DEFAULT.addressNumber))
            .andExpect(jsonPath("$.mainAddress.town").value(CaseAddressGenerator.DEFAULT.town))
            .andExpect(jsonPath("$.mainAddress.county").value(CaseAddressGenerator.DEFAULT.county))
            .andExpect(jsonPath("$.mainAddress.streetName").value(CaseAddressGenerator.DEFAULT.streetName))
            .andExpect(jsonPath("$.mainAddress.postcode").value(CaseAddressGenerator.DEFAULT.postcode))
            .andExpect(jsonPath("$.ethnicity").value(ReferenceDataGenerator.ETHNICITY_INDIAN.description))
            .andExpect(jsonPath("$.disabilities[0].type.code").value(ReferenceDataGenerator.DISABILITY_HEARING.code))
            .andExpect(jsonPath("$.disabilities[0].notes").value(DisabilityGenerator.DEFAULT.notes))
            .andExpect(jsonPath("$.provisions[0].type.description").value(ProvisionGenerator.DEFAULT.type.description))
            .andExpect(jsonPath("$.language.primaryLanguage").value(ReferenceDataGenerator.LANGUAGE_ENGLISH.description))
            .andExpect(jsonPath("$.phoneNumbers[0].type").value("MOBILE"))
            .andExpect(jsonPath("$.phoneNumbers[1].type").value("TELEPHONE"))
            .andExpect(jsonPath("$.mappaRegistration.level.code").value(ReferenceDataGenerator.MAPPA_LEVEL_1.code))
            .andExpect(jsonPath("$.mappaRegistration.category.code").value(ReferenceDataGenerator.MAPPA_CATEGORY_2.code))
            .andExpect(jsonPath("$.registerFlags[0].riskColour").value(RegisterTypeGenerator.DEFAULT.riskColour))
            .andExpect(jsonPath("$.sentence.mainOffence.category.code").value(OffenceGenerator.DEFAULT.mainCategoryCode))
            .andExpect(jsonPath("$.sentence.mainOffence.subCategory.code").value(OffenceGenerator.DEFAULT.subCategoryCode))
    }
}
