package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.ldap.core.LdapTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_NO_USER
import uk.gov.justice.digital.hmpps.integrations.delius.LdapUser
import uk.gov.justice.digital.hmpps.integrations.delius.toAddress
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.jsonPath
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest
internal class BasicDetailsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val ldapTemplate: LdapTemplate
) {

    @Test
    fun `can retrieve all basic details`() {
        val person = PersonGenerator.DEFAULT_PERSON

        val response = mockMvc.get("/basic-details/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
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
                nomsNumber = person.nomsNumber,
                dateOfBirth = person.dateOfBirth
            )
        )
    }

    @Test
    fun `can retrieve user details for sign and send endpoint`() {
        val person = DEFAULT_PERSON
        val user = UserGenerator.DEFAULT
        val ldapUser = ldapTemplate.findByUsername<LdapUser>(user.username)!!
        val staff = StaffGenerator.DEFAULT
        val officeLocation = OfficeLocationGenerator.DEFAULT

        val response = mockMvc.get("/sign-and-send/${person.crn}/${user.username}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<SignAndSendResponse>()

        assertThat(response).isEqualTo(
            SignAndSendResponse(
                userDetails = LdapName(ldapUser.firstName, ldapUser.surname),
                responsibleOfficer = ResponsibleOfficerResponse(
                    title = staff.title?.description,
                    name = Name(staff.firstName, staff.middleName, staff.surname),
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
        )
    }

    @Test
    fun `can retrieve user details for sign and send endpoint for prison offender manager`() {
        val person = PersonGenerator.PERSON_NO_REGISTRATIONS
        val user = UserGenerator.OFFICER_2
        val ldapUser = ldapTemplate.findByUsername<LdapUser>(user.username)!!
        val staff = StaffGenerator.OFFICER_2
        val officeLocation = OfficeLocationGenerator.DEFAULT_2

        val response = mockMvc.get("/sign-and-send/${person.crn}/${user.username}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<SignAndSendResponse>()

        assertThat(response).isEqualTo(
            SignAndSendResponse(
                userDetails = LdapName(ldapUser.firstName, ldapUser.surname),
                responsibleOfficer = ResponsibleOfficerResponse(
                    title = staff.title?.description,
                    name = Name(staff.firstName, staff.middleName, staff.surname),
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
        )
    }

    @Test
    fun `username not found returns 404 response`() {
        mockMvc.get("/sign-and-send/${DEFAULT_PERSON.crn}/nonexistent") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `no home area returns no addresses`() {
        mockMvc.get("/sign-and-send/${PERSON_NO_USER.crn}/NoHomeArea") { withToken() }
            .andExpect {
                status { isOk() }
                content { jsonPath("$.responsibleOfficer.addresses", emptyList<OfficeAddress>()) }
            }
    }

    @Test
    fun `404 when crn not found`() {
        mockMvc.get("/sign-and-send/X987654") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `can retrieve crn from suicide risk form id successfully`() {
        val srfId = DocumentGenerator.SUICIDE_RISK_FORM_ID
        val response = mockMvc.get("/case/$srfId") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<DocumentCrn>()

        assertThat(response.crn).isEqualTo(DEFAULT_PERSON.crn)
    }
}