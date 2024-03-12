package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.PersonalDetailsRepository

@ExtendWith(MockitoExtension::class)
internal class PersonalDetailsServiceTest {

    @Mock
    lateinit var personRepository: PersonalDetailsRepository

    @Mock
    lateinit var addressRepository: PersonAddressRepository

    @Mock
    lateinit var documentRepository: DocumentRepository

    @Mock
    lateinit var alfrescoClient: AlfrescoClient

    @InjectMocks
    lateinit var service: PersonalDetailsService

    @Test
    fun `calls personal details function`() {
        val crn = "X000005"

        whenever(personRepository.findByCrn(crn)).thenReturn(PersonDetailsGenerator.PERSONAL_DETAILS)

        whenever(addressRepository.findByPersonId(any())).thenReturn(
            listOf(PersonDetailsGenerator.PERSON_ADDRESS_1, PersonDetailsGenerator.PERSON_ADDRESS_2, PersonDetailsGenerator.NULL_ADDRESS)
        )
        whenever(documentRepository.findByPersonId(any())).thenReturn(
            listOf(PersonDetailsGenerator.DOCUMENT_1, PersonDetailsGenerator.DOCUMENT_2)
        )

        val res = service.getPersonalDetails(crn)
        assertThat(
            res.preferredName, equalTo(PersonDetailsGenerator.PERSONAL_DETAILS.preferredName)
        )
    }

    @Test
    fun `calls download document function`() {
        val crn = "X000005"
        val alfrescoId = "A001"
        val expectedResponse = ResponseEntity<StreamingResponseBody>(HttpStatus.OK)
        whenever(documentRepository.findNameByPersonCrnAndAlfrescoId(crn, alfrescoId)).thenReturn(PersonDetailsGenerator.DOCUMENT_1.name)
        whenever(alfrescoClient.streamDocument(alfrescoId, PersonDetailsGenerator.DOCUMENT_1.name)).thenReturn(expectedResponse)
        val res = service.downloadDocument(crn, alfrescoId)
        Assertions.assertEquals(expectedResponse.statusCode, res.statusCode)

    }
}