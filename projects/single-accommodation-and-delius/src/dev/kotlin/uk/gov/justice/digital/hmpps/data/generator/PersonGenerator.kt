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
        pnc = "2004/0000001P",
        currentTier = ReferenceDataGenerator.C1_TIER
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
}