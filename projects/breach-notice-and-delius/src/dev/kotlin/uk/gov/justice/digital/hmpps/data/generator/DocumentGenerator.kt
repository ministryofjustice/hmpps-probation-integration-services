package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator.OTHER_APPOINTMENTS
import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator.PSS_APPOINTMENT
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.UNSENTENCED_EVENT
import uk.gov.justice.digital.hmpps.integrations.delius.Document
import java.util.*

object DocumentGenerator {
    val BREACH_NOTICE_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    val DEFAULT_BREACH_NOTICE = generateDocument(BREACH_NOTICE_ID, OTHER_APPOINTMENTS.first().id, "CONTACT")

    val UNSENTENCED_BREACH_NOTICE_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")
    val UNSENTENCED_BREACH_NOTICE = generateDocument(UNSENTENCED_BREACH_NOTICE_ID, UNSENTENCED_EVENT.id, "EVENT")

    val DELETED_BREACH_NOTICE_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000003")
    val DELETED_BREACH_NOTICE = generateDocument(DELETED_BREACH_NOTICE_ID, OTHER_APPOINTMENTS.first().id, "CONTACT")

    val PSS_BREACH_NOTICED_ID: UUID = UUID.fromString("00000000-0000-0000-0000-00000000004")
    val PSS_BREACH_NOTICE = generateDocument(PSS_BREACH_NOTICED_ID, PSS_APPOINTMENT.id, "CONTACT")

    fun generateDocument(
        breachNoticeUuid: UUID,
        primaryKeyId: Long,
        tableName: String,
        softDeleted: Boolean = false,
        alfrescoId: UUID = UUID.randomUUID(),
        id: Long = IdGenerator.getAndIncrement()
    ) = Document(
        person = PersonGenerator.DEFAULT_PERSON,
        alfrescoId = alfrescoId.toString(),
        name = "name.doc",
        primaryKeyId = primaryKeyId,
        tableName = tableName,
        externalReference = Document.breachNoticeUrn(breachNoticeUuid),
        workInProgress = "N",
        status = "N",
        softDeleted = softDeleted,
        id = id,
    )
}