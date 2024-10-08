package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtReport
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.Outcome
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.ReportManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProbationAreaEntity
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import java.time.*
import java.time.temporal.ChronoUnit

object SentenceGenerator {

    val CURRENTLY_MANAGED = generateEvent(
        PersonGenerator.CURRENTLY_MANAGED,
        referralDate = LocalDate.now().minusDays(1),
        inBreach = true,
        breachDate = LocalDate.now().minusMonths(3),
        court = CourtGenerator.BHAM
    )

    val INACTIVE_EVENT = generateEvent(
        PersonGenerator.CURRENTLY_MANAGED,
        referralDate = LocalDate.now().minusDays(1),
        inBreach = true,
        breachDate = LocalDate.now().minusMonths(3),
        court = CourtGenerator.BHAM,
        active = false
    )

    val INACTIVE_EVENT_1 = generateEvent(
        PersonGenerator.NO_ACTIVE_EVENTS,
        referralDate = LocalDate.now().minusDays(1),
        inBreach = true,
        breachDate = LocalDate.now().minusMonths(3),
        court = CourtGenerator.BHAM,
        active = false
    )

    val CURRENT_SENTENCE = generateSentence(
        CURRENTLY_MANAGED,
        LocalDate.now(),
        DisposalTypeGenerator.CURFEW_ORDER,
        entryLength = 12,
        entryLengthUnits = ReferenceDataGenerator.LENGTH_UNITS,
        lengthInDays = 99
    )

    val NO_SENTENCE_EVENT = generateEvent(PersonGenerator.NO_SENTENCE, referralDate = LocalDate.now())

    val CURRENT_CUSTODY = generateCustody(
        CURRENT_SENTENCE,
        ReferenceDataGenerator.CUSTODIAL_STATUS,
        "FD1234",
        InstitutionGenerator.WSIHMP
    )

    val RELEASE_1 = generateRelease(LocalDateTime.now().minusDays(3))
    val RELEASE_2 = generateRelease(LocalDateTime.now().minusDays(2))

    val OUTCOME = Outcome(
        Outcome.Code.AWAITING_PSR.value,
        Outcome.Code.AWAITING_PSR.description,
        IdGenerator.getAndIncrement()
    )

