package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DISABILITY_CONDITION_1
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DISABILITY_TYPE_1
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.PROVISION_CATEGORY_1
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.PROVISION_TYPE_1
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.Disability
import uk.gov.justice.digital.hmpps.integrations.delius.person.OffenderAlias
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonFull
import uk.gov.justice.digital.hmpps.integrations.delius.person.Provision
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import java.time.LocalDate
import java.time.ZonedDateTime

object PersonGenerator {
    val DEFAULT = generate(crn = "A000001")
    val DEFAULT_FULL = generateFull()
    val DISABILITY = generateDisability(DEFAULT_FULL.id)
    val PROVISION = generateProvision(DEFAULT_FULL.id)
    val PERSON_INACTIVE_EVENT = generate(crn = "A000002")
    val EVENT = generateEvent("7", DEFAULT)
    val ANOTHER_EVENT = generateEvent("8", DEFAULT)
    val INACTIVE_EVENT = generateEvent("6", PERSON_INACTIVE_EVENT, active = false)
    val PERSON_WITH_BOOKING = generate(crn = "B000001")

    fun generate(
        crn: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(id = id, crn = crn)

    fun generateFull(
    ) = PersonFull(
        id = IdGenerator.getAndIncrement(),
        crn = "Y000002",
        forename = "Jack",
        secondName = "James",
        thirdName = "Robert",
        surname = "Smith",
        preferredName = "Frank",
        dateOfBirth = LocalDate.of(1980, 1, 1),
        gender = ReferenceDataGenerator.GENDER_MALE,
        genderIdentityDescription = "Male",
        ethnicity = ReferenceDataGenerator.ETHNICITY_WHITE,
        nationality = ReferenceDataGenerator.NATIONALITY_BRITISH,
        immigrationStatus = ReferenceDataGenerator.IMMIGRATION_STATUS_REFUGEE,
        currentTier = ReferenceDataGenerator.DEFAULT_TIER,
        addresses = emptyList(),
        allowSms = true,
        emailAddress = "test@test.none",
        telephoneNumber = "01234567890",
        mobileNumber = "09876543210",
        previousSurname = "Jones",
        title = ReferenceDataGenerator.TITLE_DR,
        requiresInterpreter = true,
        religion = ReferenceDataGenerator.RELIGION_OTHER,
        language = ReferenceDataGenerator.LANGUAGE_ENGLISH,
        languageConcerns = "A concern",
        sexualOrientation = ReferenceDataGenerator.SEXUAL_ORIENTATION_OTHER,
        secondNationality = ReferenceDataGenerator.NATIONALITY_BRITISH,
        immigrationNumber = "1234567890",
        niNumber = "ABCD-1234",
        currentExclusion = true,
        currentRestriction = true,
        exclusionMessage = "Exclusion message",
        restrictionMessage = "Restriction message",
        mostRecentPrisonerNumber = "A1234BC",
        nomsNumber = "NOMS123",
        croNumber = "CRO1234",
        pnc = "PNC12345"
    )

    fun generateEvent(
        number: String,
        person: Person,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(id, number, person, active, softDeleted)

    fun generateRegistration(
        person: Person,
        type: RegisterType,
        date: LocalDate,
        category: ReferenceData? = null,
        level: ReferenceData? = null,
        softDeleted: Boolean = false,
        deregistered: Boolean = false,
        lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now(),
        id: Long = IdGenerator.getAndIncrement(),
        notes: String? = null
    ) = Registration(person.id, type, category, level, date, softDeleted, deregistered, lastUpdatedDateTime, notes, id)

    fun generateDisability(personId: Long) =
        Disability(
            id = IdGenerator.getAndIncrement(),
            personId = personId,
            type = DISABILITY_TYPE_1,
            startDate = LocalDate.of(2024, 12, 13),
            lastUpdated = ZonedDateTime.of(2023, 4, 21, 12, 54, 3, 0, ZonedDateTime.now().zone),
            condition = DISABILITY_CONDITION_1,
            notes = null,
            finishDate = LocalDate.of(2019, 12, 25),
        )

    fun generateProvision(personId: Long) = Provision(
        id = IdGenerator.getAndIncrement(),
        personId = personId,
        type = PROVISION_TYPE_1,
        startDate = LocalDate.of(2026, 1, 1),
        lastUpdated = LocalDate.of(2026, 2, 2),
        category = PROVISION_CATEGORY_1,
        notes = null,
        finishDate = LocalDate.of(2027, 3, 3),
    )
}

object PersonManagerGenerator {
    fun generate(
        person: Person,
        team: Team = TeamGenerator.generate(),
        staff: Staff = StaffGenerator.generate(teams = listOf(team)),
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(
        id = id,
        personId = person.id,
        staff = staff,
        team = team
    )
}

object OffenderAliasGenerator {
    val DEFAULT = OffenderAlias(
        aliasID = IdGenerator.getAndIncrement(),
        personId = PersonGenerator.DEFAULT_FULL.id,
        dateOfBirth = LocalDate.of(1970, 2, 2),
        firstName = "Jane",
        secondName = "Louise",
        surname = "Smith",
        thirdName = "Mary",
        gender = ReferenceDataGenerator.GENDER_FEMALE
    )
}
