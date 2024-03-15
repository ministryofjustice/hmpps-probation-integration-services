package uk.gov.justice.digital.hmpps.api.controller

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.personalDetails.*
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.service.PersonalDetailsService
import uk.gov.justice.digital.hmpps.service.toAddress
import uk.gov.justice.digital.hmpps.service.toContact
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class PersonalDetailsControllerTest {

    @Mock
    lateinit var personalDetailsService: PersonalDetailsService

    @InjectMocks
    lateinit var controller: PersonalDetailsController

    @Test
    fun `calls personal details service to get data`() {
        val crn = "X000005"
        val personalDetails = PersonalDetails(
            crn = "X000005",
            pnc = "pnc123",
            name = Name(forename = "Stephen", middleName = "Harry", surname = "Bloggs"),
            circumstances = Circumstances(emptyList(), lastUpdated = null),
            disabilities = Disabilities(emptyList(), lastUpdated = null),
            provisions = Provisions(emptyList(), lastUpdated = null),
            contacts = emptyList(),
            dateOfBirth = LocalDate.now().minusYears(50),
            documents = emptyList(),
            email = "testemail",
            mainAddress = PersonDetailsGenerator.PERSON_ADDRESS_1.toAddress(),
            mobileNumber = "0897672332",
            telephoneNumber = "090876522",
            otherAddressCount = 1,
            previousAddressCount = 1,
            preferredGender = "Male",
            preferredName = "Steve",
            religionOrBelief = "Christian",
            sex = "Male",
            sexualOrientation = "Heterosexual",
            previousSurname = "Smith",
            preferredLanguage = "English",
            aliases = emptyList(),
            genderIdentity = null,
            selfDescribedGender = null
        )
        whenever(personalDetailsService.getPersonalDetails(crn)).thenReturn(personalDetails)
        val res = controller.getPersonalDetails("X000005")
        assertThat(res.preferredName, equalTo("Steve"))
        assertThat(res.mainAddress?.streetName, equalTo("Test Street"))
    }

    @Test
    fun `calls personal details service to download document`() {
        val crn = "X000005"
        val alfrescoId = "A001"
        val expectedResponse = ResponseEntity<StreamingResponseBody>(HttpStatus.OK)
        whenever(personalDetailsService.downloadDocument(crn, alfrescoId)).thenReturn(expectedResponse)
        val res = controller.downloadDocument(crn, alfrescoId)
        Assertions.assertEquals(expectedResponse.statusCode, res.statusCode)
    }

    @Test
    fun `calls get Person Contact function `() {
        val crn = "X000005"
        val id = 1234L
        val expectedResponse = PersonDetailsGenerator.PERSONAL_CONTACT_1.toContact()
        whenever(personalDetailsService.getPersonContact(crn, id)).thenReturn(expectedResponse)
        val res = controller.getPersonContact(crn, id)
        assertThat(res.relationship, equalTo(PersonDetailsGenerator.PERSONAL_CONTACT_1.relationship))
    }

    @Test
    fun `calls get Person summary function `() {
        val crn = "X000005"
        val expectedResponse = PersonSummary(
            Name(forename = "TestName", middleName = null, surname = "TestSurname"), pnc = "Test PNC",
            crn = "CRN",
            dateOfBirth = LocalDate.now(),
        )

        whenever(personalDetailsService.getPersonSummary(crn)).thenReturn(expectedResponse)
        val res = controller.getPersonSummary(crn)
        assertThat(res, equalTo(expectedResponse))
    }

    @Test
    fun `calls get addresses function `() {
        val crn = "X000005"
        val expectedSummary = PersonSummary(
            Name(forename = "TestName", middleName = null, surname = "TestSurname"), pnc = "Test PNC",
            crn = "CRN",
            dateOfBirth = LocalDate.now(),
        )

        val expectedResponse = AddressOverview(
            personSummary = expectedSummary, mainAddress = PersonDetailsGenerator.PERSON_ADDRESS_1.toAddress(),
            otherAddresses = listOfNotNull(PersonDetailsGenerator.PERSON_ADDRESS_2.toAddress()),
            previousAddresses = listOfNotNull(PersonDetailsGenerator.PREVIOUS_ADDRESS.toAddress())
        )
        whenever(personalDetailsService.getPersonAddresses(crn)).thenReturn(expectedResponse)
        val res = controller.getPersonAddresses(crn)
        assertThat(res, equalTo(expectedResponse))
    }
}