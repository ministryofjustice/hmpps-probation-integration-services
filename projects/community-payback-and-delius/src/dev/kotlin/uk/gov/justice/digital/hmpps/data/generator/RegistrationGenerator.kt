package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.entity.person.Person
import uk.gov.justice.digital.hmpps.entity.person.RegisterType
import uk.gov.justice.digital.hmpps.entity.person.Registration

object RegistrationGenerator {
    val VULNERABLE_REGISTRATION_TYPE = RegisterType(
        id = IdGenerator.getAndIncrement(),
        code = "RVLN",
        description = "Vulnerable person"
    )

    val SELF_HARM_REGISTRATION_TYPE = RegisterType(
        id = IdGenerator.getAndIncrement(),
        code = "SHRM",
        description = "Self harm"
    )

    val DEFAULT_PERSON_VULNERABLE_REGISTRATION = createRegistration(DEFAULT_PERSON, VULNERABLE_REGISTRATION_TYPE)
    val DEFAULT_PERSON_SELF_HARM_REGISTRATION = createRegistration(DEFAULT_PERSON, SELF_HARM_REGISTRATION_TYPE)

    fun createRegistration(person: Person, type: RegisterType): Registration {
        return Registration(
            id = IdGenerator.getAndIncrement(),
            person = person,
            type = type,
            deregistered = false,
            softDeleted = false
        )
    }
}