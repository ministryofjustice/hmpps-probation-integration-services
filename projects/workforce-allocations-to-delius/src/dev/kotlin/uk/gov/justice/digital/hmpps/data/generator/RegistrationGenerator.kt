package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.registration.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.event.registration.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.LocalDate

object RegistrationGenerator {
    val DEFAULT = generate()

    fun generate(
        person: Person = PersonGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Registration(
        id,
        person,
        LocalDate.now(),
        "Registration notes",
        RegisterTypeGenerator.DEFAULT,
        listOf(),
        false,
    )
}

object RegisterTypeGenerator {
    val DEFAULT = generate("Register Type")

    fun generate(
        code: String,
        description: String = code,
        id: Long = IdGenerator.getAndIncrement()
    ) = RegisterType(id, description)
}
