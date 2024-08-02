package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.api.model.DocumentType
import uk.gov.justice.digital.hmpps.data.generator.AreaGenerator.PARTITION_AREA
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DEFAULT_ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DEFAULT_ADDRESS_TYPE
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DEFAULT_ALLOCATION_REASON
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DEFAULT_TIER
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DISABILITY_CONDITION_1
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DISABILITY_TYPE_1
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.ETHNICITY
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.GENDER_IDENTITY
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.GENDER_MALE
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.IMMIGRATION_STATUS
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.LANGUAGE_ENG
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.NATIONALITY
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.PROVISION_CATEGORY_1
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.PROVISION_TYPE_1
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RELIGION
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.SECOND_NATIONALITY
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.SEXUAL_ORIENTATION
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.TITLE
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.*
import java.time.LocalDate
import java.time.ZonedDateTime

object PersonGenerator {

    val NEW_TO_PROBATION = generate("N123456")
    val CURRENTLY_MANAGED = generate("C123456", currentDisposal = true)
    val PREVIOUSLY_MANAGED = generate("P123456")
    val NO_SENTENCE = generate("U123456")
    val NO_ACTIVE_EVENTS = generate("Z123456")
    val RESTRICTED_CASE = generate("X123456", currentRestriction = true)
    val EXCLUDED_CASE = generate("Y123456", currentExclusion = true)

    val PROVISION_1 = generateProvision(CURRENTLY_MANAGED.id, null)
    val DISABILITY_1 = generateDisability(CURRENTLY_MANAGED.id, null)

    val PREVIOUS_CONVICTION_DOC = DocumentEntityGenerator.generateDocument(
        personId = CURRENTLY_MANAGED.id,
        primaryKeyId = null,
        DocumentType.PREVIOUS_CONVICTION.name,
        null
    )

    val ADDRESS = generateAddress(CURRENTLY_MANAGED.id, false)
    val ALIAS = generatePersonAlias(CURRENTLY_MANAGED)

    val PRISON_MANAGER = generatePrisonManager(CURRENTLY_MANAGED)
    val RESPONSIBLE_OFFICER = generateResponsibleOfficer(CURRENTLY_MANAGED, PRISON_MANAGER)

    fun generate(
        crn: String,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
        currentDisposal: Boolean = false,
        currentExclusion: Boolean = false,
        currentRestriction: Boolean = false,
        emailAddress: String? = "test@test.none",
        secondName: String? = "MiddleName",
        thirdName: String? = "OtherMiddleName",
        mobileNumber: String? = "07876545678",
        telephoneNumber: String? = "0161786567",
        title: ReferenceData? = TITLE,
        currentTier: ReferenceData? = DEFAULT_TIER
    ) =
        Person(
            id = id,
            crn = crn,
            softDeleted = softDeleted,
            emailAddress = emailAddress,
            currentDisposal = currentDisposal,
            currentExclusion = currentExclusion,
            currentRestriction = currentRestriction,
            currentHighestRiskColour = "RED",
            dateOfBirth = LocalDate.of(1977, 8, 12),
            partitionArea = PARTITION_AREA,
            allowSms = true,
            mobileNumber = mobileNumber,
            telephoneNumber = telephoneNumber,
            forename = "TestForename",
            secondName = secondName,
            thirdName = thirdName,
            gender = GENDER_MALE,
            surname = "TestSurname",
            preferredName = "Other name",
            offenderDetails = "Some details",
            genderIdentity = GENDER_IDENTITY,
            genderIdentityDescription = "Some self described gender identity",
            ethnicity = ETHNICITY,
            immigrationStatus = IMMIGRATION_STATUS,
            nationality = NATIONALITY,
            language = LANGUAGE_ENG,
            languageConcerns = "A concern",
            requiresInterpreter = false,
            currentRemandStatus = "Remand Status",
            secondNationality = SECOND_NATIONALITY,
            sexualOrientation = SEXUAL_ORIENTATION,
            religion = RELIGION,
            niNumber = "JK002213K",
            pnc = "1234567890123",
            nomsNumber = "NOMS123",
            croNumber = "CRO123",
            immigrationNumber = "IMA123",
            mostRecentPrisonerNumber = "PRS123",
            previousSurname = "Previous",
            title = title,
            offenderManagers = emptyList(),
            restrictionMessage = "restrictionMessage",
            exclusionMessage = "exclusionMessage",
            currentTier = currentTier
        )

