package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Person
import uk.gov.justice.digital.hmpps.integrations.delius.PersonManager
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT = Person(
        id = IdGenerator.getAndIncrement(),
        crn = "A000001",
        firstName = "Bob",
        secondName = null,
        thirdName = null,
        surname = "Smith",
        dateOfBirth = LocalDate.of(1980, 1, 1),
        gender = ReferenceDataGenerator.GENDER_MALE,
        noms = "A0001AA",
        pnc = "2004/0000001P"
    )
    val EXCLUDED = Person(
        id = IdGenerator.getAndIncrement(),
        crn = "E123456",
        firstName = "Excluded",
        secondName = null,
        thirdName = null,
        surname = "Person",
        dateOfBirth = LocalDate.of(1985, 3, 15),
        gender = ReferenceDataGenerator.GENDER_MALE,
        noms = "E0001AA",
        pnc = "2005/0000001E",
        exclusionMessage = "This case is excluded.",
        restrictionMessage = null
    )
    val RESTRICTED = Person(
        id = IdGenerator.getAndIncrement(),
        crn = "R123456",
        firstName = "Restricted",
        secondName = null,
        thirdName = null,
        surname = "Person",
        dateOfBirth = LocalDate.of(1992, 8, 30),
        gender = ReferenceDataGenerator.GENDER_MALE,
        noms = "R0001AA",
        pnc = "2006/0000001R",
        exclusionMessage = null,
        restrictionMessage = "This case is restricted."
    )
    val TEAM = Person(
        id = IdGenerator.getAndIncrement(),
        crn = "A000002",
        firstName = "Team",
        secondName = null,
        thirdName = null,
        surname = "Person",
        dateOfBirth = LocalDate.of(1980, 6, 15),
        gender = ReferenceDataGenerator.GENDER_MALE,
        noms = "A0002AA",
        pnc = "2010/00000001"
    )
    val OTHER_TEAM = Person(
        id = IdGenerator.getAndIncrement(),
        crn = "A000003",
        firstName = "Other Team",
        secondName = null,
        thirdName = null,
        surname = "Person",
        dateOfBirth = LocalDate.of(1980, 6, 18),
        gender = ReferenceDataGenerator.GENDER_MALE,
        noms = "A0003AA",
        pnc = "2010/00000002"
    )
}

object PersonManagerGenerator {
    val DEFAULT = PersonManager(
        id = IdGenerator.getAndIncrement(),
        personId = PersonGenerator.DEFAULT.id,
        team = TeamGenerator.DEFAULT,
        staff = StaffGenerator.DEFAULT,
        probationAreaId = ProviderGenerator.DEFAULT.id,
        active = true,
        softDeleted = false
    )
    val EXCLUDED = PersonManager(
        id = IdGenerator.getAndIncrement(),
        personId = PersonGenerator.EXCLUDED.id,
        team = TeamGenerator.DEFAULT,
        staff = StaffGenerator.DEFAULT,
        probationAreaId = ProviderGenerator.DEFAULT.id,
        active = true,
        softDeleted = false
    )
    val RESTRICTED = PersonManager(
        id = IdGenerator.getAndIncrement(),
        personId = PersonGenerator.RESTRICTED.id,
        team = TeamGenerator.DEFAULT,
        staff = StaffGenerator.DEFAULT,
        probationAreaId = ProviderGenerator.DEFAULT.id,
        active = true,
        softDeleted = false
    )
    val TEAM_MANAGER = PersonManager(
        id = IdGenerator.getAndIncrement(),
        personId = PersonGenerator.TEAM.id,
        team = TeamGenerator.DEFAULT,
        staff = StaffGenerator.TEAM_STAFF,
        probationAreaId = ProviderGenerator.DEFAULT.id,
        active = true,
        softDeleted = false
    )
    val OTHER_TEAM_MANAGER = PersonManager(
        id = IdGenerator.getAndIncrement(),
        personId = PersonGenerator.OTHER_TEAM.id,
        team = TeamGenerator.OTHER_TEAM,
        staff = StaffGenerator.OTHER_TEAM_STAFF,
        probationAreaId = ProviderGenerator.DEFAULT.id,
        active = true,
        softDeleted = false
    )
}