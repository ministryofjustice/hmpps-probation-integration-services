package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.api.model.DocumentFilter
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Document
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.service.DocumentService
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class DocumentServiceTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var documentRepository: DocumentRepository

    @Mock
    lateinit var alfrescoClient: AlfrescoClient

    @InjectMocks
    lateinit var documentService: DocumentService

    lateinit var documents: List<DocumentTest>

    @BeforeEach
    fun setUp() {
        documents = listOf(
            generateDocument("PREVIOUS_CONVICTION", "OFFENDER"),
            generateDocument("CPS_PACK", "EVENT", eventId = 4L),
            generateDocument("CPS_PACK", "EVENT", eventId = 5L),
            generateDocument("TEST", "CASE_ALLOCATION"),
            generateDocument("OFFENDER_DOCUMENT", "OFFENDER"),
            generateDocument(
                "OFFENDER_DOCUMENT",
                "COURT_REPORT",
                subTypeCode = "PSR",
                dateRequested = Instant.now(),
                dateRequired = Instant.now(),
                completedDate = Instant.now()
            )
        )

        whenever(personRepository.findByCrn(any())).thenReturn(PersonGenerator.CURRENTLY_MANAGED)
    }

    @Test
    fun ` InvalidRequestException when subtype is supplied with no type`() {

        val filter = DocumentFilter(subtype = "AAA")
        val exception = assertThrows<InvalidRequestException> {
            documentService.getDocumentsGroupedFor("C123456", filter)
        }
        assertThat(
            exception.message,
            equalTo("subtype of AAA was supplied but no type. subtype can only be supplied when a valid type is supplied")
        )
    }

    @Test
    fun ` InvalidRequestException when invalid type is supplied`() {

        val filter = DocumentFilter(type = "WRONG", subtype = "AAA")
        val exception = assertThrows<InvalidRequestException> {
            documentService.getDocumentsGroupedFor("C123456", filter)
        }
        assertThat(exception.message, equalTo("type of WRONG was not valid"))
    }

    @Test
    fun ` InvalidRequestException when valid type but invalid subtype`() {

        val filter = DocumentFilter(type = "OFFENDER_DOCUMENT", subtype = "AAA")
        val exception = assertThrows<InvalidRequestException> {
            documentService.getDocumentsGroupedFor("C123456", filter)
        }
        assertThat(exception.message, equalTo("subtype of AAA was not valid"))
    }

    @Test
    fun ` InvalidRequestException when valid type, valid subtype but subtype not valid for type`() {

        val filter = DocumentFilter(type = "OFFENDER_DOCUMENT", subtype = "PSR")
        val exception = assertThrows<InvalidRequestException> {
            documentService.getDocumentsGroupedFor("C123456", filter)
        }
        assertThat(exception.message, equalTo("subtype of PSR was not valid for type OFFENDER_DOCUMENT"))
    }

    @Test
    fun ` valid type and subtype filter returns results `() {

        whenever(documentRepository.getPersonAndEventDocuments(any())).thenReturn(documents)

        val filter = DocumentFilter(type = "COURT_REPORT_DOCUMENT", subtype = "PSR")
        val docs = documentService.getDocumentsGroupedFor("C123456", filter)

        assertThat(docs.documents.size, equalTo(1))
    }

    @Test
    fun `  no filter returns all documents `() {

        whenever(documentRepository.getPersonAndEventDocuments(any())).thenReturn(documents)

        val filter = DocumentFilter()
        val docs = documentService.getDocumentsGroupedFor("C123456", filter)

        assertThat(docs.documents.size, equalTo(4))
        assertThat(docs.convictions.size, equalTo(2))
    }

    fun generateDocument(
        type: String,
        tableName: String,
        subTypeCode: String? = null,
        dateRequested: Instant? = null,
        dateRequired: Instant? = null,
        completedDate: Instant? = null,
        eventId: Long? = null
    ) = DocumentTest(
        alfrescoId = "usdiuhasduihd9sd9a8d09u",
        name = "Doc1",
        type = type,
        tableName = tableName,
        lastModifiedAt = Instant.now(),
        createdAt = Instant.now(),
        primaryKeyId = 2L,
        author = "Test Author",
        description = "A description",
        eventId = eventId,
        subTypeCode = subTypeCode,
        subTypeDescription = "subtype description",
        dateRequested = dateRequested,
        dateRequired = dateRequired,
        completedDate = completedDate
    )
}

data class DocumentTest(
    override val alfrescoId: String,
    override val name: String,
    override val type: String,
    override val tableName: String,
    override val lastModifiedAt: Instant?,
    override val createdAt: Instant?,
    override val primaryKeyId: Long?,
    override val author: String?,
    override val description: String?,
    override val eventId: Long?,
    override val subTypeCode: String?,
    override val subTypeDescription: String?,
    override val dateRequested: Instant?,
    override val dateRequired: Instant?,
    override val completedDate: Instant?
) : Document

