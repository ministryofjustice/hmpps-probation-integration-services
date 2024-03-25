package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.api.model.Colour
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.DeRegistration
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.Registration
import java.time.LocalDate

object RegistrationGenerator {
    val DEFAULT = generate()
    val WITH_DEREGISTRATION = generate(deRegistration = DeRegistrationGenerator.DEFAULT)

    fun generate(
        person: Person = PersonGenerator.DEFAULT,
        type: RegisterType = RegisterTypeGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement(),
        deRegistration: DeRegistration? = null
    ) = Registration(
        id,
        person,
        LocalDate.now(),
        "Registration notes",
        type,
        listOfNotNull(deRegistration),
        false,
        false
    )

    fun generateRegistrations(person: Person = PersonGenerator.DEFAULT): List<Registration> =
        RegisterTypeGenerator.REGISTER_TYPES.map { generate(person, it) }
}

object DeRegistrationGenerator {
    val DEFAULT = generate()
    fun generate(id: Long = IdGenerator.getAndIncrement()) = DeRegistration(
        id,
        LocalDate.now(),
        RegistrationGenerator.DEFAULT,
        false
    )
}

object RegisterTypeGenerator {
    val ROSH = "Rosh"
    val ALERTS = "Alerts"
    val SAFEGUARDING = "Safeguarding"
    val INFORMATION = "Information"
    val PUBLIC_PROTECTION = "Public Protection"

    val ROSH_FLAG = generateRiskFlag("1", ROSH)
    val ALERTS_FLAG = generateRiskFlag("2", ALERTS)
    val SAFEGUARDING_FLAG = generateRiskFlag("3", SAFEGUARDING)
    val INFORMATION_FLAG = generateRiskFlag("4", INFORMATION)
    val PUBLIC_PROTECTION_FLAG = generateRiskFlag("5", PUBLIC_PROTECTION)

    val DEFAULT = generate("Register Type")
    val REGISTER_TYPES = listOf(
        generate(
            "RMRH",
            colour = Colour.AMBER.name,
            flag = ROSH_FLAG
        ),
        generate(
            "RHRH",
            colour = Colour.RED.name,
            flag = ROSH_FLAG
        ),
        generate(
            "ALT1",
            colour = Colour.GREEN.name,
            flag = ALERTS_FLAG
        ),
        generate(
            "ALERT",
            colour = Colour.RED.name,
            flag = ALERTS_FLAG
        ),
        generate(
            "CL2",
            colour = Colour.GREEN.name,
            flag = SAFEGUARDING_FLAG
        ),
        generate(
            "RVAD",
            colour = Colour.RED.name,
            flag = SAFEGUARDING_FLAG
        ),
        generate(
            "CPPC",
            colour = Colour.WHITE.name,
            flag = INFORMATION_FLAG
        ),
        generate(
            "SCH1",
            colour = Colour.RED.name,
            flag = INFORMATION_FLAG
        ),
        generate(
            "ADVP",
            colour = Colour.RED.name,
            flag = PUBLIC_PROTECTION_FLAG
        )
    )

    fun generateRiskFlag(
        code: String,
        description: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceDataGenerator.generate(DatasetGenerator.REGISTER_TYPE_FLAG, code, description, id)

    fun generate(
        code: String,
        flag: ReferenceData? = null,
        colour: String? = null,
        description: String = code,
        id: Long = IdGenerator.getAndIncrement()
    ) = RegisterType(id, description, flag, colour)
}
