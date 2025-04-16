package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import uk.gov.justice.digital.hmpps.api.model.personalDetails.DocumentSearch
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_DETAILS
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentEntity
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentsRepository
import uk.gov.justice.digital.hmpps.utils.Summary
import java.time.LocalDateTime
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
internal class DocumentsServiceTest {

    @Mock
    lateinit var documentsRepository: DocumentsRepository

    @Mock
    lateinit var personRepository: PersonRepository

    @InjectMocks
    lateinit var service: DocumentsService

    private lateinit var personSummary: Summary

    @BeforeEach
    fun setup() {
        personSummary = Summary(
            id = PERSONAL_DETAILS.id,
            forename = PERSONAL_DETAILS.forename,
            secondName = PERSONAL_DETAILS.secondName,
            surname = PERSONAL_DETAILS.surname, crn = PERSONAL_DETAILS.crn, pnc = PERSONAL_DETAILS.pnc,
            noms = PERSONAL_DETAILS.noms, dateOfBirth = PERSONAL_DETAILS.dateOfBirth
        )
    }

    @Test
    fun `calls get person documents function`() {
        val offenderId = PERSONAL_DETAILS.id
        val crn = PERSONAL_DETAILS.crn
        val expectedDocuments = listOf(
            DocumentEntity(
                alfrescoId = "alfrescoId",
                offenderId = 123456L,
                name = "TestName",
                level = "Doc Level",
                type = "TestDescription",
                createdAt = LocalDateTime.now(),
                lastUpdatedAt = LocalDateTime.now(),
                author = "TestUser",
                status = "Sensitive",
            )
        )
        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)

        whenever(
            documentsRepository.findByOffenderId(
                offenderId,
                PageRequest.of(1, 1, Sort.by(Sort.Direction.valueOf("DESC"), "name"))
            )
        ).thenReturn(PageImpl(expectedDocuments))

        val res = service.getDocuments(
            crn, PageRequest.of(
                1, 1,
                Sort.by(Sort.Direction.valueOf("DESC"), "name")
            ), "name.desc"
        )

        assertThat(res.documents, equalTo(expectedDocuments.map { it.toDocumentDetails() }))
    }

    @Test
    fun `calls search documents function`() {

        val docSearch = DocumentSearch(
            name = "contact",
            dateFrom = LocalDateTime.now().minusDays(3),
            dateTo = LocalDateTime.now().minusDays(1)
        )
        val offenderId = PERSONAL_DETAILS.id
        val crn = PERSONAL_DETAILS.crn
        val expectedDocuments = listOf(
            DocumentEntity(
                alfrescoId = "alfrescoId",
                offenderId = 123456L,
                name = "TestName",
                level = "Doc Level",
                type = "TestDescription",
                createdAt = LocalDateTime.now(),
                lastUpdatedAt = LocalDateTime.now(),
                author = "TestUser",
                status = "Sensitive",
            )
        )
        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)

        whenever(
            documentsRepository
                .search(
                    offenderId, docSearch.name,
                    docSearch.dateFrom?.toLocalDate()?.atStartOfDay(),
                    docSearch.dateTo?.toLocalDate()?.atTime(LocalTime.MAX),
                    PageRequest.of(
                        1, 1,
                        Sort.by(Sort.Direction.valueOf("DESC"), "name")
                    )
                )
        )
            .thenReturn(PageImpl(expectedDocuments))

        val res = service.search(
            docSearch,
            crn, PageRequest.of(
                1, 1,
                Sort.by(Sort.Direction.valueOf("DESC"), "name")
            ), "name.desc"
        )

        assertThat(res.documents, equalTo(expectedDocuments.map { it.toDocumentDetails() }))
    }
}