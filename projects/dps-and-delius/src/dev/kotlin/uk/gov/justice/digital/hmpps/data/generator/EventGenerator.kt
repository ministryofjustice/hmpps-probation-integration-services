package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.entity.Contact
import uk.gov.justice.digital.hmpps.data.entity.ContactType
import uk.gov.justice.digital.hmpps.data.entity.Court
import uk.gov.justice.digital.hmpps.data.entity.CourtReport
import uk.gov.justice.digital.hmpps.data.entity.CourtReportType
import uk.gov.justice.digital.hmpps.data.entity.InstitutionalReport
import uk.gov.justice.digital.hmpps.data.entity.Nsi
import uk.gov.justice.digital.hmpps.data.entity.NsiType
import uk.gov.justice.digital.hmpps.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.entity.Custody
import uk.gov.justice.digital.hmpps.entity.Disposal
import uk.gov.justice.digital.hmpps.entity.DisposalType
import uk.gov.justice.digital.hmpps.entity.Event
import uk.gov.justice.digital.hmpps.entity.Institution
import uk.gov.justice.digital.hmpps.entity.MainOffence
import uk.gov.justice.digital.hmpps.entity.Offence
import uk.gov.justice.digital.hmpps.entity.ReferenceData
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
            )
        ),
        courtAppearances = listOf()
    ).also { it.mainOffence.set("event", it) }
    val DISPOSAL = Disposal(
        id = IdGenerator.getAndIncrement(),
        event = EVENT,
        type = DisposalType(
            id = IdGenerator.getAndIncrement(),
            description = "Sentenced"
        ),
        length = 6,
        lengthUnits = ReferenceData(IdGenerator.getAndIncrement(), "M", "Months")
    )
    val INSTITUTION = Institution(
        id = IdGenerator.getAndIncrement(),
        name = "test institution",
        establishment = "Y"
    )
    val CUSTODY = Custody(
        id = IdGenerator.getAndIncrement(),
        disposal = DISPOSAL,
        institution = INSTITUTION
    )

    val COURT = Court(courtId = IdGenerator.getAndIncrement(), courtName = "test court")
    val COURT_REPORT_TYPE = CourtReportType(courtReportTypeId = IdGenerator.getAndIncrement(), description = "court report type")
    val COURT_APPEARANCE = CourtAppearance(
        id = IdGenerator.getAndIncrement(),
        date = ZonedDateTime.now(),
        courtId = COURT.courtId,
        event = EVENT,
        outcome = ReferenceData(IdGenerator.getAndIncrement(), "TEST", "Community Order")
    )
    val COURT_REPORT = CourtReport(
        courtReportId = IdGenerator.getAndIncrement(),
        courtReportTypeId = COURT_REPORT_TYPE.courtReportTypeId,
        courtAppearanceId = COURT_APPEARANCE.id,
        dateRequested = LocalDate.of(2000, 1, 1)
    )

    val INSTITUTIONAL_REPORT_TYPE = ReferenceData(IdGenerator.getAndIncrement(), "IR", "institutional report type")
    val INSTITUTIONAL_REPORT = InstitutionalReport(
        institutionalReportId = IdGenerator.getAndIncrement(),
        institutionId = INSTITUTION.id,
        institutionReportTypeId = INSTITUTIONAL_REPORT_TYPE.id,
        custodyId = CUSTODY.id,
        establishment = "Y",
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