    val COURT_APPEARANCE = generateCourtAppearance(
        CURRENTLY_MANAGED,
        OUTCOME,
        LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)
    )

    val CURRENT_ORDER_MANAGER = generateOrderManager(
        CURRENTLY_MANAGED,
        StaffGenerator.ALLOCATED,
        CourtGenerator.PROBATION_AREA,
        ZonedDateTime.of(LocalDate.now(), LocalTime.NOON, ZoneId.of("Europe/London")),
        ZonedDateTime.of(LocalDate.now().minusDays(3), LocalTime.NOON, ZoneId.of("Europe/London"))
    )

    val ACTIVE_NSI_STATUS = NsiStatus(IdGenerator.getAndIncrement(), "SLI01", "Active")

    val BREACH_NSIS = generateBreachNsi(CURRENT_SENTENCE)

    val CONDITIONAL_RELEASE_KEY_DATE = generateKeyDates(LocalDate.now(), CURRENT_CUSTODY, ReferenceDataGenerator.ACR)
    val LED_KEY_DATE = generateKeyDates(LocalDate.now().plusDays(1), CURRENT_CUSTODY, ReferenceDataGenerator.LED)
    val HDC_KEY_DATE = generateKeyDates(LocalDate.now().plusDays(2), CURRENT_CUSTODY, ReferenceDataGenerator.HDE)
    val PAROLE_KEY_DATE = generateKeyDates(LocalDate.now().plusDays(3), CURRENT_CUSTODY, ReferenceDataGenerator.PED)
    val SENTENCE_KEY_DATE = generateKeyDates(LocalDate.now().plusDays(4), CURRENT_CUSTODY, ReferenceDataGenerator.SED)
    val EXPECTED_RELEASE_KEY_DATE =
        generateKeyDates(LocalDate.now().plusDays(5), CURRENT_CUSTODY, ReferenceDataGenerator.EXP)
    val SUPERVISION_KEY_DATE =
        generateKeyDates(LocalDate.now().plusDays(6), CURRENT_CUSTODY, ReferenceDataGenerator.PSSED)
    val HANDOVER_START_KEY_DATE =
        generateKeyDates(LocalDate.now().plusDays(7), CURRENT_CUSTODY, ReferenceDataGenerator.POM1)
    val HANDOVER_KEY_DATE = generateKeyDates(LocalDate.now().plusDays(8), CURRENT_CUSTODY, ReferenceDataGenerator.POM2)

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
        unpaidWorkDetails: UpwDetails? = null,
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
        unpaidWorkDetails,
        id
    )

    fun generateEvent(
        person: Person,
        mainOffence: MainOffence? = null,
        referralDate: LocalDate,
        inBreach: Boolean = false,
        breachDate: LocalDate? = null,
        court: Court? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(
        person.id,
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
        referralDate,
        court = court
    )

    fun generateOrderManager(
        event: Event,
        staff: Staff? = null,
        probationArea: ProbationAreaEntity,
        allocatedDate: ZonedDateTime,
        endDate: ZonedDateTime,
        team: Team? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = OrderManager(event, staff, active, probationArea, team, allocatedDate, endDate, softDeleted, id)

    fun generateCourtAppearance(
        event: Event,
        outcome: Outcome,
        appearanceDate: LocalDateTime,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = CourtAppearance(
        event,
        outcome,
        appearanceDate,
        softDeleted,
        ReferenceDataGenerator.TRIAL,
        CourtGenerator.BHAM,
        PersonGenerator.CURRENTLY_MANAGED,
        id
    )

    fun generateCustody(
        disposal: Disposal,
        custodialStatus: ReferenceData,
        prisonerNumber: String,
        institution: Institution?,
        id: Long = IdGenerator.getAndIncrement()
    ) = Custody(disposal, custodialStatus, prisonerNumber = prisonerNumber, institution = institution, id = id)

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
            LocalDate.now().atStartOfDay(),
            offenceCount = 1,
            PersonGenerator.CURRENTLY_MANAGED.id,
            ZonedDateTime.of(LocalDate.now().minusDays(3), LocalTime.NOON, EuropeLondon),
            ZonedDateTime.of(LocalDate.now().plusDays(1), LocalTime.NOON, EuropeLondon),
        )

    val MAIN_OFFENCE_FOR_INACTIVE_EVENT =
        generateMainOffence(
            INACTIVE_EVENT,
            MAIN_OFFENCE,
            LocalDate.now().atStartOfDay(),
            offenceCount = 1,
            PersonGenerator.CURRENTLY_MANAGED.id,
            ZonedDateTime.of(LocalDate.now().minusDays(3), LocalTime.NOON, EuropeLondon),
            ZonedDateTime.of(LocalDate.now().plusDays(1), LocalTime.NOON, EuropeLondon),
        )

    val MAIN_OFFENCE_FOR_INACTIVE_EVENT_1 =
        generateMainOffence(
            INACTIVE_EVENT_1,
            MAIN_OFFENCE,
            LocalDate.now().atStartOfDay(),
            offenceCount = 1,
            PersonGenerator.NO_ACTIVE_EVENTS.id,
            ZonedDateTime.of(LocalDate.now().minusDays(3), LocalTime.NOON, EuropeLondon),
            ZonedDateTime.of(LocalDate.now().plusDays(1), LocalTime.NOON, EuropeLondon),
        )

    val ADDITIONAL_OFFENCE_DEFAULT = generateAdditionalOffence(
        CURRENTLY_MANAGED,
        ADDITIONAL_OFFENCE,
        LocalDate.now().atStartOfDay(),
        ZonedDateTime.of(LocalDate.now().minusMonths(1), LocalTime.NOON, EuropeLondon),
        ZonedDateTime.of(LocalDate.now().plusMonths(1), LocalTime.NOON, EuropeLondon),
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
        created: ZonedDateTime,
        updated: ZonedDateTime,
        tics: Long? = null,
        verdict: String? = null,
        id: Long = IdGenerator.getAndIncrement(),
        softDeleted: Boolean = false
    ) = MainOffence(id, event, offence, date, offenceCount, tics, verdict, offenderId, created, updated, softDeleted)

    fun generateAdditionalOffence(
        event: Event,
        offence: Offence,
        date: LocalDateTime,
        created: ZonedDateTime,
        updated: ZonedDateTime,
        id: Long = IdGenerator.getAndIncrement(),
        softDeleted: Boolean = false,
        offenceCount: Long? = null,
    ) = AdditionalOffence(event, offence, date, softDeleted, offenceCount, created, updated, id)

    fun generateLicenseCondition(
        id: Long = IdGenerator.getAndIncrement(),
        disposal: Disposal
    ) = LicenceCondition(
        disposal,
        LocalDate.now(),
        LocalDate.now(),
        "commencement notes",
        LocalDate.now(),
        "termination notes",
        LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
        ReferenceDataGenerator.LIC_COND_MAIN_CAT,
        ReferenceDataGenerator.LIC_COND_SUB_CAT,
        "Licence Condition notes",
        id
    )

    fun generateBreachNsi(
        disposal: Disposal,
        outcome: ReferenceData? = ReferenceDataGenerator.NSI_BREACH_OUTCOME,
        status: NsiStatus = ACTIVE_NSI_STATUS
    ) = Nsi(
        disposal.event.personId,
        disposal.event.id,
        ReferenceDataGenerator.NSI_TYPE,
        status,
        referralDate = LocalDate.now().plusDays(1),
        statusDate = LocalDateTime.of(2024, 7, 1, 12, 0, 0, 0),
        null,
        outcome,
        actualStartDate = LocalDate.now(),
        expectedStartDate = LocalDate.of(2024, 1, 1),
        actualEndDate = LocalDate.now(),
        expectedEndDate = LocalDate.now().minusDays(1),
        RequirementsGenerator.ACTIVE_REQ.id,
        7,
        "notes",
        "external ref",
        CourtGenerator.PROBATION_AREA
    )

    fun generatePssRequirement(custodyId: Long, id: Long = IdGenerator.getAndIncrement(), active: Boolean) =
        PssRequirement(
            custodyId,
            ReferenceDataGenerator.PSS_MAIN_CAT,
            ReferenceDataGenerator.PSS_SUB_CAT,
            active = active,
            id = id
        )

    fun generateCourtReport(
        courtAppearance: CourtAppearance,
        personId: Long,
        id: Long = IdGenerator.getAndIncrement()
    ) =
        CourtReport(
            personId,
            LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
            LocalDateTime.now().plusDays(5).truncatedTo(ChronoUnit.MICROS),
            null,
            LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
            LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
            LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
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

    fun generateKeyDates(date: LocalDate, custody: Custody, keyDateType: ReferenceData) =
        KeyDate(IdGenerator.getAndIncrement(), date, custody, keyDateType)

    fun generateRelease(date: LocalDateTime) = Release(
        id = IdGenerator.getAndIncrement(),
        custody = CURRENT_CUSTODY,
        date = date
    )
}
