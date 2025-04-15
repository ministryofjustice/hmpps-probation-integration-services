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
import uk.gov.justice.digital.hmpps.api.model.personalDetails.DocumentSearch
import uk.gov.justice.digital.hmpps.api.model.personalDetails.PersonDocuments
import uk.gov.justice.digital.hmpps.service.DocumentsService
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
internal class DocumentsControllerTest {

    @Mock
    lateinit var documentsService: DocumentsService

    @InjectMocks
    lateinit var controller: DocumentsController

    private lateinit var personSummary: PersonSummary
    private lateinit var expectedResponse: PersonDocuments
    private lateinit var crn: String

    @BeforeEach
    fun setup() {
        personSummary = PersonSummary(
            Name(forename = "TestName", middleName = null, surname = "TestSurname"), pnc = "Test PNC",
            crn = "CRN", noms = "NOMS",
            dateOfBirth = LocalDate.now(), offenderId = 1L
        )
        crn = "X000005"
        expectedResponse = PersonDocuments(
            personSummary = personSummary,
            totalPages = 1,
            totalElements = 1,
            documents = listOfNotNull(
                DocumentDetails(
                    alfrescoId = "alfrescoId",
                    offenderId = 123456L,
                    name = "TestName",
                    level = "Doc Level",
                    type = "TestDescription",
                    createdAt = LocalDateTime.now(),
                    lastUpdatedAt = LocalDateTime.now(),
                    author = "TestUser",
                    status = "Sensitive",
                    workInProgress = false
                )
            ),
            sortedBy = "name.desc"
        )
    }

    @Test
    fun `calls get all documents`() {
        whenever(
            documentsService.getDocuments(
                crn,
                PageRequest.of(1, 1, Sort.by(Sort.Direction.valueOf("DESC"), "name")),
                "name.desc"
            )
        ).thenReturn(expectedResponse)
        val res = controller.getPersonDocuments(crn, 1, 1, "name.desc")
        assertThat(res, equalTo(expectedResponse))
    }

    @Test
    fun `calls search documents by name and file name`() {

        val docSearch = DocumentSearch(
            name = "contact",
            dateFrom = LocalDateTime.now().minusDays(3),
            dateTo = LocalDateTime.now().minusDays(1)
        )
        whenever(
            documentsService.search(
                docSearch,
                crn,
                PageRequest.of(1, 1, Sort.by(Sort.Direction.valueOf("DESC"), "name")),
                "name.desc"
            )
        ).thenReturn(expectedResponse)
        val res = controller.searchPersonDocuments(crn, docSearch, 1, 1, "name.desc")
        assertThat(res, equalTo(expectedResponse))
    }
}