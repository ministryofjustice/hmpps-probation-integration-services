package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Registration
import java.time.LocalDate

object RegistrationGenerator {
    val DEFAULT_ALT7_REGISTRATION = Registration(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        registrationNotes = "Some registration notes",
        startDate = LocalDate.now().minusDays(20),
        endDate = null,
        softDeleted = false,
        deregistered = false,
        documentLinked = true,
        registerType = ReferenceDataGenerator.DEFAULT_REGISTER_TYPE,
        registerLevel = ReferenceDataGenerator.DEFAULT_REGISTER_LEVEL,
        registerCategory = ReferenceDataGenerator.DEFAULT_REGISTER_CATEGORY
    )

    val DEFAULT_OTHER_REGISTRATION = Registration(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        registrationNotes = "Some registration notes",
        startDate = LocalDate.now().minusDays(20),
        endDate = null,
        softDeleted = false,
        deregistered = false,
        documentLinked = true,
        registerType = ReferenceDataGenerator.OTHER_REGISTER_TYPE,
        registerLevel = ReferenceDataGenerator.DEFAULT_REGISTER_LEVEL,
        registerCategory = ReferenceDataGenerator.DEFAULT_REGISTER_CATEGORY
    )
}