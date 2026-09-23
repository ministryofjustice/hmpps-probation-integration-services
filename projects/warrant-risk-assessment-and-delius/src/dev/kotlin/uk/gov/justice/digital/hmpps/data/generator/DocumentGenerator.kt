package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Document
import uk.gov.justice.digital.hmpps.entity.Person
import java.util.*

object DocumentGenerator {
    val WRA_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    val DEFAULT_WRA_FORM = generateDocument(wraId = WRA_ID)

    val DELETED_WRA_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")
    val DELETED_WRA_FORM =
        generateDocument(wraId = DELETED_WRA_ID, primaryKeyId = 2L)


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