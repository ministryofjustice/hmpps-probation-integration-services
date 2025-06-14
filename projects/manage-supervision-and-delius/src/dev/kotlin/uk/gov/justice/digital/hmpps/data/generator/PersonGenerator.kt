package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_STAFF
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.LIMITED_ACCESS_STAFF
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.risk.DeRegistration
import uk.gov.justice.digital.hmpps.integrations.delius.risk.RegistrationReview
import uk.gov.justice.digital.hmpps.integrations.delius.risk.RiskFlag
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Court
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Caseload
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.CaseloadPerson
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Team
import java.time.*
import java.time.temporal.ChronoUnit

object PersonGenerator {

    val GENDER_MALE = ReferenceData(IdGenerator.getAndIncrement(), "M", "Male")
    val MAPPA_CATEGORY = ReferenceData(IdGenerator.getAndIncrement(), "X9", "X9 Desc")
    val MAPPA_LEVEL = ReferenceData(IdGenerator.getAndIncrement(), "M2", "M2 Desc")
    val OVERVIEW = generateOverview("X000004")
    val EVENT_1 = generateEvent(
        OVERVIEW,
        eventNumber = "7654321",
        notes = "overview",
        additionalOffences = emptyList(),
        court = CourtGenerator.BHAM,
        convictionDate = LocalDate.now(),
    )
    val EVENT_2 = generateEvent(
        OVERVIEW,
        eventNumber = "1234567",
        inBreach = true,
        notes = "overview",
        additionalOffences = emptyList()
    )
    val INACTIVE_EVENT_1 = generateEvent(
        OVERVIEW,
        eventNumber = "654321",
        inBreach = true,
        active = false,
        notes = "inactive",
        additionalOffences = emptyList()
    )
    val INACTIVE_EVENT_2 = generateEvent(
        OVERVIEW,
        eventNumber = "854321",
        inBreach = true,
        active = false,
        notes = "inactive",
        additionalOffences = emptyList()
    )

    val INACTIVE_EVENT_3 = generateEvent(
        OVERVIEW,
        eventNumber = "954321",
        inBreach = true,
        active = true,
        notes = "inactive",
        additionalOffences = emptyList()
    )

    val INACTIVE_EVENT_NO_TIME_UNIT = generateEvent(
        OVERVIEW,
        eventNumber = "954322",
        inBreach = false,
        active = false,
        notes = "inactive",
        additionalOffences = emptyList()
    )

    val OFFENCE_1 = generateOffence("Murder", "Murder", "MAIN")
    val OFFENCE_2 = generateOffence("Another Murder", "Murder", "MAINA")
    val OFFENCE_3 = generateOffence("Burglary in a dwelling - 02800", "Burglary in a dwelling", "02800")
    val OFFENCE_4 =
        generateOffence("Burglary, other than a dwelling - 03000", "Burglary other than in a dwelling", "03000")

    val MAIN_OFFENCE_1 = generateMainOffence(
        1,
        EVENT_1,
        OFFENCE_1,
        LocalDate.now()
    )

    val MAIN_OFFENCE_2 = generateMainOffence(
        1,
        EVENT_2,
        OFFENCE_2,
        LocalDate.now()
    )

    val MAIN_OFFENCE_3 = generateMainOffence(
        1,
        INACTIVE_EVENT_1,
        OFFENCE_1,
        LocalDate.now()
    )

    val MAIN_OFFENCE_4 = generateMainOffence(
        1,
        INACTIVE_EVENT_2,
        OFFENCE_4,
        LocalDate.now()
    )

    val MAIN_OFFENCE_5 = generateMainOffence(
        1,
        INACTIVE_EVENT_3,
        OFFENCE_4,
        LocalDate.now()
    )

    val MAIN_OFFENCE_6 = generateMainOffence(
        1,
        INACTIVE_EVENT_NO_TIME_UNIT,
        OFFENCE_4,
        LocalDate.now()
    )

    val DEFAULT_DISPOSAL_TYPE = generateDisposalType("DFS", "Default Sentence Type", "NP", 0)

