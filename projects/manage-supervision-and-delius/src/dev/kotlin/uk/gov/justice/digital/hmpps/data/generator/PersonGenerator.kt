package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object PersonGenerator {

    val OVERVIEW = generateOverview("X000004")
    val EVENT_1 = generateEvent(OVERVIEW, eventNumber = "7654321")
    val EVENT_2 = generateEvent(OVERVIEW, eventNumber = "1234567", inBreach = true)
    val INACTIVE_EVENT_1 = generateEvent(OVERVIEW, eventNumber = "654321", inBreach = true, active = false)
    val INACTIVE_EVENT_2 = generateEvent(OVERVIEW, eventNumber = "854321", inBreach = true, active = false)
    val OFFENCE_1 = generateOffence("Murder", "MAIN")
    val OFFENCE_2 = generateOffence("Another Murder", "MAINA")

    val MAIN_OFFENCE_1 = generateMainOffence(
        EVENT_1,
        OFFENCE_1,
        LocalDate.now()
    )

    val MAIN_OFFENCE_2 = generateMainOffence(
        EVENT_2,
        OFFENCE_2,
        LocalDate.now()
    )

    val DEFAULT_DISPOSAL_TYPE = generateDisposalType("DFS", "Default Sentence Type", "NP", 0)
    val ACTIVE_ORDER = generateDisposal(EVENT_1)

    val INACTIVE_ORDER_1 = generateDisposal(INACTIVE_EVENT_1)
    val INACTIVE_ORDER_2 = generateDisposal(INACTIVE_EVENT_2)

    val ADD_OFF_1 = generateOffence("Burglary", "ADD1")
    val ADDITIONAL_OFFENCE_1 = generateAdditionalOffence(
        EVENT_1,
        ADD_OFF_1,
        LocalDate.now()
    )

    val ADD_OFF_2 = generateOffence("Assault", "ADD2")
    val ADDITIONAL_OFFENCE_2 = generateAdditionalOffence(
        EVENT_1,
        ADD_OFF_2,
        LocalDate.now()
    )

    val MAIN_CAT_F = RequirementMainCategory(IdGenerator.getAndIncrement(), "F")
    val REQUIREMENT = generateRequirement(ACTIVE_ORDER)
    val REQUIREMENT_CONTACT_1 = ContactGenerator.generateContact(
        OVERVIEW,
        ContactGenerator.APPT_CT_1,
        ZonedDateTime.of(LocalDateTime.now().minusHours(1), ZoneId.of("Europe/London")),
        rarActivity = true,
        attended = true,
        complied = true,
        requirementId = REQUIREMENT.id
    )
    val REQUIREMENT_CONTACT_2 = ContactGenerator.generateContact(
        OVERVIEW,
        ContactGenerator.APPT_CT_1,
        ZonedDateTime.of(LocalDateTime.now().minusHours(1), ZoneId.of("Europe/London")),
        rarActivity = true,
        attended = null,
        complied = true,
        requirementId = REQUIREMENT.id
    )

    fun generateEvent(
        person: Person,
        id: Long = IdGenerator.getAndIncrement(),
        eventNumber: String,
        active: Boolean = true,
        inBreach: Boolean = false,
        disposal: Disposal? = null,
        mainOffence: MainOffence? = null
    ) =
        Event(
            id,
            person.id,
            eventNumber,
            disposal = disposal,
            inBreach = inBreach,
            active = active,
            mainOffence = mainOffence
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
        id: Long = IdGenerator.getAndIncrement(),
        disabilities: List<Disability> = listOf(
            Disability(
                IdGenerator.getAndIncrement(),
                id,
                ReferenceData(IdGenerator.getAndIncrement(), "D01", "Mental Illness"),
                LocalDate.now().minusDays(1)
            ),
            Disability(
                IdGenerator.getAndIncrement(),
                id,
                ReferenceData(IdGenerator.getAndIncrement(), "D02", "Visual Impairment"),
                LocalDate.now()
            )
        ),
        personalCircumstances: List<PersonalCircumstance> = listOf(
            PersonalCircumstance(
                IdGenerator.getAndIncrement(),
                id,
                ReferenceData(IdGenerator.getAndIncrement(), "E01", "Employment"),
                PersonalCircumstanceSubType(
                    IdGenerator.getAndIncrement(),
                    "Full-time employed (30 or more hours per week"
                ),
                LocalDate.now()
            ),
            PersonalCircumstance(
                IdGenerator.getAndIncrement(),
                id,
                ReferenceData(IdGenerator.getAndIncrement(), "A02", "Accommodation"),
                PersonalCircumstanceSubType(IdGenerator.getAndIncrement(), "Friends/Family (settled)"),
                LocalDate.now()
            )
        ),
        provisions: List<Provision> = listOf(
            Provision(
                IdGenerator.getAndIncrement(),
                id,
                ReferenceData(IdGenerator.getAndIncrement(), "FF01", "Flex refreshment breaks"),
                LocalDate.now()
            ),
            Provision(
                IdGenerator.getAndIncrement(),
                id,
                ReferenceData(IdGenerator.getAndIncrement(), "CC02", "Colour/visibility marking"),
                LocalDate.now()
            )
        ),

        ) = Person(
        id = id,
        crn = crn,
        forename = forename,
        secondName = secondName,
        thirdName = thirdName,
        surname = surname,
        dateOfBirth = dateOfBirth,
        gender = gender,
        disabilities = disabilities,
        emailAddress = emailAddress,
        mobileNumber = mobileNumber,
        personalCircumstances = personalCircumstances,
        provisions = provisions,
        telephoneNumber = telephoneNumber,
        preferredName = preferredName
    )

    fun generateRequirement(
        disposal: Disposal,
        mainCategory: RequirementMainCategory = MAIN_CAT_F,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Requirement(id, disposal, mainCategory, active, softDeleted)

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
        type: DisposalType = DEFAULT_DISPOSAL_TYPE,
        enteredEndDate: LocalDate? = null,
        notionalEndDate: LocalDate? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(event, date, type, enteredEndDate, notionalEndDate, active, softDeleted, id)

    fun generateOffence(
        description: String,
        code: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Offence(id, code, description)

    fun generateMainOffence(
        event: Event,
        offence: Offence,
        date: LocalDate,
        id: Long = IdGenerator.getAndIncrement(),
        softDeleted: Boolean = false
    ) = MainOffence(id, event, date, offence, softDeleted)

    fun generateAdditionalOffence(
        event: Event,
        offence: Offence,
        date: LocalDate,
        id: Long = IdGenerator.getAndIncrement(),
        softDeleted: Boolean = false
    ) = AdditionalOffence(id, event, date, offence, softDeleted)
}

