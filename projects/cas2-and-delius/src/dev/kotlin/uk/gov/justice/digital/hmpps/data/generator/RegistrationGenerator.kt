package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.Deregistration
import uk.gov.justice.digital.hmpps.entity.RegisterType
import uk.gov.justice.digital.hmpps.entity.Registration
import java.time.LocalDate

object RegistrationGenerator {
    fun generateType(
        code: String = "RSC",
        description: String = code,
        id: Long = IdGenerator.getAndIncrement(),
    ) = RegisterType(
        id = id,
        code = code,
        description = description,
    )

    fun generateCategory(
        code: String = "CAT1",
        description: String = code,
        id: Long = IdGenerator.getAndIncrement(),
    ) = ReferenceData(
        id = id,
        code = code,
        description = description,
    )

    fun generate(
        person: Person,
        type: RegisterType = generateType(),
        category: ReferenceData = generateCategory(),
        registrationDate: LocalDate = LocalDate.now(),
        nextReviewDate: LocalDate? = registrationDate.plusYears(1),
        id: Long = IdGenerator.getAndIncrement(),
    ) = Registration(
        id = id,
        person = person,
        registrationDate = registrationDate,
        nextReviewDate = nextReviewDate,
        registerType = type,
        category = category,
    )

    fun generateDeregistration(
        registration: Registration,
        endDate: LocalDate = LocalDate.now(),
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Deregistration(
        id = id,
        registration = registration,
        endDate = endDate,
        softDeleted = softDeleted,
    )
}

