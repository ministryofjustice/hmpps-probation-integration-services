package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Document
import uk.gov.justice.digital.hmpps.entity.Person
import java.util.*

object DocumentGenerator {
    val WRA_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    val DEFAULT_WRA_FORM = generateDocument(
        wraId = WRA_ID,
        tableName = "EVENT",
        primaryKeyId = EventGenerator.DEFAULT_EVENT.id,
    )

    val DELETED_WRA_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")
    val DELETED_WRA_FORM =
        generateDocument(wraId = DELETED_WRA_ID, primaryKeyId = 2L)

    val SOFT_DELETED_WRA_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000003")
    val SOFT_DELETED_WRA_FORM =
        generateDocument(wraId = SOFT_DELETED_WRA_ID, primaryKeyId = 3L, softDeleted = true)

    val TERMINATED_EVENT_DOCUMENT_ID: UUID = UUID.fromString("00000000-0000-0000-0000-00000000004")
    val TERMINATED_EVENT_DOCUMENT = generateDocument(
        wraId = TERMINATED_EVENT_DOCUMENT_ID,
        tableName = "EVENT",
        primaryKeyId = EventGenerator.TERMINATED_EVENT.id
    )

    fun generateDocument(
        wraId: UUID,
        id: Long = IdGenerator.getAndIncrement(),
        person: Person = PersonGenerator.DEFAULT,
        alfrescoId: String = UUID.randomUUID().toString(),
        name: String = "wra.pdf",
        primaryKeyId: Long = 1L,
        tableName: String = "CONTACT",
        workInProgress: String = "N",
        status: String = "N",
        softDeleted: Boolean = false
    ) = Document(
        id = id,
        person = person,
        alfrescoId = alfrescoId,
        name = name,
        primaryKeyId = primaryKeyId,
        tableName = tableName,
        externalReference = Document.wraFormUrn(wraId),
        workInProgress = workInProgress,
        status = status,
        softDeleted = softDeleted
    )
}