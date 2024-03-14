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
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.PersonalContactRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class PersonalDetailsServiceTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var addressRepository: PersonAddressRepository

    @Mock
    lateinit var documentRepository: DocumentRepository

    @Mock
    lateinit var provisionRepository: ProvisionRepository

    @Mock
    lateinit var disabilityRepository: DisabilityRepository

    @Mock
    lateinit var personalCircumstanceRepository: PersonCircumstanceRepository

    @Mock
    lateinit var aliasRepository: AliasRepository

    @Mock
    lateinit var personalContactRepository: PersonalContactRepository

    @Mock
    lateinit var alfrescoClient: AlfrescoClient

    @InjectMocks
    lateinit var service: PersonalDetailsService

    @Test
    fun `calls personal details function`() {
        val crn = "X000005"

        whenever(personRepository.findByCrn(crn)).thenReturn(PersonDetailsGenerator.PERSONAL_DETAILS)
        whenever(provisionRepository.findByPersonId(any())).thenReturn(emptyList())
        whenever(disabilityRepository.findByPersonId(any())).thenReturn(emptyList())
        whenever(personalCircumstanceRepository.findByPersonId(any())).thenReturn(PersonGenerator.PERSONAL_CIRCUMSTANCES)
        whenever(aliasRepository.findByPersonId(any())).thenReturn(emptyList())
        whenever(personalContactRepository.findByPersonId(any())).thenReturn(emptyList())

        whenever(addressRepository.findByPersonId(any())).thenReturn(
            listOf(
                PersonDetailsGenerator.PERSON_ADDRESS_1,
                PersonDetailsGenerator.PERSON_ADDRESS_2,
                PersonDetailsGenerator.NULL_ADDRESS
            )
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
        whenever(
            documentRepository.findNameByPersonCrnAndAlfrescoId(
                crn,
                alfrescoId
            )
        ).thenReturn(PersonDetailsGenerator.DOCUMENT_1.name)
        whenever(alfrescoClient.streamDocument(alfrescoId, PersonDetailsGenerator.DOCUMENT_1.name)).thenReturn(
            expectedResponse
        )
        val res = service.downloadDocument(crn, alfrescoId)
        Assertions.assertEquals(expectedResponse.statusCode, res.statusCode)
    }

    @Test
    fun `calls get contact function`() {
        val crn = "X000005"
        val id = 1234L
        whenever(personalContactRepository.findById(crn, id)).thenReturn(PersonDetailsGenerator.PERSONAL_CONTACT_1)
        val res = service.getPersonContact(crn, id)
        assertThat(res, equalTo(PersonDetailsGenerator.PERSONAL_CONTACT_1.toContact()))
    }

    @Test
    fun `calls get summary function`() {
        val crn = "X000005"

        val expected = Summary(
            forename = "TestName",
            surname = "TestSurname", crn = "CRN", pnc = "PNC", dateOfBirth = LocalDate.now().minusYears(50)
        )
        whenever(personRepository.findSummary(crn)).thenReturn(expected)
        val res = service.getPersonSummary(crn)
        assertThat(res, equalTo(expected.toPersonSummary()))
    }

    data class Summary(
        override val forename: String,
        override val secondName: String? = null,
        override val thirdName: String? = null,
        override val surname: String,
        override val crn: String,
        override val pnc: String?,
        override val dateOfBirth: LocalDate
    ) : PersonSummaryEntity {

    }
}