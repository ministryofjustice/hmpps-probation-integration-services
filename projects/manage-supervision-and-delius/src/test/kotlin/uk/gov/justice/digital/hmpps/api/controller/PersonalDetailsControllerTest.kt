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
import uk.gov.justice.digital.hmpps.api.model.personalDetails.Circumstances
import uk.gov.justice.digital.hmpps.api.model.personalDetails.Disabilities
import uk.gov.justice.digital.hmpps.api.model.personalDetails.PersonalDetails
import uk.gov.justice.digital.hmpps.api.model.personalDetails.Provisions
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.service.PersonalDetailsService
import uk.gov.justice.digital.hmpps.service.toAddress
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
            otherAddresses = listOfNotNull(PersonDetailsGenerator.PERSON_ADDRESS_2.toAddress()),
            preferredGender = "Male",
            preferredName = "Steve",
            religionOrBelief = "Christian",
            sex = "Male",
            sexualOrientation = "Heterosexual"
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
}