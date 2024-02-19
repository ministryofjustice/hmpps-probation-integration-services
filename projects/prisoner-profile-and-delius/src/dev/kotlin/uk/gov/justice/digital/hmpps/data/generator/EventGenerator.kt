package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.documents.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate
import java.time.ZonedDateTime

object EventGenerator {
    val EVENT = Event(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT,
        referralDate = LocalDate.now(),
        mainOffence = MainOffence(
            id = IdGenerator.getAndIncrement(),
            offence = Offence(
                id = IdGenerator.getAndIncrement(),
                subCategoryDescription = "Burglary"
            ),
            event = null,
            softDeleted = false
        ), disposal = null, active = true, softDeleted = false
    ).also { it.mainOffence.set("event", it) }

    val UNSENTENCED_EVENT = Event(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT,
        referralDate = LocalDate.now(),
        mainOffence = MainOffence(
            id = IdGenerator.getAndIncrement(),
            offence = Offence(
                id = IdGenerator.getAndIncrement(),
                subCategoryDescription = "Daylight Robbery"
            ),
            event = null,
            softDeleted = false
        ), disposal = null, active = true, softDeleted = false
    ).also { it.mainOffence.set("event", it) }

    val DISPOSAL = Disposal(
        id = IdGenerator.getAndIncrement(),
        event = EVENT,
        type = DisposalType(
            id = IdGenerator.getAndIncrement(),
            description = "Sentenced"
        ),
        length = 6,
        lengthUnits = ReferenceData("M", "Months", IdGenerator.getAndIncrement()),
        custody = null,
        active = true,
        softDeleted = false
    )

    val INSTITUTION = Institution(
        id = IdGenerator.getAndIncrement(),
        name = "test institution",
        establishment = true
    )

    val CUSTODY = Custody(
        id = IdGenerator.getAndIncrement(),
        disposal = DISPOSAL,
        institution = INSTITUTION
    )

    val COURT = Court(courtId = IdGenerator.getAndIncrement(), courtName = "test court")

    val COURT_APPEARANCE = CourtAppearance(
        id = IdGenerator.getAndIncrement(),
        date = ZonedDateTime.now(),
        courtId = COURT.courtId,
        event = EVENT,
        outcome = null,
        softDeleted = false
    ).also { EVENT.set(Event::courtAppearances, listOf(this)) }

    val UNSENTENCED_COURT_APPEARANCE = CourtAppearance(
        id = IdGenerator.getAndIncrement(),
        date = ZonedDateTime.now(),
        courtId = COURT.courtId,
        event = UNSENTENCED_EVENT,
        outcome = ReferenceData("TEST", "Community Order", IdGenerator.getAndIncrement()),
        softDeleted = false
    ).also { UNSENTENCED_EVENT.set(Event::courtAppearances, listOf(this)) }

    val COURT_REPORT_TYPE =
        CourtReportType(courtReportTypeId = IdGenerator.getAndIncrement(), description = "court report type")
    val COURT_REPORT = CourtReport(
        courtReportId = IdGenerator.getAndIncrement(),
        courtReportTypeId = COURT_REPORT_TYPE.courtReportTypeId,
        courtAppearanceId = COURT_APPEARANCE.id,
        dateRequested = LocalDate.of(2000, 1, 1)
    )

    val INSTITUTIONAL_REPORT_TYPE = ReferenceData("IR", "institutional report type", IdGenerator.getAndIncrement())
    val INSTITUTIONAL_REPORT = InstitutionalReport(
        institutionalReportId = IdGenerator.getAndIncrement(),
        institutionId = INSTITUTION.id,
        institutionReportTypeId = INSTITUTIONAL_REPORT_TYPE.id,
        custodyId = CUSTODY.id,
        establishment = true,
        dateRequested = LocalDate.of(2000, 1, 2)
    )

    val CONTACT_TYPE = ContactType(contactTypeId = IdGenerator.getAndIncrement(), description = "contact type")
    val CONTACT = Contact(
        contactId = IdGenerator.getAndIncrement(),
        contactTypeId = CONTACT_TYPE.contactTypeId,
        eventId = EVENT.id,
        contactDate = LocalDate.of(2000, 1, 3)
    )

    val NSI_TYPE = NsiType(nsiTypeId = IdGenerator.getAndIncrement(), description = "nsi type")
    val NSI = Nsi(
        nsiId = IdGenerator.getAndIncrement(),
        nsiTypeId = NSI_TYPE.nsiTypeId,
        eventId = EVENT.id,
        referralDate = LocalDate.of(2000, 1, 4)
    )
}
