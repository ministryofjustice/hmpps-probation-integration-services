package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.overview.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import kotlin.Comparator

object PersonGenerator {

    val OVERVIEW = generateOverview("X000004")
    val EVENT_1 = generateEvent(OVERVIEW)
    val EVENT_2 = generateEvent(OVERVIEW, inBreach = true)
    val INACTIVE_EVENT_1 = generateEvent(OVERVIEW, inBreach = true, active = false)
    val INACTIVE_EVENT_2 = generateEvent(OVERVIEW, inBreach = true, active = false)
    fun generateEvent(person: Person, id: Long = IdGenerator.getAndIncrement(), active: Boolean = true, inBreach: Boolean = false) =
        Event(id, person.id, "1", inBreach, active = active)
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
        ethnicity: ReferenceData = ReferenceData(IdGenerator.getAndIncrement(), "A1", "Asian or Asian British: Indian"),
        primaryLanguage: ReferenceData = ReferenceData(IdGenerator.getAndIncrement(), "001", "English"),
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
                ReferenceData(IdGenerator.getAndIncrement(), "FT01", "Full-time employed (30 or more hours per week)"),
                LocalDate.now()
            ),
            PersonalCircumstance(
                IdGenerator.getAndIncrement(),
                id,
                ReferenceData(IdGenerator.getAndIncrement(), "A02", "Accommodation"),
                ReferenceData(IdGenerator.getAndIncrement(), "FM01", "Friends/Family (settled)"),
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
        nomsNumber = "TEST123",
        croNumber = "TEST",
        pncNumber = "TEST123456789",
        mostRecentPrisonerNumber = "TEST",
        dateOfBirth = dateOfBirth,
        gender = gender,
        ethnicity = ethnicity,
        primaryLanguage = primaryLanguage,
        disabilities = disabilities,
        emailAddress = emailAddress,
        mobileNumber = mobileNumber,
        personalCircumstances = personalCircumstances,
        provisions = provisions,
        telephoneNumber = telephoneNumber,
        preferredName = preferredName
    )


    val OFFENCE_MAIN = generateOffence("Murder", "MAIN")

    val MAIN_OFFENCE = generateMainOffence(
        EVENT_1,
        OFFENCE_MAIN,
        LocalDate.now())

    val DEFAULT_DISPOSAL_TYPE = generateDisposalType("DFS", "Default Sentence Type", "NP", 0)
    val FULL_DETAIL_ORDER = generateSentence(EVENT_1)

    val INACTIVE_ORDER_1 = generateSentence(INACTIVE_EVENT_1)
    val INACTIVE_ORDER_2 = generateSentence(INACTIVE_EVENT_2)

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



    fun generateDisposalType(
        code: String,
        description: String,
        sentenceType: String? = null,
        ftcLimit: Long? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = DisposalType(code, description, sentenceType, ftcLimit, id)

    fun generateSentence(
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

