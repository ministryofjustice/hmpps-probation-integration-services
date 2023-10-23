package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Document
import java.time.ZonedDateTime
import java.util.UUID

object DocumentGenerator {
    val OFFENDER = generate("OFFENDER", primaryKeyId = PersonGenerator.DEFAULT.id, alfrescoId = "uuid1")
    val PREVIOUS_CONVICTIONS = generate("OFFENDER", "PREVIOUS_CONVICTION", primaryKeyId = PersonGenerator.DEFAULT.id)
    val EVENT = generate("EVENT", primaryKeyId = EventGenerator.EVENT.id)
    val CPS_PACK = generate("EVENT", "CPS_PACK", primaryKeyId = EventGenerator.EVENT.id)
    val ADDRESSASSESSMENT = generate("ADDRESSASSESSMENT")
    val PERSONALCONTACT = generate("PERSONALCONTACT")
    val PERSONAL_CIRCUMSTANCE = generate("PERSONAL_CIRCUMSTANCE")
    val COURT_REPORT = generate("COURT_REPORT", primaryKeyId = EventGenerator.COURT_REPORT.courtReportId)
    val INSTITUTIONAL_REPORT = generate("INSTITUTIONAL_REPORT", primaryKeyId = EventGenerator.INSTITUTIONAL_REPORT.institutionalReportId)
    val OFFENDER_CONTACT = generate("CONTACT")
    val EVENT_CONTACT = generate("CONTACT", primaryKeyId = EventGenerator.CONTACT.contactId)
    val OFFENDER_NSI = generate("NSI")
    val EVENT_NSI = generate("NSI", primaryKeyId = EventGenerator.NSI.nsiId)

    fun generate(tableName: String, type: String = "DOCUMENT", primaryKeyId: Long = 0, alfrescoId: String = UUID.randomUUID().toString()) = Document(
        id = IdGenerator.getAndIncrement(),
        personId = PersonGenerator.DEFAULT.id,
        alfrescoId = alfrescoId,
        primaryKeyId = primaryKeyId,
        name = "$tableName-related document",
        type = type,
        tableName = tableName,
        createdAt = ZonedDateTime.now(),
        createdByUserId = 0,
        lastUpdatedUserId = 0,
        softDeleted = false
    )
}
