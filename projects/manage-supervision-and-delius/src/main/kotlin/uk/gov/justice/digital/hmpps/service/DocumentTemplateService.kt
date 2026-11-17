package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.contact.DocumentTemplateDownload
import uk.gov.justice.digital.hmpps.api.model.contact.DocumentTemplateMetadata
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integrations.delius.documentTemplates.DocumentTemplateRepository

@Service
class DocumentTemplateService(
    private val documentTemplateRepository: DocumentTemplateRepository
) {
    fun getDocumentTemplatesByContactTypeCode(code: String) =
        documentTemplateRepository.findAllByContactTypeCode(code).map {
            DocumentTemplateMetadata(it.id, it.name, it.dateCreated.toLocalDate())
        }

    fun downloadDocumentTemplate(typeCode: String, name: String): DocumentTemplateDownload {
        val template = documentTemplateRepository.findByContactTypeCodeAndName(typeCode, name)
            .orNotFoundBy("Document Template", name)

        return DocumentTemplateDownload(
            fileName = template.name,
            fileContent = template.template
        )
    }
}