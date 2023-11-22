package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.entity.Court
import uk.gov.justice.digital.hmpps.data.entity.CourtReport
import uk.gov.justice.digital.hmpps.data.entity.CourtReportType
import uk.gov.justice.digital.hmpps.data.entity.Institution
import uk.gov.justice.digital.hmpps.data.entity.InstitutionalReport
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DocumentEntity
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

object DocumentEntityGenerator {
    val COURT = Court(courtId = IdGenerator.getAndIncrement(), courtName = "test court")
    val COURT_REPORT_TYPE =
        CourtReportType(courtReportTypeId = IdGenerator.getAndIncrement(), description = "court report type")
    val COURT_REPORT = CourtReport(
        courtReportId = IdGenerator.getAndIncrement(),
        courtReportTypeId = COURT_REPORT_TYPE.courtReportTypeId,
        courtAppearanceId = 1,
        dateRequested = LocalDate.of(2000, 1, 1)
    )

    val INSTITUTIONAL_REPORT_TYPE = ReferenceData("IR", "institutional report type", IdGenerator.getAndIncrement())
    val INSTITUTIONAL_REPORT = InstitutionalReport(
        institutionalReportId = IdGenerator.getAndIncrement(),
        institutionId = 1,
        institutionReportTypeId = INSTITUTIONAL_REPORT_TYPE.id,
        custodyId = 1,
        establishment = true,
        dateRequested = LocalDate.of(2000, 1, 2)
    )

    val R_INSTITUTION = Institution(IdGenerator.getAndIncrement(), "test", false)

    fun generateDocument(personId: Long, primaryKeyId: Long, type: String, tableName: String) =
        DocumentEntity(
            IdGenerator.getAndIncrement(),
            personId,
            "alfrescoId",
            primaryKeyId,
            "filename.txt",
            type,
            tableName,
            ZonedDateTime.now()
        )
}
