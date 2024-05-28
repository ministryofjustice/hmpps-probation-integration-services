package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtReport
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.Outcome
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.ReportManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.PssRequirement
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import java.time.LocalDate
import java.time.ZonedDateTime

object SentenceGenerator {
    val CURRENTLY_MANAGED = generateEvent(
        PersonGenerator.CURRENTLY_MANAGED,
        referralDate = LocalDate.now().minusDays(1),
        inBreach = true,
        breachDate = LocalDate.now().minusMonths(3)
    )

    fun generateSentence(
        event: Event,
        startDate: ZonedDateTime,
        disposalType: ReferenceData,
        custody: Custody? = null,
        endDate: ZonedDateTime? = null,
        terminationDate: ZonedDateTime? = null,
        entryLength: Long? = null,
        entryLengthUnits: ReferenceData? = null,
        lengthInDays: Long? = null,
        terminationReason: ReferenceData? = null,
        upw: Boolean = true,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(
        event,
        custody,
        startDate,
        disposalType,
        endDate,
        terminationDate,
        entryLength,
        entryLengthUnits,
        lengthInDays,
        terminationReason,
        upw,
        active,
        softDeleted,
        id
    )

    fun generateEvent(
        person: Person,
        mainOffence: MainOffence? = null,
        referralDate: LocalDate,
        inBreach: Boolean = false,
        breachDate: LocalDate? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(
        person,
        mainOffence,
        inBreach,
        breachDate,
        LocalDate.now(),
        null,
        active,
        softDeleted,
        id,
        "1",
        2,
        referralDate
    )

    fun generateOrderManager(
        event: Event,
        staff: Staff,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = OrderManager(event, staff, active, softDeleted, id)

    fun generateCourtAppearance(
        event: Event,
        outcome: Outcome,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = CourtAppearance(event, outcome, DocumentEntityGenerator.COURT.courtId, softDeleted, id)

    fun generateCustody(
        disposal: Disposal,
        custodialStatus: ReferenceData,
        id: Long = IdGenerator.getAndIncrement()
    ) = Custody(disposal, custodialStatus, id = id)

    val MAIN_OFFENCE = generateOffence("00303", "Main Offence")
    val ADDITIONAL_OFFENCE = generateOffence("00701", "Additional Offence")
    val MAIN_OFFENCE_DEFAULT = generateMainOffence(CURRENTLY_MANAGED, MAIN_OFFENCE, LocalDate.now())

    fun generateOffence(
        code: String,
        description: String,
        abbreviation: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Offence(id, code, description, abbreviation)

    fun generateMainOffence(
        event: Event,
        offence: Offence,
        date: LocalDate,
        id: Long = IdGenerator.getAndIncrement(),
        softDeleted: Boolean = false
    ) = MainOffence(id, event, offence, date, softDeleted)

    fun generateAdditionalOffence(
        event: Event,
        offence: Offence,
        date: LocalDate,
        id: Long = IdGenerator.getAndIncrement(),
        softDeleted: Boolean = false
    ) = AdditionalOffence(event, offence, date, softDeleted, id)

    fun generateRequirement(
        id: Long = IdGenerator.getAndIncrement(),
        disposal: Disposal
    ) = Requirement(
        disposal,
        ReferenceDataGenerator.REQUIREMENT_MAIN_CAT,
        ReferenceDataGenerator.REQUIREMENT_SUB_CAT,
        ReferenceDataGenerator.AD_REQUIREMENT_MAIN_CAT,
        ReferenceDataGenerator.AD_REQUIREMENT_SUB_CAT,
        LocalDate.now(),
        id = id
    )

    fun generateLicenseCondition(
        id: Long = IdGenerator.getAndIncrement(),
        disposal: Disposal
    ) = LicenceCondition(
        disposal,
        LocalDate.now(),
        ReferenceDataGenerator.LIC_COND_MAIN_CAT,
        ReferenceDataGenerator.LIC_COND_SUB_CAT,
        "Licence Condition notes",
        id
    )

    fun generateBreachNsi(disposal: Disposal) = Nsi(
        disposal.event.person.id,
        disposal.event.id,
        ReferenceDataGenerator.NSI_TYPE,
        null,
        ReferenceDataGenerator.NSI_BREACH_OUTCOME,
        LocalDate.now(),
        LocalDate.now(),
        LocalDate.now()
    )

    fun generatePssRequirement(custodyId: Long, id: Long = IdGenerator.getAndIncrement()) = PssRequirement(
        custodyId,
        ReferenceDataGenerator.PSS_MAIN_CAT,
        ReferenceDataGenerator.PSS_SUB_CAT,
        id = id
    )

    fun generateCourtReport(courtAppearance: CourtAppearance, id: Long = IdGenerator.getAndIncrement()) =
        CourtReport(
            LocalDate.now(),
            LocalDate.now().plusDays(5),
            null,
            ReferenceDataGenerator.COURT_REPORT_TYPE,
            null,
            courtAppearance,
            softDeleted = false,
            id = id
        )

    fun generateCourtReportManager(courtReport: CourtReport, id: Long = IdGenerator.getAndIncrement()) =
        ReportManager(
            courtReport,
            StaffGenerator.ALLOCATED,
            active = true,
            softDeleted = false,
            id = id
        )
}
