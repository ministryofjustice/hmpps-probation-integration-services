package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MultiValueMap
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.audit.entity.User
import uk.gov.justice.digital.hmpps.integrations.delius.audit.entity.UserRepository
import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtReport
import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtReportRepository
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.PsrReference
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProviderRepository
import uk.gov.justice.digital.hmpps.integrations.newtech.NewTechEncoder
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.security.ServiceContext
import java.time.ZonedDateTime
import java.util.UUID

@Service
class DocumentService(
    auditedInteractionService: AuditedInteractionService,
    private val userRepository: UserRepository,
    private val providerRepository: ProviderRepository,
    private val documentRepository: DocumentRepository,
    private val courtReportRepository: CourtReportRepository,
    private val alfrescoClient: AlfrescoClient,
    private val newTechEncoder: NewTechEncoder
) : AuditableService(auditedInteractionService) {
    fun AdditionalInformation.reportId() = this["reportId"] as String

    @Transactional
    fun updateCourtReportDocument(hmppsEvent: HmppsDomainEvent, file: ByteArray) =
        audit(BusinessInteractionCode.UPLOAD_DOCUMENT) {
            val reportId = hmppsEvent.additionalInformation.reportId()
            val document = documentRepository.findByExternalReference(reportId)
                ?: throw NotFoundException("Document", "externalReference", reportId)
            it["documentId"] = document.id
            it["alfrescoDocumentId"] = document.alfrescoId
            it["entityId"] = document.courtReportId
            it["tableName"] = "COURT_REPORT"

            val courtReport = courtReportRepository.findById(document.courtReportId).orElseThrow {
                NotFoundException("CourtReport", "id", document.courtReportId)
            }

            if (courtReport.person.crn != hmppsEvent.personReference.findCrn()) {
                throw ConflictException("Court report ${courtReport.id} not for ${hmppsEvent.personReference.findCrn()}")
            }

            alfrescoClient.releaseDocument(document.alfrescoId)
            alfrescoClient.updateDocument(
                document.alfrescoId,
                populateBodyValues(hmppsEvent, document, file)
            )

            document.lastSaved = ZonedDateTime.now()
            document.lastUpdatedUserId = ServiceContext.servicePrincipal()!!.userId
        }

    @Transactional
    fun createNewCourtReportDocument(
        reportType: String,
        courtReport: CourtReport,
        psrRef: PsrReference,
        pdf: ByteArray,
        username: String
    ) = audit(BusinessInteractionCode.UPLOAD_DOCUMENT) { audit ->
        audit["entityId"] = courtReport.id
        audit["tableName"] = "COURT_REPORT"

        val filename = "$reportType-${psrRef.id}.pdf"
        val user = userRepository.findByUsername(username)
        // Can be improved later to look for provider of actual user
        val provider = providerRepository.findByCode("N00")
        val aur = alfrescoClient.uploadNewDocument(
            populateBodyValues(
                courtReport.person.crn,
                courtReport.id,
                filename,
                pdf,
                user.name(),
                true
            )
        )

        val document = documentRepository.save(
            Document(
                personId = courtReport.person.id,
                courtReportId = courtReport.id,
                templateName = reportType,
                name = filename,
                externalReference = psrRef.urn,
                alfrescoId = aur.id,
                createdDateTime = ZonedDateTime.now(),
                createdByUserId = user.id,
                lastSaved = ZonedDateTime.now(),
                lastUpdatedUserId = user.id,
                createdProviderId = provider.id,
                lastUpdatedProviderId = provider.id
            )
        )

        audit["documentId"] = document.id
        audit["alfrescoDocumentId"] = document.alfrescoId
    }

    fun getPreSentenceReportUrl(uuid: UUID, psrBaseUrl: String): String {
        val templateName = documentRepository.findByExternalReference(uuid.toString())?.templateName
        return "$psrBaseUrl/$templateName/$uuid"
    }

    fun getLegacyNewTechReportUrl(uuid: UUID, newTechBaseUrl: String, username: String): String {
        val user = userRepository.findByUsername(username)
        val doc = documentRepository.findByExternalReference(uuid.toString())
            ?: throw NotFoundException("Document", "externalReference", uuid)
        return UriComponentsBuilder
            .fromUriString("$newTechBaseUrl/report/${doc.templateName}")
            .queryParam("t", newTechEncoder.encode(System.currentTimeMillis().toString()))
            .queryParam("user", newTechEncoder.encode(user.username))
            .queryParam("onBehalfOfUser", newTechEncoder.encode(user.name()))
            .queryParam("documentId", newTechEncoder.encode(uuid.toString()))
            .build().toUriString()
    }

    private fun populateBodyValues(
        hmppsEvent: HmppsDomainEvent,
        document: Document,
        file: ByteArray
    ): MultiValueMap<String, HttpEntity<*>> {
        val crn = hmppsEvent.personReference.findCrn()!!
        return populateBodyValues(crn, document.courtReportId, document.name, file, "Service,Pre-Sentence")
    }

    private fun populateBodyValues(
        crn: String,
        courtReportId: Long,
        filename: String,
        file: ByteArray,
        authorName: String,
        locked: Boolean = false
    ): MultiValueMap<String, HttpEntity<*>> {
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("CRN", crn, MediaType.TEXT_PLAIN)
        bodyBuilder.part("entityId", courtReportId.toString(), MediaType.TEXT_PLAIN)
        bodyBuilder.part("author", authorName, MediaType.TEXT_PLAIN)
        bodyBuilder.part("filedata", file, MediaType.APPLICATION_OCTET_STREAM).filename(filename)
        bodyBuilder.part("docType", "DOCUMENT", MediaType.TEXT_PLAIN)
        bodyBuilder.part("entityType", "COURTREPORT", MediaType.TEXT_PLAIN)
        if (locked) {
            bodyBuilder.part("locked", "true", MediaType.TEXT_PLAIN)
        }
        return bodyBuilder.build()
    }

    fun User.name() = "$surname,$forename"
}
