package uk.gov.justice.digital.hmpps.alfresco.model

data class DocumentResponse(
    val numberOfDocuments: Int,
    val maxResults: Int,
    val startIndex: Int,
    val documents: List<Document>
)

data class Document(
    val id: String,
)
