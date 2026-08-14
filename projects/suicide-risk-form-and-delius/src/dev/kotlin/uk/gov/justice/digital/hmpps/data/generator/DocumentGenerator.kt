package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Document
import uk.gov.justice.digital.hmpps.integrations.delius.Person
import java.util.*

object DocumentGenerator {
    val SUICIDE_RISK_FORM_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    val DEFAULT_SUICIDE_RISK_FORM = generateDocument(suicideRiskFormId = SUICIDE_RISK_FORM_ID)

    val EVENT_CONTACT_SUICIDE_RISK_FORM_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000005")
    val EVENT_CONTACT_SUICIDE_RISK_FORM = generateDocument(
        suicideRiskFormId = EVENT_CONTACT_SUICIDE_RISK_FORM_ID,
        primaryKeyId = ContactGenerator.EVENT_CONTACT.id,
        tableName = "CONTACT"
    )

    val EVENT_LEVEL_SUICIDE_RISK_FORM_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000003")
    val EVENT_LEVEL_SUICIDE_RISK_FORM = generateDocument(
        suicideRiskFormId = EVENT_LEVEL_SUICIDE_RISK_FORM_ID,
        primaryKeyId = EventGenerator.DEFAULT_EVENT.id,
        tableName = "EVENT"
    )

    val NSI_SUICIDE_RISK_FORM_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000006")
    val NSI_SUICIDE_RISK_FORM = generateDocument(
        suicideRiskFormId = NSI_SUICIDE_RISK_FORM_ID,
        primaryKeyId = NsiGenerator.DEFAULT_NSI.nsiId,
        tableName = "NSI"
    )

    val TERMINATED_EVENT_SUICIDE_RISK_FORM_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000004")
    val TERMINATED_EVENT_SUICIDE_RISK_FORM = generateDocument(
        suicideRiskFormId = TERMINATED_EVENT_SUICIDE_RISK_FORM_ID,
        primaryKeyId = EventGenerator.TERMINATED_EVENT.id,
        tableName = "EVENT"
    )

    val DELETED_SUICIDE_RISK_FORM_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")
    val DELETED_SUICIDE_RISK_FORM =
        generateDocument(suicideRiskFormId = DELETED_SUICIDE_RISK_FORM_ID, primaryKeyId = 2L)

    fun generateDocument(
        suicideRiskFormId: UUID,
        id: Long = IdGenerator.getAndIncrement(),
        person: Person = PersonGenerator.DEFAULT_PERSON,
        alfrescoId: String = UUID.randomUUID().toString(),
        name: String = "srf.doc",
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
        externalReference = Document.suicideRiskFormUrn(suicideRiskFormId),
        workInProgress = workInProgress,
        status = status,
        softDeleted = softDeleted
    )
}