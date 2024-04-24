package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.entity.ApprovedPremisesReferral
import uk.gov.justice.digital.hmpps.data.entity.User
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocumentEntity
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.Person
import java.time.LocalDate
import java.time.ZonedDateTime

object DocumentGenerator {

    val AP_REFERRAL_1 = ApprovedPremisesReferral(IdGenerator.getAndIncrement(), LocalDate.now().minusDays(2), 1)
    val AP_REFERRAL_2 = ApprovedPremisesReferral(IdGenerator.getAndIncrement(), LocalDate.now().minusDays(3), 2)
    val AP_REFERRAL_3 = ApprovedPremisesReferral(IdGenerator.getAndIncrement(), LocalDate.now().minusDays(4), 3)

    val DOC_PERSON = Person(IdGenerator.getAndIncrement(), "X000010", false)

    val AP_USER_1 = User(IdGenerator.getAndIncrement(), "Dave", "Brown")
    val AP_USER_2 = User(IdGenerator.getAndIncrement(), "Steve", "Smith")
    val AP_USER_3 = User(IdGenerator.getAndIncrement(), "Brian", "Cox")

    val AP_DOCUMENT_1 = generateAPDocument(
        createdByUserId = AP_USER_1.userId,
        createdAt = ZonedDateTime.now().minusDays(3),
        lastUpdatedUserId = AP_USER_2.userId,
        lastSaved = ZonedDateTime.now().minusDays(2),
        alfrescoId = "uuidap1",
        name = "ap_doc_1"
    )

    val AP_DOCUMENT_2 = generateAPDocument(
        createdByUserId = AP_USER_3.userId,
        createdAt = ZonedDateTime.now().minusDays(2),
        lastUpdatedUserId = AP_USER_1.userId,
        lastSaved = ZonedDateTime.now().minusDays(1),
        alfrescoId = "uuidap2",
        name = "ap_doc_2"
    )

    val AP_DOCUMENT_3 = generateAPDocument(
        createdByUserId = AP_USER_2.userId,
        createdAt = ZonedDateTime.now().minusDays(1),
        lastUpdatedUserId = AP_USER_3.userId,
        lastSaved = ZonedDateTime.now().minusDays(1),
        alfrescoId = "uuidap3",
        name = "ap_doc_3"
    )

    fun generateAPDocument(
        createdByUserId: Long, createdAt: ZonedDateTime,
        lastUpdatedUserId: Long, lastSaved: ZonedDateTime, name: String, alfrescoId: String
    ) = DocumentEntity(
        id = IdGenerator.getAndIncrement(),
        alfrescoId = alfrescoId,
        createdAt = createdAt,
        createdByUserId = createdByUserId,
        lastSaved = lastSaved,
        lastUpdatedUserId = lastUpdatedUserId,
        name = name,
        person = DOC_PERSON,
        primaryKeyId = AP_REFERRAL_1.approvedPremisesReferralId,
        softDeleted = false,
        tableName = "APPROVED_PREMISES_REFERRAL",
        type = "DOCUMENT"
    )
}
