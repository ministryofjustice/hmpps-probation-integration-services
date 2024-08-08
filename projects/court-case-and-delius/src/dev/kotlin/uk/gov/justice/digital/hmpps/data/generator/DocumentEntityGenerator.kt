package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.entity.InstitutionalReport
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DocumentEntity
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

object DocumentEntityGenerator {
    val INSTITUTIONAL_REPORT_TYPE = ReferenceData("IR", "institutional report type", IdGenerator.getAndIncrement())
    val INSTITUTIONAL_REPORT = InstitutionalReport(
        institutionalReportId = IdGenerator.getAndIncrement(),
        institutionId = 1,
        institutionReportTypeId = INSTITUTIONAL_REPORT_TYPE.id,
        custodyId = 1,
        establishment = true,
        dateRequested = LocalDate.of(2000, 1, 2),
        dateRequired = LocalDate.of(2000, 1, 2),
        dateCompleted = LocalDateTime.of(2000, 1, 2, 0, 0)
    )

    fun generateDocument(personId: Long, primaryKeyId: Long?, type: String, tableName: String?) =
        DocumentEntity(
            IdGenerator.getAndIncrement(),
            personId,
            "alfrescoId",
            primaryKeyId,
            "filename.txt",
            type,
            tableName,
            ZonedDateTime.now(),
            dateProduced = LocalDateTime.of(1971, 3, 6, 0, 0, 0),
            lastUpdated = ZonedDateTime.now()
        )
}
