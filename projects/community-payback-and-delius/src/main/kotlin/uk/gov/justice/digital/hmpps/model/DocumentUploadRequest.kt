package uk.gov.justice.digital.hmpps.model

import org.springframework.web.multipart.MultipartFile

data class DocumentUploadRequest(
    val file: MultipartFile
)

data class DocumentUploadResponse(
    val documentId: Long,
    val filename: String,
    val alfrescoId: String
)
