package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
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
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.integrations.delius.audit.entity.User
import uk.gov.justice.digital.hmpps.integrations.delius.audit.entity.UserRepository
import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtReportRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProviderRepository
import uk.gov.justice.digital.hmpps.integrations.newtech.NewTechEncoder
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class DocumentServiceTest {

    @Mock
    private lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    private lateinit var documentRepository: DocumentRepository

    @Mock
    private lateinit var courtReportRepository: CourtReportRepository

    @Mock
    private lateinit var alfrescoClient: AlfrescoClient

    @Mock
    private lateinit var providerRepository: ProviderRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var newTechEncoder: NewTechEncoder

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

    @Test
    fun `getting a pre-sentence report url`() {
        val uuid = UUID.fromString("f9b09fcf-39c0-4008-8b43-e616ddfd918c")
        val document = DocumentGenerator.DEFAULT
        val psrBaseUrl = "https://psr-service.gov.uk"
        whenever(documentRepository.findByExternalReference(uuid.toString())).thenReturn(document)

        val res = documentService.getPreSentenceReportUrl(uuid, psrBaseUrl)

        assertThat(res).isEqualTo("$psrBaseUrl/${document.templateName}/$uuid")
    }

    @Test
    fun `getting a new tech report url`() {
        val uuid = UUID.fromString("f9b09fcf-39c0-4008-8b43-e616ddfd918c")
        val document = DocumentGenerator.DEFAULT
        val user = User(24, "john.smith", "smith", "john")
        val newTechBaseUrl = "https://new-tech.co.uk"
        whenever(documentRepository.findByExternalReference(uuid.toString())).thenReturn(document)
        whenever(userRepository.findByUsername(user.username)).thenReturn(user)
        whenever(newTechEncoder.encode(ArgumentMatchers.anyString())).then { "encoded-${it.arguments[0]}" }

        val res = documentService.getLegacyNewTechReportUrl(uuid, newTechBaseUrl, user.username)

        assertThat(res)
            .startsWith("$newTechBaseUrl/report/${document.templateName}")
            .contains("documentId=encoded-$uuid")
            .contains("onBehalfOfUser=encoded-${user.surname},${user.forename}")
            .contains("user=encoded-${user.username}")
    }
}