    val TERMINATION_REASON = generateTerminationReason()

    val REF_DATA_YEARS = ReferenceData(IdGenerator.getAndIncrement(), "Y", "Years")
    val REF_DATA_MONTHS = ReferenceData(IdGenerator.getAndIncrement(), "M", "Months")
    val ACTIVE_ORDER = generateDisposal(EVENT_1, length = 12, lengthUnit = REF_DATA_MONTHS)
    val INACTIVE_ORDER_1 =
        generateDisposal(
            INACTIVE_EVENT_1,
            LocalDate.now().minusDays(8),
            REF_DATA_YEARS,
            length = 25,
            terminationReason = TERMINATION_REASON
        )

    val INACTIVE_ORDER_2 = generateDisposal(INACTIVE_EVENT_2, LocalDate.now().minusDays(7), REF_DATA_MONTHS, length = 7)
    val INACTIVE_ORDER_3 = generateDisposal(INACTIVE_EVENT_3, LocalDate.now().minusDays(7), active = false)
    val INACTIVE_ORDER_4 =
        generateDisposal(INACTIVE_EVENT_NO_TIME_UNIT, LocalDate.now().minusDays(7), length = 36, active = false)

    val ADD_OFF_1 = generateOffence("Burglary", "Burglary", "ADD1")
    val ADDITIONAL_OFFENCE_1 = generateAdditionalOffence(
        1,
        EVENT_1,
        ADD_OFF_1,
        LocalDate.now()
    )

    val ADD_OFF_2 = generateOffence("Assault", "Assault", "ADD2")
    val ADDITIONAL_OFFENCE_2 = generateAdditionalOffence(
        1,
        EVENT_1,
        ADD_OFF_2,
        LocalDate.now()
    )

    val MAIN_CAT_F_TYPE = ReferenceData(IdGenerator.getAndIncrement(), "G03", "High Intensity")
    val UNIT = ReferenceData(IdGenerator.getAndIncrement(), "D", "Days")
    val MAIN_CAT_F = RequirementMainCategory(IdGenerator.getAndIncrement(), "F", "Main", UNIT)
    val REQUIREMENT = generateRequirement(
        ACTIVE_ORDER,
        subCategory = MAIN_CAT_F_TYPE,
        mainCategory = MAIN_CAT_F,
        terminationDetails = null
    )

    val MAIN_CAT_W_TYPE = ReferenceData(IdGenerator.getAndIncrement(), "W02", "Intensive")
    val MAIN_CAT_W = RequirementMainCategory(IdGenerator.getAndIncrement(), "W", "Unpaid Work", UNIT)
    val REQUIREMENT_UNPAID_WORK = generateRequirement(
        ACTIVE_ORDER,
        subCategory = MAIN_CAT_W_TYPE,
        mainCategory = MAIN_CAT_W,
        terminationDetails = null
    )

    val REQUIREMENT_CONTACT_1 = ContactGenerator.generateContact(
        OVERVIEW,
        ContactGenerator.APPT_CT_1,
        ZonedDateTime.of(LocalDateTime.now().minusHours(1), ZoneId.of("Europe/London")),
        rarActivity = true,
        attended = true,
        complied = true,
        requirement = REQUIREMENT
    )
    val REQUIREMENT_CONTACT_2 = ContactGenerator.generateContact(
        OVERVIEW,
        ContactGenerator.APPT_CT_1,
        ZonedDateTime.of(LocalDateTime.now().minusHours(1), ZoneId.of("Europe/London")),
        rarActivity = true,
        attended = null,
        complied = true,
        requirement = REQUIREMENT
    )

    val REGISTER_TYPE_1 = generateRegisterType("CODE1", "Restraining Order", "White")
    val REGISTER_TYPE_2 = generateRegisterType("CODE2", "Domestic Abuse Perpetrator", "Red")
    val MAPPA_TYPE = generateRegisterType("MAPP", "Mappa", "Green")

    val riskNotes = """
            Risk Notes 1
            ---------------------------------------------------------
            Risk Notes 2
        """.trimIndent()

