package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator.OTHER_APPOINTMENTS
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.UNSENTENCED_EVENT
import uk.gov.justice.digital.hmpps.integrations.delius.Document
import java.util.*

object DocumentGenerator {
    val BREACH_NOTICE_ID: UUID = UUID.randomUUID()
    val DEFAULT_BREACH_NOTICE = generateDocument(BREACH_NOTICE_ID, OTHER_APPOINTMENTS.first().id, "CONTACT")

    val UNSENTENCED_BREACH_NOTICE_ID: UUID = UUID.randomUUID()
    val UNSENTENCED_BREACH_NOTICE = generateDocument(UNSENTENCED_BREACH_NOTICE_ID, UNSENTENCED_EVENT.id, "EVENT")

    fun generateDocument(
        breachNoticeUuid: UUID,
        primaryKeyId: Long,
        tableName: String,
        softDeleted: Boolean = false,
        alfrescoId: UUID = UUID.randomUUID(),
        id: Long = IdGenerator.getAndIncrement()
    ) = Document(
        PersonGenerator.DEFAULT_PERSON,
        alfrescoId.toString(),
        primaryKeyId,
        tableName,
        Document.breachNoticeUrn(breachNoticeUuid),
        softDeleted,
        id
    )
}