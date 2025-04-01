package uk.gov.justice.digital.hmpps.api.controller

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.personalDetails.DocumentDetails
import uk.gov.justice.digital.hmpps.api.model.personalDetails.PersonDocuments
import uk.gov.justice.digital.hmpps.service.DocumentsService
import java.time.Instant
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class DocumentsControllerTest {

    @Mock
    lateinit var documentsService: DocumentsService

    @InjectMocks
    lateinit var controller: DocumentsController

    private lateinit var personSummary: PersonSummary

    @BeforeEach
    fun setup() {
        personSummary = PersonSummary(
            Name(forename = "TestName", middleName = null, surname = "TestSurname"), pnc = "Test PNC",
            crn = "CRN", noms = "NOMS",
            dateOfBirth = LocalDate.now(), offenderId = 1L
        )
    }

    @Test
    fun `calls get all documents`() {
        val crn = "X000005"
        val expectedResponse = PersonDocuments(
            personSummary = personSummary,
            totalPages = 1,
            totalElements = 1,
            documents = listOfNotNull(
                DocumentDetails(
                    alfrescoId = "alfrescoId",
                    offenderId = 123456L,
                    name = "TestName",
                    docLevel = "Doc Level",
                    description = "TestDescription",
                    createdAt = Instant.now(),
                    lastUpdatedAt = Instant.now(),
                    author = "TestUser",
                    eventId = 1L,
                    sensitive = true,
                )
            ),
            sortedBy = "name.desc"
        )
        whenever(
            documentsService.getDocuments(
                crn,
                PageRequest.of(1, 1, Sort.by(Sort.Direction.valueOf("DESC"), "name")),
                "name.desc"
            )
        ).thenReturn(expectedResponse)
        val res = controller.getPersonActivity(crn, 1, 1, "name.desc")
        assertThat(res, equalTo(expectedResponse))
    }
}