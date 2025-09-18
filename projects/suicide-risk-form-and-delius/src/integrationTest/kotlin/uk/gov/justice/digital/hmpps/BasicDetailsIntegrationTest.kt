package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.ldap.core.LdapTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.LdapUser
import uk.gov.justice.digital.hmpps.integrations.delius.toAddress
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest
class BasicDetailsIntegrationTest {

    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var wireMockServer: WireMockServer

    @Autowired
    internal lateinit var ldapTemplate: LdapTemplate

    @Test
    fun `can retrieve all basic details`() {
        val person = PersonGenerator.DEFAULT_PERSON

        val response = mockMvc
            .perform(get("/basic-details/${person.crn}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<BasicDetails>()

        assertThat(response).isEqualTo(
            BasicDetails(
                title = null,
                name = Name(
                    person.firstName,
                    listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
                    person.surname
                ),
                addresses = listOf(PersonGenerator.DEFAULT_ADDRESS.toAddress()),
                prisonNumber = person.prisonerNumber,
                dateOfBirth = person.dateOfBirth
            )
        )
    }

    @Test
    fun `can retrieve user details for sign and send endpoint`() {
        val user = UserGenerator.DEFAULT
        val staff = user.staff!!
        val ldapUser = ldapTemplate.findByUsername<LdapUser>(user.username)!!
        val officeLocation = OfficeLocationGenerator.DEFAULT

        val response = mockMvc
            .perform(get("/sign-and-send/${user.username}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<SignAndSendResponse>()

        assertThat(response).isEqualTo(
            SignAndSendResponse(
                title = staff.title?.code,
                name = Name(
                    staff.firstName,
                    staff.middleName,
                    staff.surname
                ),
                telephoneNumber = ldapUser.telephoneNumber,
                emailAddress = ldapUser.email,
                addresses = listOf(
                    OfficeAddress(
                        id = officeLocation.id,
                        status = "Default",
                        officeDescription = officeLocation.description,
                        buildingNumber = officeLocation.buildingNumber,
                        buildingName = officeLocation.buildingName,
                        streetName = officeLocation.streetName,
                        townCity = officeLocation.townCity,
                        district = officeLocation.district,
                        county = officeLocation.county,
                        postcode = officeLocation.postcode
                    )
                )
            )
        )
    }

    @Test
    fun `can retrieve crn from suicide risk form id successfully`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val srfId = DocumentGenerator.SUICIDE_RISK_FORM_ID
        val response = mockMvc
            .perform(get("/case/$srfId").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<DocumentCrn>()

        assertThat(response.crn).isEqualTo(person.crn)
    }
}