package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.CourtReportGenerator.DEFAULT_COURT_REPORT
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.entity.Document
import java.time.ZonedDateTime
import java.util.*

object DocumentGenerator {
    val DOCUMENT_UUID = UUID.randomUUID().toString()
    val DOCUMENT_INVALID_UUID = UUID.randomUUID().toString()

    val DEFAULT_DOCUMENT = Document(
        name = "Default Person's court report",
        person = DEFAULT_PERSON,
        courtReport = DEFAULT_COURT_REPORT,
        tableName = "COURT_REPORT",
        externalReference = "urn:uk:gov:hmpps:pre-sentence-service:report:$DOCUMENT_UUID",
        alfrescoId = "0123432",
        rowVersion = 0,
        softDeleted = false,
        workInProgress = "N",
        status = "Y",
        lastUpdatedUserId = IdGenerator.getAndIncrement(),
        createdDatetime = ZonedDateTime.now(),
        lastSaved = ZonedDateTime.now(),
        id = IdGenerator.getAndIncrement()
    )

    val FINAL_DOCUMENT = Document(
        name = "Final Person's court report",
        person = DEFAULT_PERSON,
        courtReport = DEFAULT_COURT_REPORT,
        tableName = "COURT_REPORT",
        externalReference = "urn:uk:gov:hmpps:pre-sentence-service:report:00000000-0000-0000-0000-000000000001",
        alfrescoId = "0123433",
        rowVersion = 0,
        softDeleted = false,
        workInProgress = "N",
        status = "Y",
        lastUpdatedUserId = IdGenerator.getAndIncrement(),
        createdDatetime = ZonedDateTime.now(),
        lastSaved = ZonedDateTime.now(),
        id = IdGenerator.getAndIncrement()
    )
}