    val REGISTRATION_1 = generateRegistration(REGISTER_TYPE_1, null, OVERVIEW.id, "Notes")
    val REGISTRATION_2 = generateRegistration(REGISTER_TYPE_2, null, OVERVIEW.id, riskNotes)
    val REGISTRATION_3 = generateRegistration(REGISTER_TYPE_2, null, OVERVIEW.id, "Notes", deRegistered = true)

    val MAPPA_REGISTRATION = generateRegistration(MAPPA_TYPE, MAPPA_CATEGORY, OVERVIEW.id, "Notes", level = MAPPA_LEVEL)

    val REGISTRATION_REVIEW_1 = generateRiskReview(
        REGISTRATION_2, LocalDate.now().minusDays(4),
        LocalDate.now().minusDays(1), "Notes", ZonedDateTime.now().minusDays(1)
    )

    val REGISTRATION_REVIEW_2 = generateRiskReview(
        REGISTRATION_2, LocalDate.now().minusDays(1),
        LocalDate.now().minusDays(2), "Most recent Notes", ZonedDateTime.now().minusDays(1)
    )

    val REGISTRATION_REVIEW_3 = generateRiskReview(
        REGISTRATION_3, LocalDate.now().minusDays(1),
        LocalDate.now().minusDays(2), "Most recent Notes", ZonedDateTime.now().minusDays(1)
    )

    val deRegistrationRiskNotes = """
            Comment added by Alan Shearer on 23/04/2024 at 14:45
            My note
            ---------------------------------------------------------
            Comment added by Harry Kane on 23/04/2024 at 14:47
            Made a mistake
        """.trimIndent()

    val DEREGISTRATION_1 = generateDeRegistration(REGISTRATION_3, LocalDate.now().minusDays(1), deRegistrationRiskNotes)

    val PERSON_1 =
        generateCaseloadPerson(OVERVIEW.id, OVERVIEW.crn, OVERVIEW.forename, OVERVIEW.secondName, OVERVIEW.surname)
    val PERSON_2 = generateCaseloadPerson(
        PersonDetailsGenerator.PERSONAL_DETAILS.id,
        PersonDetailsGenerator.PERSONAL_DETAILS.crn,
        PersonDetailsGenerator.PERSONAL_DETAILS.forename,
        PersonDetailsGenerator.PERSONAL_DETAILS.secondName,
        PersonDetailsGenerator.PERSONAL_DETAILS.surname
    )

    val CL_EXCLUDED =
        generateCaseloadPerson(
            PersonDetailsGenerator.EXCLUSION.id,
            PersonDetailsGenerator.EXCLUSION.crn,
            PersonDetailsGenerator.EXCLUSION.forename,
            PersonDetailsGenerator.EXCLUSION.secondName,
            PersonDetailsGenerator.EXCLUSION.surname
        )
    val CL_RESTRICTED = generateCaseloadPerson(
        PersonDetailsGenerator.RESTRICTION.id,
        PersonDetailsGenerator.RESTRICTION.crn,
        PersonDetailsGenerator.RESTRICTION.forename,
        PersonDetailsGenerator.RESTRICTION.secondName,
        PersonDetailsGenerator.RESTRICTION.surname
    )

    val CL_RESTRICTED_EXCLUDED = generateCaseloadPerson(
        PersonDetailsGenerator.RESTRICTION_EXCLUSION.id,
        PersonDetailsGenerator.RESTRICTION_EXCLUSION.crn,
        PersonDetailsGenerator.RESTRICTION_EXCLUSION.forename,
        PersonDetailsGenerator.RESTRICTION_EXCLUSION.secondName,
        PersonDetailsGenerator.RESTRICTION_EXCLUSION.surname
    )

    val CASELOAD_PERSON_1 = generateCaseload(PERSON_1, DEFAULT_STAFF, ContactGenerator.DEFAULT_TEAM)
    val CASELOAD_PERSON_2 = generateCaseload(PERSON_2, ContactGenerator.STAFF_1, ContactGenerator.DEFAULT_TEAM)
    val CASELOAD_PERSON_3 = generateCaseload(PERSON_2, DEFAULT_STAFF, ContactGenerator.DEFAULT_TEAM)

