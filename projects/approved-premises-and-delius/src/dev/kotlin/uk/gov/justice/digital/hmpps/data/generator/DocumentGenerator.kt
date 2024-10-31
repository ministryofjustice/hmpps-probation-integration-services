package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocumentEntity
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.ZonedDateTime
import java.util.*

object DocumentGenerator {
    val EVENT = generate(
        tableName = "EVENT",
        name = "test.doc",
        primaryKeyId = PersonGenerator.ANOTHER_EVENT.id,
        alfrescoId = "uuid1"
    )

    val PERSON = generate(
        tableName = "OFFENDER",
        name = "offender.doc",
        primaryKeyId = PersonGenerator.DEFAULT.id,
        alfrescoId = "uuid2"
    )

    val PREVIOUS_CONVICTIONS = generate("OFFENDER", "PREVIOUS_CONVICTION", primaryKeyId = PersonGenerator.DEFAULT.id)
    val CPS_PACK = generate("EVENT", "CPS_PACK", primaryKeyId = PersonGenerator.EVENT.id)
    val ADDRESSASSESSMENT = generate("ADDRESSASSESSMENT")
    val PERSONALCONTACT = generate("PERSONALCONTACT")
    val PERSONAL_CIRCUMSTANCE = generate("PERSONAL_CIRCUMSTANCE")
    val OFFENDER_CONTACT = generate("CONTACT")
    val OFFENDER_NSI = generate("NSI")

    fun generate(
        tableName: String,
        type: String = "DOCUMENT",
        name: String = "$tableName-related document",
        person: Person = PersonGenerator.DEFAULT,
        primaryKeyId: Long = 0,
        alfrescoId: String = UUID.randomUUID().toString()
    ) = DocumentEntity(
        id = IdGenerator.getAndIncrement(),
        person = person,
        alfrescoId = alfrescoId,
        primaryKeyId = primaryKeyId,
        name = name,
        type = type,
        tableName = tableName,
        createdAt = ZonedDateTime.now(),
        createdByUserId = 0,
        lastSaved = ZonedDateTime.now().minusDays(7),
        lastUpdatedUserId = 0,
        softDeleted = false
    )
}
