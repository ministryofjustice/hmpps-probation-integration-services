package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integration.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integration.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integration.delius.reference.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integration.delius.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integration.delius.registration.entity.Registration
import uk.gov.justice.digital.hmpps.integration.delius.registration.entity.RegistrationReview
import java.time.LocalDate
import java.time.ZonedDateTime

object RegistrationGenerator {
    val FLAG = ReferenceDataGenerator.generate("FLAG1")
    val CATEGORY = ReferenceDataGenerator.generate("CAT1")
    val LEVEL = ReferenceDataGenerator.generate("LEV1")
    val DEFAULT_TYPE = generateType("RT1", FLAG, alertMessage = true, reviewPeriod = 6, colour = "GREEN")
    val ANOTHER_TYPE = generateType("AN1", null)

    fun generateType(
        code: String,
        flag: ReferenceData?,
        alertMessage: Boolean = false,
        description: String = "Description of $code",
        reviewPeriod: Long? = null,
        colour: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = RegisterType(code, description, flag, alertMessage, reviewPeriod, colour, id)

    fun generate(
        person: Person,
        type: RegisterType = DEFAULT_TYPE,
        category: ReferenceData? = CATEGORY,
        level: ReferenceData? = LEVEL,
        date: LocalDate = LocalDate.now(),
        reviewDate: LocalDate? = type.reviewPeriod?.let { date.plusMonths(it) },
        notes: String? = null,
        team: Team = ProviderGenerator.DEFAULT_TEAM,
        staff: Staff = ProviderGenerator.UNALLOCATED_STAFF,
        deregistered: Boolean = false,
        softDeleted: Boolean = false,
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        id: Long = IdGenerator.getAndIncrement()
    ) = Registration(
        person,
        type,
        category,
        level,
        date,
        reviewDate,
        notes,
        team,
        staff,
        listOf(),
        deregistered,
        softDeleted,
        createdDateTime,
        id
    )

    fun generateReview(
        registration: Registration,
        nextReviewDate: LocalDate? = null,
        notes: String? = null,
        team: Team = ProviderGenerator.DEFAULT_TEAM,
        staff: Staff = ProviderGenerator.UNALLOCATED_STAFF,
        completed: Boolean = false,
        softDeleted: Boolean = false,
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        id: Long = IdGenerator.getAndIncrement()
    ) = RegistrationReview(
        registration,
        registration.reviewDate ?: LocalDate.now(),
        nextReviewDate,
        notes,
        team,
        staff,
        completed,
        softDeleted,
        createdDateTime,
        id
    )
}