    val CASELOAD_LIMITED_ACCESS_EXCLUSION =
        generateCaseload(CL_EXCLUDED, LIMITED_ACCESS_STAFF, ContactGenerator.DEFAULT_TEAM)
    val CASELOAD_LIMITED_ACCESS_RESTRICTION =
        generateCaseload(CL_RESTRICTED, LIMITED_ACCESS_STAFF, ContactGenerator.DEFAULT_TEAM)
    val CASELOAD_LIMITED_ACCESS_BOTH =
        generateCaseload(CL_RESTRICTED_EXCLUDED, LIMITED_ACCESS_STAFF, ContactGenerator.DEFAULT_TEAM)
    val CASELOAD_LIMITED_ACCESS_NEITHER =
        generateCaseload(PERSON_2, LIMITED_ACCESS_STAFF, ContactGenerator.DEFAULT_TEAM)

    fun generateEvent(
        person: Person,
        id: Long = IdGenerator.getAndIncrement(),
        court: Court? = null,
        convictionDate: LocalDate? = null,
        eventNumber: String,
        active: Boolean = true,
        inBreach: Boolean = false,
        disposal: Disposal? = null,
        mainOffence: MainOffence? = null,
        notes: String,
        additionalOffences: List<AdditionalOffence>
    ) =
        Event(
            id,
            person.id,
            court,
            convictionDate,
            eventNumber,
            disposal = disposal,
            inBreach = inBreach,
            active = active,
            mainOffence = mainOffence,
            notes = notes,
            additionalOffences = additionalOffences,
            dateCreated = ZonedDateTime.now()
        )

    val DISABILITIES: List<Disability> = listOf(
        Disability(
            IdGenerator.getAndIncrement(),
            OVERVIEW.id,
            ReferenceData(IdGenerator.getAndIncrement(), "D01", "Mental Illness"),
            LocalDate.now().minusDays(1),
            LocalDate.now().minusDays(1),
            USER
        ),
        Disability(
            IdGenerator.getAndIncrement(),
            OVERVIEW.id,
            ReferenceData(IdGenerator.getAndIncrement(), "D02", "Visual Impairment"),
            LocalDate.now(),
            LocalDate.now().minusDays(1),
            USER
        )
    )

    val PERSONAL_CIRCUMSTANCES: List<PersonalCircumstance> = listOf(
        PersonalCircumstance(
            IdGenerator.getAndIncrement(),
            OVERVIEW.id,
            PersonalCircumstanceType(IdGenerator.getAndIncrement(), "E01", "Employment"),
            PersonalCircumstanceSubType(
                IdGenerator.getAndIncrement(),
                "Full-time employed (30 or more hours per week"
            ),
            LocalDate.now(),
            USER,
            null,
            true,
            LocalDate.now().minusDays(1),

            ),
        PersonalCircumstance(
            IdGenerator.getAndIncrement(),
            OVERVIEW.id,
            PersonalCircumstanceType(IdGenerator.getAndIncrement(), "A02", "Accommodation"),
            PersonalCircumstanceSubType(IdGenerator.getAndIncrement(), "Friends/Family (settled)"),
            LocalDate.now(),
            USER,
            null,
            true,
            LocalDate.now().minusDays(1)
        )
    )

    val PROVISIONS: List<Provision> = listOf(
        Provision(
            IdGenerator.getAndIncrement(),
            OVERVIEW.id,
            ReferenceData(IdGenerator.getAndIncrement(), "FF01", "Flex refreshment breaks"),
            LocalDate.now(),
            LocalDate.now().minusDays(1),
            USER
        ),
        Provision(
            IdGenerator.getAndIncrement(),
            OVERVIEW.id,
            ReferenceData(IdGenerator.getAndIncrement(), "CC02", "Colour/visibility marking"),
            LocalDate.now(),
            LocalDate.now().minusDays(1),
            USER
        )
    )

