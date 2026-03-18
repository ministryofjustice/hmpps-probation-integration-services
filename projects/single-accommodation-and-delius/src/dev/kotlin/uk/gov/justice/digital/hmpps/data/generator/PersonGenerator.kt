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
}