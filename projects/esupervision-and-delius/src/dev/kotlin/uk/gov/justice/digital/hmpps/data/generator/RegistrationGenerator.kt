package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.RegisterType
import uk.gov.justice.digital.hmpps.entity.Registration

object RegistrationGenerator {
    val CONTACT_SUSPENDED_TYPE = RegisterType(id = IdGenerator.getAndIncrement(), code = "PRC")

    val CONTACT_SUSPENDED_REGISTRATION = generateRegistration(PersonGenerator.PERSON_CONTACT_DETAILS_2)

    val DEREGISTERED_PRC_REGISTRATION = generateRegistration(
        person = PersonGenerator.PERSON_CONTACT_DETAILS_1,
        deregistered = true,
    )

    fun generateRegistration(
        person: Person,
        type: RegisterType = CONTACT_SUSPENDED_TYPE,
        deregistered: Boolean = false,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Registration(
        id = id,
        personId = person.id,
        type = type,
        deregistered = deregistered,
        softDeleted = softDeleted,
    )
}