    fun generateOverview(
        crn: String,
        forename: String = "Forename",
        secondName: String? = "Middle1",
        thirdName: String? = "Middle2",
        surname: String = "Surname",
        emailAddress: String? = "testemail",
        mobileNumber: String? = "1234",
        telephoneNumber: String? = "4321",
        preferredName: String? = "Dee",
        dateOfBirth: LocalDate = LocalDate.now().minusYears(50),
        gender: ReferenceData = GENDER_MALE,
        id: Long = IdGenerator.getAndIncrement(),
        exclusionMessage: String? = null,
        restrictionMessage: String? = null
    ) = Person(
        id = id,
        crn = crn,
        forename = forename,
        secondName = secondName,
        thirdName = thirdName,
        surname = surname,
        dateOfBirth = dateOfBirth,
        gender = gender,
        emailAddress = emailAddress,
        mobileNumber = mobileNumber,
        telephoneNumber = telephoneNumber,
        preferredName = preferredName,
        pnc = "pnc1234567890",
        noms = "noms123",
        religion = null,
        sexualOrientation = null,
        genderIdentity = null,
        genderIdentityDescription = null,
        exclusionMessage = exclusionMessage,
        restrictionMessage = restrictionMessage
    )

    fun generateRequirement(
        disposal: Disposal,
        length: Long = 12,
        notes: String? = "my notes",
        mainCategory: RequirementMainCategory,
        active: Boolean = true,
        softDeleted: Boolean = false,
        expectedStartDate: LocalDate? = LocalDate.now().minusDays(1),
        startDate: LocalDate = LocalDate.now(),
        commencementDate: LocalDate? = LocalDate.now().minusDays(4),
        expectedEndDate: LocalDate? = LocalDate.now().minusDays(2),
        terminationDate: LocalDate? = LocalDate.now().minusDays(3),
        subCategory: ReferenceData?,
        terminationDetails: ReferenceData?,
        id: Long = IdGenerator.getAndIncrement()
    ) = Requirement(
        id,
        length,
        notes,
        expectedStartDate,
        startDate,
        commencementDate,
        expectedEndDate,
        terminationDate,
        disposal,
        mainCategory,
        subCategory,
        terminationDetails,
        active,
        softDeleted
    )

