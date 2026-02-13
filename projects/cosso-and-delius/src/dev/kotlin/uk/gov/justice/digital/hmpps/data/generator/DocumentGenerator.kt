package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.MISSING_COURT_APPEARANCE_EVENT
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.MISSING_MAIN_OFFENCE_EVENT
import uk.gov.justice.digital.hmpps.entity.DocumentEntity
import java.util.UUID

object DocumentGenerator {
    val DEFAULT_DOCUMENT_UUID = UUID.randomUUID()
    val MISSING_MAIN_OFFENCE_DOCUMENT_UUID = UUID.randomUUID()
    val MISSING_COURT_APPEARANCE_DOCUMENT_UUID = UUID.randomUUID()
    val MISSING_DISPOSAL_DOCUMENT_UUID = UUID.randomUUID()
    val DEFAULT_DOCUMENT = DocumentEntity(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        primaryKeyId = EventGenerator.DEFAULT_EVENT.eventId,
        tableName = "EVENT",
        externalReference = DocumentEntity.cossoBreachNoticeUrn(DEFAULT_DOCUMENT_UUID),
        softDeleted = false
    )

    val MISSING_MAIN_OFFENCE_DOCUMENT = DocumentEntity(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        primaryKeyId = MISSING_MAIN_OFFENCE_EVENT.eventId,
        tableName = "EVENT",
        externalReference = DocumentEntity.cossoBreachNoticeUrn(MISSING_MAIN_OFFENCE_DOCUMENT_UUID),
        softDeleted = false
    )

    val MISSING_COURT_APPEARANCE_DOCUMENT = DocumentEntity(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        primaryKeyId = MISSING_COURT_APPEARANCE_EVENT.eventId,
        tableName = "EVENT",
        externalReference = DocumentEntity.cossoBreachNoticeUrn(MISSING_COURT_APPEARANCE_DOCUMENT_UUID),
        softDeleted = false
    )

    val MISSING_DISPOSAL_DOCUMENT = DocumentEntity(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        primaryKeyId = EventGenerator.MISSING_DISPOSAL_EVENT.eventId,
        tableName = "EVENT",
        externalReference = DocumentEntity.cossoBreachNoticeUrn(MISSING_DISPOSAL_DOCUMENT_UUID),
        softDeleted = false
    )
}