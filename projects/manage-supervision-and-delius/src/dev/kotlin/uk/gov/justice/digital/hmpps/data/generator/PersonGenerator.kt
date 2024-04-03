package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.UserGenerator.USER
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.risk.RegistrationReview
import uk.gov.justice.digital.hmpps.integrations.delius.risk.RiskFlag
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Court
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object PersonGenerator {

    val OVERVIEW = generateOverview("X000004")
    val OFFENDER_WITHOUT_EVENTS = generateOverview("X000005")
    val EVENT_1 = generateEvent(
        OVERVIEW,
        eventNumber = "7654321",
        notes = "overview",
        additionalOffences = emptyList(),
        court = CourtGenerator.BHAM,
        convictionDate = LocalDate.now()
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
    val OFFENCE_1 = generateOffence("Murder", "MAIN")
    val OFFENCE_2 = generateOffence("Another Murder", "MAINA")

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

    val DEFAULT_DISPOSAL_TYPE = generateDisposalType("DFS", "Default Sentence Type", "NP", 0)
    val ACTIVE_ORDER = generateDisposal(EVENT_1, length = 12)

    val INACTIVE_ORDER_1 = generateDisposal(INACTIVE_EVENT_1)
    val INACTIVE_ORDER_2 = generateDisposal(INACTIVE_EVENT_2)

    val ADD_OFF_1 = generateOffence("Burglary", "ADD1")
    val ADDITIONAL_OFFENCE_1 = generateAdditionalOffence(
        1,
        EVENT_1,
        ADD_OFF_1,
        LocalDate.now()
    )

    val ADD_OFF_2 = generateOffence("Assault", "ADD2")
    val ADDITIONAL_OFFENCE_2 = generateAdditionalOffence(
        1,
        EVENT_1,
        ADD_OFF_2,
        LocalDate.now()
    )

    val MAIN_CAT_F_SUB_ID = IdGenerator.getAndIncrement();
    val MAIN_CAT_F_TYPE = ReferenceData(MAIN_CAT_F_SUB_ID, "G03", "High Intensity")
    val MAIN_CAT_F = RequirementMainCategory(IdGenerator.getAndIncrement(), "F", "Main")
    val REQUIREMENT = generateRequirement(ACTIVE_ORDER, MAIN_CAT_F_SUB_ID)
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

    val REGISTER_TYPE_1 = generateRegisterType("CODE1", "Restraining Order")
    val REGISTER_TYPE_2 = generateRegisterType("CODE2", "Domestic Abuse Perpetrator")
    val REGISRATION_1 = generateRegistration(REGISTER_TYPE_1, OVERVIEW.id, "Notes")
    val REGISRATION_2 = generateRegistration(REGISTER_TYPE_2, OVERVIEW.id, "Notes")

    val REGISTRATION_REVIEW_1 = generateRiskReview(
        REGISRATION_2, LocalDate.now().minusDays(4),
        LocalDate.now().minusDays(1), "Notes", ZonedDateTime.now().minusDays(1)
    )

    val REGISTRATION_REVIEW_2 = generateRiskReview(
        REGISRATION_2, LocalDate.now().minusDays(1),
        LocalDate.now().minusDays(2), "Most recent Notes", ZonedDateTime.now().minusDays(1)
    )

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
            additionalOffences = additionalOffences
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
            ReferenceData(IdGenerator.getAndIncrement(), "E01", "Employment"),
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
            ReferenceData(IdGenerator.getAndIncrement(), "A02", "Accommodation"),
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
        gender: ReferenceData = ReferenceData(IdGenerator.getAndIncrement(), "M", "Male"),
        id: Long = IdGenerator.getAndIncrement()
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
        pnc = "pnc",
        religion = null,
        sexualOrientation = null,
        genderIdentity = null,
        genderIdentityDescription = null
    )

    fun generateRequirement(
        disposal: Disposal,
        subCategoryId: Long,
        length: Long = 12,
        notes: String? = "my notes",
        mainCategory: RequirementMainCategory = MAIN_CAT_F,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Requirement(id, length, notes, subCategoryId, disposal, mainCategory, active, softDeleted)

    fun generateDisposalType(
        code: String,
        description: String,
        sentenceType: String? = null,
        ftcLimit: Long? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = DisposalType(code, description, sentenceType, ftcLimit, id)

    fun generateDisposal(
        event: Event,
        date: LocalDate = LocalDate.now().minusDays(14),
        length: Long? = null,
        type: DisposalType = DEFAULT_DISPOSAL_TYPE,
        enteredEndDate: LocalDate? = null,
        notionalEndDate: LocalDate? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(event, date, length, type, enteredEndDate, notionalEndDate, active, softDeleted, id)

    fun generateOffence(
        description: String,
        code: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Offence(id, code, description)

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
        id: Long = IdGenerator.getAndIncrement()
    ) = RegisterType(code, description, id)

    fun generateRegistration(
        type: RegisterType,
        personId: Long,
        notes: String?,
        id: Long = IdGenerator.getAndIncrement(),
    ) = RiskFlag(
        personId, type, false, notes, LocalDate.now(), USER, LocalDate.now().plusDays(1), emptyList(),
        false, id
    )

    fun generateRiskReview(
        riskFlag: RiskFlag,
        date: LocalDate,
        reviewDue: LocalDate,
        notes: String?,
        createdDate: ZonedDateTime,
    ) = RegistrationReview(riskFlag, date, reviewDue, notes, true, false, createdDate, IdGenerator.getAndIncrement())
}

