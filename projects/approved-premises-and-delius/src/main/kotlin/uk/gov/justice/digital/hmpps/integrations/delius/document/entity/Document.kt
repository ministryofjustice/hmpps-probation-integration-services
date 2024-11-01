package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentType
import java.time.Instant

interface Document {
    val id: String
    val eventNumber: String?
    val name: String
    val type: String
    val tableName: String
    val dateSaved: Instant
    val dateCreated: Instant?
    val description: String?
}

val Document.documentType get(): DocumentType = DocumentType.of(tableName, type)
val Document.typeCode get() = documentType.name
val Document.typeDescription get() = documentType.description