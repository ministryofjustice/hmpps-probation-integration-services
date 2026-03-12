package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.MISSING_COURT_APPEARANCE_EVENT
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.MISSING_MAIN_OFFENCE_EVENT
import uk.gov.justice.digital.hmpps.entity.DocumentEntity
import java.time.ZonedDateTime
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
        softDeleted = false,
        alfrescoId = UUID.randomUUID().toString(),
        name = "Default Document.docx",
        status = "Y",
        workInProgress = "N",
        lastSaved = ZonedDateTime.now(),
        createdDatetime = ZonedDateTime.now(),
        lastUpdatedUserId = UserGenerator.DEFAULT_PROBATION_USER.id
    )

    val MISSING_MAIN_OFFENCE_DOCUMENT = DocumentEntity(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        primaryKeyId = MISSING_MAIN_OFFENCE_EVENT.eventId,
        tableName = "EVENT",
        externalReference = DocumentEntity.cossoBreachNoticeUrn(MISSING_MAIN_OFFENCE_DOCUMENT_UUID),
        softDeleted = false,
        alfrescoId = UUID.randomUUID().toString(),
        name = "Missing Main Offence Document.docx",
        status = "Y",
        workInProgress = "N",
        lastSaved = ZonedDateTime.now(),
        createdDatetime = ZonedDateTime.now(),
        lastUpdatedUserId = UserGenerator.DEFAULT_PROBATION_USER.id
    )

    val MISSING_COURT_APPEARANCE_DOCUMENT = DocumentEntity(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        primaryKeyId = MISSING_COURT_APPEARANCE_EVENT.eventId,
        tableName = "EVENT",
        externalReference = DocumentEntity.cossoBreachNoticeUrn(MISSING_COURT_APPEARANCE_DOCUMENT_UUID),
        softDeleted = false,
        alfrescoId = UUID.randomUUID().toString(),
        name = "Missing COURT APPEARANCE Document.docx",
        status = "Y",
        workInProgress = "N",
        lastSaved = ZonedDateTime.now(),
        createdDatetime = ZonedDateTime.now(),
        lastUpdatedUserId = UserGenerator.DEFAULT_PROBATION_USER.id
    )

    val MISSING_DISPOSAL_DOCUMENT = DocumentEntity(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        primaryKeyId = EventGenerator.MISSING_DISPOSAL_EVENT.eventId,
        tableName = "EVENT",
        externalReference = DocumentEntity.cossoBreachNoticeUrn(MISSING_DISPOSAL_DOCUMENT_UUID),
        softDeleted = false,
        alfrescoId = UUID.randomUUID().toString(),
        name = "Missing Disposal Document.docx",
        status = "Y",
        workInProgress = "N",
        lastSaved = ZonedDateTime.now(),
        createdDatetime = ZonedDateTime.now(),
        lastUpdatedUserId = UserGenerator.DEFAULT_PROBATION_USER.id
    )

    val DEFAULT_COSSO_CREATED = DocumentEntity(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        primaryKeyId = EventGenerator.DEFAULT_EVENT.eventId,
        tableName = "EVENT",
        externalReference = DocumentEntity.cossoBreachNoticeUrn(UUID.fromString("00000000-0000-0000-0000-000000000001")),
        softDeleted = false,
        alfrescoId = "00000000-0000-0000-0000-000000000001",
        name = "test.pdf",
        status = "Y",
        workInProgress = "N",
        lastUpdatedUserId = UserGenerator.DEFAULT_PROBATION_USER.id,
        lastSaved = ZonedDateTime.now(),
        createdDatetime = ZonedDateTime.now()
    )

    val DEFAULT_COSSO_DELETED = DocumentEntity(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        primaryKeyId = EventGenerator.DEFAULT_EVENT.eventId,
        tableName = "EVENT",
        externalReference = DocumentEntity.cossoBreachNoticeUrn(UUID.fromString("00000000-0000-0000-0000-000000000003")),
        softDeleted = false,
        alfrescoId = "00000000-0000-0000-0000-000000000003",
        name = "test.pdf",
        status = "Y",
        workInProgress = "N",
        lastUpdatedUserId = UserGenerator.DEFAULT_PROBATION_USER.id,
        lastSaved = ZonedDateTime.now(),
        createdDatetime = ZonedDateTime.now()
    )
}