    fun generatePrisonManager(person: Person) = PrisonManager(
        id = IdGenerator.getAndIncrement(),
        personId = person.id,
        date = ZonedDateTime.now(),
        allocationReason = DEFAULT_ALLOCATION_REASON,
        staff = StaffGenerator.ALLOCATED,
        team = TeamGenerator.DEFAULT,
        probationArea = ProviderGenerator.DEFAULT,
        telephoneNumber = "0987654321",
    )

    fun generateResponsibleOfficer(person: Person, prisonManager: PrisonManager) = ResponsibleOfficer(
        personId = person.id,
        prisonManager = prisonManager,
        startDate = ZonedDateTime.now(),
        id = IdGenerator.getAndIncrement()
    )

    fun generatePersonManager(person: Person) =
        PersonManager(
            id = IdGenerator.getAndIncrement(),
            trustProviderFlag = false,
            person = person,
            team = TeamGenerator.DEFAULT,
            staff = StaffGenerator.ALLOCATED,
            provider = ProviderGenerator.DEFAULT,
            date = ZonedDateTime.now().minusDays(2),
            allocationReason = DEFAULT_ALLOCATION_REASON,
            officer = StaffGenerator.OFFICER,
            partitionArea = PARTITION_AREA,
            staffEmployeeId = StaffGenerator.ALLOCATED.id,
            providerEmployee = ProviderEmployeeGenerator.PROVIDER_EMPLOYEE
        )

    fun generatePersonAlias(person: Person, secondName: String? = "Reg", thirdName: String? = "Xavier") =
        OffenderAlias(
            aliasID = IdGenerator.getAndIncrement(),
            personId = person.id,
            dateOfBirth = LocalDate.of(1968, 1, 1),
            firstName = "Bob",
            secondName = secondName,
            thirdName = thirdName,
            surname = "Potts",
            gender = GENDER_MALE,
            softDeleted = false
        )

    fun generateProvision(personId: Long, end: LocalDate?) = Provision(
        id = IdGenerator.getAndIncrement(),
        personId = personId,
        type = PROVISION_TYPE_1,
        startDate = LocalDate.now().minusDays(1),
        lastUpdated = LocalDate.now().minusDays(1),
        category = PROVISION_CATEGORY_1,
        notes = null,
        finishDate = end,
    )

    fun generateDisability(personId: Long, end: LocalDate?, condition: ReferenceData? = DISABILITY_CONDITION_1) =
        Disability(
            id = IdGenerator.getAndIncrement(),
            personId = personId,
            type = DISABILITY_TYPE_1,
            startDate = LocalDate.now().minusDays(1),
            lastUpdated = ZonedDateTime.now().minusDays(1),
            condition = condition,
            notes = null,
            finishDate = end,
        )

    fun generateAddress(
        personId: Long,
        softDeleted: Boolean,
        type: ReferenceData? = DEFAULT_ADDRESS_TYPE,
        typeVerified: Boolean? = true
    ) =
        PersonAddress(
            id = IdGenerator.getAndIncrement(),
            personId = personId,
            type = type,
            status = DEFAULT_ADDRESS_STATUS,
            streetName = "A Street",
            town = "A town",
            county = "A county",
            postcode = "NE209XL",
            telephoneNumber = "089876765",
            buildingName = "The building",
            district = "A District",
            addressNumber = "20",
            noFixedAbode = false,
            typeVerified = typeVerified,
            startDate = LocalDate.now().minusDays(1),
            endDate = null,
            softDeleted = softDeleted,
            createdDatetime = ZonedDateTime.now().minusDays(1),
        )
}
