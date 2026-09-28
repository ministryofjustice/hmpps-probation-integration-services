package uk.gov.justice.digital.hmpps.model

data class DocumentUploadResponse(
    val documentId: Long,
    val filename: String,
    val alfrescoId: String
)