    fun generateDisposalType(
        code: String,
        description: String,
        sentenceType: String? = null,
        ftcLimit: Long? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = DisposalType(code, description, sentenceType, ftcLimit, id)

    fun generateTerminationReason() =
        ReferenceData(id = IdGenerator.getAndIncrement(), code = "TERM1", "Termination Reason")

    fun generateDisposal(
        event: Event,
        terminationDate: LocalDate? = null,
        lengthUnit: ReferenceData? = null,
        date: LocalDate = LocalDate.now().minusDays(14),
        length: Long? = null,
        type: DisposalType = DEFAULT_DISPOSAL_TYPE,
        enteredEndDate: LocalDate? = null,
        notionalEndDate: LocalDate? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
        terminationReason: ReferenceData? = null
    ) = Disposal(
        event,
        event.personId,
        date,
        length,
        type,
        terminationReason,
        lengthUnit,
        enteredEndDate,
        notionalEndDate,
        terminationDate,
        active,
        softDeleted,
        id
    )

    fun generateOffence(
        description: String,
        category: String,
        code: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Offence(id, code, description, category)

    fun generateMainOffence(
        offenceCount: Long,
        event: Event,
        offence: Offence,
        date: LocalDate,
        id: Long = IdGenerator.getAndIncrement(),
        softDeleted: Boolean = false
    ) = MainOffence(id, offenceCount, event, date, offence, softDeleted)

    fun generateAdditionalOffence(
        offenceCount: Long,
        event: Event,
        offence: Offence,
        date: LocalDate,
        id: Long = IdGenerator.getAndIncrement(),
        softDeleted: Boolean = false
    ) = AdditionalOffence(id, offenceCount, event, date, offence, softDeleted)

    fun generateRegisterType(
        code: String,
        description: String,
        colour: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = RegisterType(code, description, colour, id)

    fun generateRegistration(
        type: RegisterType,
        category: ReferenceData? = null,
        personId: Long,
        notes: String?,
        id: Long = IdGenerator.getAndIncrement(),
        deRegistered: Boolean = false,
        date: LocalDate = LocalDate.now(),
        level: ReferenceData? = null,
    ) = RiskFlag(
        personId,
        type,
        category,
        deRegistered,
        notes,
        LocalDate.now(),
        emptyList(),
        USER,
        LocalDate.now().plusDays(1),
        emptyList(),
        date,
        ZonedDateTime.of(date, LocalTime.NOON, EuropeLondon),
        level,
        false,
        id
    )

    fun generateDeRegistration(
        riskFlag: RiskFlag,
        deRegistrationDate: LocalDate,
        notes: String?,
    ) = DeRegistration(
        IdGenerator.getAndIncrement(), deRegistrationDate, riskFlag, notes, DEFAULT_STAFF, false
    )

    fun generateRiskReview(
        riskFlag: RiskFlag,
        date: LocalDate,
        reviewDue: LocalDate,
        notes: String?,
        createdDate: ZonedDateTime,
    ) = RegistrationReview(riskFlag, date, reviewDue, notes, true, false, createdDate, IdGenerator.getAndIncrement())

    val NSI_BREACH_TYPE = generateNsiType("BRE")
    val NSI_OPD_TYPE = generateNsiType("OPD1")
    val NSI_OPD_SUB_TYPE = ReferenceData(IdGenerator.getAndIncrement(), "OPD11", "OPD1 subtype")
    val NSI_STATUS = generateNsiStatus("STATUS1", "An NSI Status")
    val BREACH_PREVIOUS_ORDER_1 = generateNsi(OVERVIEW.id, INACTIVE_ORDER_1.event.id, NSI_BREACH_TYPE, NSI_STATUS)
    val BREACH_PREVIOUS_ORDER_2 = generateNsi(OVERVIEW.id, INACTIVE_ORDER_2.event.id, NSI_BREACH_TYPE, NSI_STATUS)
    val BREACH_ON_ACTIVE_ORDER = generateNsi(OVERVIEW.id, ACTIVE_ORDER.event.id, NSI_BREACH_TYPE, NSI_STATUS)

    val OPD_NSI = generateNsi(OVERVIEW.id, ACTIVE_ORDER.event.id, NSI_OPD_TYPE, NSI_STATUS, subType = NSI_OPD_SUB_TYPE)

    fun generateNsiType(code: String) =
        NsiType(id = IdGenerator.getAndIncrement(), code = code, description = "$code description")

    fun generateNsiStatus(code: String, description: String) =
        NsiStatus(id = IdGenerator.getAndIncrement(), code = code, description = description)

    fun generateNsi(
        personId: Long,
        eventId: Long,
        type: NsiType,
        status: NsiStatus,
        active: Boolean = true,
        subType: ReferenceData? = null
    ) = Nsi(
        id = IdGenerator.getAndIncrement(),
        personId = personId,
        actualStartDate = LocalDate.now().minusDays(5),
        expectedStartDate = LocalDate.now().minusDays(5),
        eventId = eventId,
        type = type,
        subType = subType,
        nsiStatus = status,
        lastUpdated = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS),
        active = active
    )

    fun generateCaseload(caseLoadPerson: CaseloadPerson, staff: Staff, team: Team) =
        Caseload(
            id = IdGenerator.getAndIncrement(),
            person = caseLoadPerson,
            staff = staff,
            team = team,
            roleCode = "OM"
        )

    fun generateCaseloadPerson(id: Long, crn: String, forename: String, middleName: String?, surname: String) =
        CaseloadPerson(
            id = id,
            crn = crn,
            forename = forename,
            secondName = middleName,
            dateOfBirth = LocalDate.now().minusYears(50),
            surname = surname
        )
}

