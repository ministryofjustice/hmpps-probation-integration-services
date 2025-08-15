package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Person
import uk.gov.justice.digital.hmpps.integrations.delius.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.Registration
import java.time.LocalDate
import java.time.LocalDateTime

object RegistrationGenerator {
    val SUICIDE_SELF_HARM_RISK_TYPE = RegisterType(
        id = IdGenerator.getAndIncrement(),
        code = "ALSH",
        description = "Suicide/Self Harm Risk",
        flag = ReferenceDataGenerator.SAFEGUARDING_FLAG
    )

    val CONTACT_SUSPENDED_TYPE = RegisterType(
        id = IdGenerator.getAndIncrement(),
        code = "PRC",
        description = "Contact Suspended",
        flag = ReferenceDataGenerator.INFORMATION_FLAG
    )

    val SUICIDE_SELF_HARM_REGISTRATION = generateRegistration(
        type = SUICIDE_SELF_HARM_RISK_TYPE,
        level = ReferenceDataGenerator.HIGH_RISK_REGISTER_LEVEL,
        notes = "Suicide/Self Harm risk",
        date = LocalDate.now().minusDays(2)
    )

    val CONTACT_SUSPENDED_REGISTRATION = generateRegistration(
        type = CONTACT_SUSPENDED_TYPE,
        level = ReferenceDataGenerator.HIGH_RISK_REGISTER_LEVEL,
        notes = "Contact suspended",
        date = LocalDate.now()
    )

    fun generateRegistration(
        type: RegisterType,
        person: Person = PersonGenerator.DEFAULT_PERSON,
        date: LocalDate = LocalDate.now(),
        level: ReferenceData,
        documentLinked: Boolean = false,
        deregistered: Boolean = false,
        softDeleted: Boolean = false,
        createdDateTime: LocalDateTime = LocalDateTime.now(),
        notes: String? = null
    ): Registration = Registration(
        id = IdGenerator.getAndIncrement(),
        person = person,
        type = type,
        date = date,
        level = level,
        documentLinked = documentLinked,
        deregistered = deregistered,
        softDeleted = softDeleted,
        createdDateTime = createdDateTime,
        notes = notes
    )
}