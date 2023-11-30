package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.CourtReportGenerator
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.PsrMessageGenerator
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtReportRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class DocumentServiceTest {

    @Mock
    private lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    private lateinit var documentRepository: DocumentRepository

    @Mock
    private lateinit var courtReportRepository: CourtReportRepository

    @Mock
    private lateinit var alfrescoUploadClient: AlfrescoUploadClient

    @InjectMocks
    private lateinit var documentService: DocumentService

    private val simpleHmppsEvent = PsrMessageGenerator.PSR_MESSAGE

    @Test
    fun `when document not found exception thrown`() {
        whenever(documentRepository.findByExternalReference(simpleHmppsEvent.additionalInformation["reportId"].toString()))
            .thenReturn(null)

        assertThrows<NotFoundException> {
            documentService.updateCourtReportDocument(
                simpleHmppsEvent,
                "test".toByteArray()
            )
        }
    }

    @Test
    fun `when court report not found exception thrown`() {
        whenever(documentRepository.findByExternalReference(simpleHmppsEvent.additionalInformation["reportId"].toString())).thenReturn(
            DocumentGenerator.DEFAULT
        )

        whenever(courtReportRepository.findById(DocumentGenerator.DEFAULT.courtReportId)).thenReturn(
            Optional.ofNullable(null)
        )

        assertThrows<NotFoundException> {
            documentService.updateCourtReportDocument(
                simpleHmppsEvent,
                "test".toByteArray()
            )
        }
    }

    @Test
    fun `when court report for wrong crn conflict exception thrown`() {
        whenever(documentRepository.findByExternalReference(simpleHmppsEvent.additionalInformation["reportId"].toString())).thenReturn(
            DocumentGenerator.DEFAULT
        )

        whenever(courtReportRepository.findById(DocumentGenerator.DEFAULT.courtReportId)).thenReturn(
            Optional.of(
                CourtReportGenerator.generate(
                    Person(crn = "X111111B", id = 123L),
                    CourtReportGenerator.generateAppearance(CourtReportGenerator.generateEvent("1"))
                )
            )
        )

        assertThrows<ConflictException> {
            documentService.updateCourtReportDocument(
                simpleHmppsEvent,
                "test".toByteArray()
            )
        }
    }
}
