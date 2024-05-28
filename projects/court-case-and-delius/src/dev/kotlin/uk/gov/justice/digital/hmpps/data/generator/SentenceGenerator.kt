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
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.PssRequirement
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

object SentenceGenerator {
    val CURRENTLY_MANAGED = generateEvent(
        PersonGenerator.CURRENTLY_MANAGED,
        referralDate = LocalDate.now().minusDays(1),
        inBreach = true,
        breachDate = LocalDate.now().minusMonths(3)
    )

    val CURRENT_SENTENCE = generateSentence(
        CURRENTLY_MANAGED,
        LocalDate.now(),
        DisposalTypeGenerator.CURFEW_ORDER,
        entryLength = 12,
        entryLengthUnits = ReferenceDataGenerator.LENGTH_UNITS,
        lengthInDays = 99
    )

    fun generateSentence(
        event: Event,
        startDate: LocalDate,
        disposalType: DisposalType,
        custody: Custody? = null,
        endDate: ZonedDateTime? = null,
        terminationDate: LocalDate? = null,
        entryLength: Long? = null,
        entryLengthUnits: ReferenceData? = null,
        lengthInDays: Long? = null,
        terminationReason: ReferenceData? = null,
        upw: Boolean = true,
        effectiveLength: Long? = null,
        entryLengthUnits2: ReferenceData? = null,
        length2: Long? = null,
        length: Long? = null,
        enteredSentenceEndDate: LocalDate? = null,
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
        effectiveLength,
        entryLengthUnits2,
        length2,
        length,
        enteredSentenceEndDate,
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

    val MAIN_OFFENCE =
        generateOffence(
            code = "00303",
            description = "Main Offence",
            offenceCategory = ReferenceDataGenerator.VIOLENCE,
            mainCategoryCode = "003",
            mainCategoryDescription = "Threats, conspiracy, or incitement to murder",
            mainCategoryAbbreviation = "Threats, conspiracy, or incitement to murder",
            subCategoryCode = "03",
            subCategoryDescription = "Assisting offender by impeding his apprehension or prosecution in a case of murder",
            form20Code = "21"
        )
    val ADDITIONAL_OFFENCE =
        generateOffence(
            code = "00701",
            description = "Additional Offence",
            offenceCategory = ReferenceDataGenerator.VIOLENCE,
            mainCategoryCode = "007",
            mainCategoryDescription = "Endangering life at sea",
            mainCategoryAbbreviation = "Endangering life at sea",
            subCategoryCode = "01",
            subCategoryDescription = "Sending unseaworthy ship to sea",
            form20Code = "2"
        )
    val MAIN_OFFENCE_DEFAULT =
        generateMainOffence(
            CURRENTLY_MANAGED,
            MAIN_OFFENCE,
            LocalDateTime.now(),
            offenceCount = 1,
            PersonGenerator.CURRENTLY_MANAGED.id,
            LocalDateTime.now().minusDays(3),
            LocalDateTime.now().plusDays(1)
        )

    val ADDITIONAL_OFFENCE_DEFAULT = generateAdditionalOffence(
        CURRENTLY_MANAGED,
        SentenceGenerator.ADDITIONAL_OFFENCE,
        LocalDateTime.now(),
        LocalDateTime.now().minusMonths(1),
        LocalDateTime.now().plusMonths(1),
    )

    fun generateOffence(
        code: String,
        description: String,
        offenceCategory: ReferenceData,
        mainCategoryCode: String,
        mainCategoryDescription: String,
        mainCategoryAbbreviation: String,
        subCategoryCode: String,
        subCategoryDescription: String,
        abbreviation: String? = null,
        form20Code: String? = null,
        subCategoryAbbreviation: String? = null,
        cjitCode: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) =
        Offence(
            id,
            offenceCategory,
            code,
            description,
            abbreviation,
            mainCategoryCode,
            mainCategoryDescription,
            mainCategoryAbbreviation,
            subCategoryCode,
            subCategoryDescription,
            form20Code,
            subCategoryAbbreviation,
            cjitCode
        )

    fun generateMainOffence(
        event: Event,
        offence: Offence,
        date: LocalDateTime,
        offenceCount: Long,
        offenderId: Long,
        created: LocalDateTime,
        updated: LocalDateTime,
        tics: Long? = null,
        verdict: String? = null,
        id: Long = IdGenerator.getAndIncrement(),
        softDeleted: Boolean = false
    ) = MainOffence(id, event, offence, date, offenceCount, tics, verdict, offenderId, created, updated, softDeleted)

    fun generateAdditionalOffence(
        event: Event,
        offence: Offence,
        date: LocalDateTime,
        created: LocalDateTime,
        updated: LocalDateTime,
        id: Long = IdGenerator.getAndIncrement(),
        softDeleted: Boolean = false,
        offenceCount: Long? = null,
    ) = AdditionalOffence(event, offence, date, softDeleted, offenceCount, created, updated, id)

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
