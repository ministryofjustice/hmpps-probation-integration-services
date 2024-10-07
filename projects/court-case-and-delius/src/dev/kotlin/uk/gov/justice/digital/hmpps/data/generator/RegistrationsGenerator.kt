package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.CURRENTLY_MANAGED
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.REG_CATEGORY
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.REG_FLAG
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.REG_LEVEL
import uk.gov.justice.digital.hmpps.integration.delius.registration.entity.DeRegistration
import uk.gov.justice.digital.hmpps.integration.delius.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integration.delius.registration.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import java.time.LocalDate
import java.time.LocalDateTime

object RegistrationsGenerator {

    val REG_TYPE = RegisterType(
        "T1", "Register Type 1", REG_FLAG,
        true, 6L, "Red", IdGenerator.getAndIncrement()
    )

    val ACTIVE_REG = generateRegistrations(CURRENTLY_MANAGED, false)
    val INACTIVE_REG = generateRegistrations(CURRENTLY_MANAGED, true)

    val REG_NO_DEREG = generateRegistrations(CURRENTLY_MANAGED, false, level = null, category = null)

    val DEREG_1 = generateDeregistration(
        ACTIVE_REG,
        date = LocalDate.now().minusDays(1),
        createdDateTime = LocalDate.now().minusDays(1).atTime(13, 0)
    )

    val DEREG_2 = generateDeregistration(
        ACTIVE_REG,
        date = LocalDate.now().minusDays(1),
        createdDateTime = LocalDate.now().minusDays(1).atTime(14, 0)
    )

    val DEREG_3 = generateDeregistration(
        ACTIVE_REG,
        date = LocalDate.now().minusDays(2),
        createdDateTime = LocalDate.now().minusDays(2).atTime(18, 0)
    )

    val DEREG_4 = generateDeregistration(
        INACTIVE_REG,
        date = LocalDate.now().minusDays(1),
        createdDateTime = LocalDate.now().minusDays(1).atTime(13, 0)
    )

    val DEREG_5 = generateDeregistration(
        INACTIVE_REG,
        date = LocalDate.now().minusDays(1),
        createdDateTime = LocalDate.now().minusDays(1).atTime(14, 0)
    )

    fun generateRegistrations(
        person: Person,
        deRegistered: Boolean,
        category: ReferenceData? = REG_CATEGORY,
        level: ReferenceData? = REG_LEVEL
    ) = Registration(
        id = IdGenerator.getAndIncrement(),
        person = person,
        category = category,
        level = level,
        type = REG_TYPE,
        date = LocalDate.now().minusDays(2),
        reviewDate = LocalDate.now().minusDays(1),
        notes = "Notes",
        team = TeamGenerator.DEFAULT,
        staff = StaffGenerator.ALLOCATED,
        deRegistrations = emptyList(),
        deRegistered = deRegistered,
        softDeleted = false,
        createdDateTime = LocalDateTime.now()

    )

    fun generateDeregistration(registration: Registration, date: LocalDate, createdDateTime: LocalDateTime) =
        DeRegistration(
            id = IdGenerator.getAndIncrement(),
            registration = registration,
            deRegistrationDate = date,
            team = TeamGenerator.DEFAULT,
            staff = StaffGenerator.ALLOCATED,
            notes = "Deregistration notes",
            createdDateTime = createdDateTime,
            softDeleted = false
        )
}
