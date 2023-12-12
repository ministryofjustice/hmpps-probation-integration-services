package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.controller.entity.RegisterType
import uk.gov.justice.digital.hmpps.controller.entity.RegistrationEntity
import java.time.LocalDate

object RegistrationGenerator {
    val DEFAULT = generate()

    fun generate(
        id: Long = IdGenerator.getAndIncrement()
    ) = RegistrationEntity(
        id,
        CaseEntityGenerator.DEFAULT.id,
        RegisterTypeGenerator.DEFAULT,
        ReferenceDataGenerator.LEVEL_ONE,
        LocalDate.now()
    )
}

object RegisterTypeGenerator {
    val DEFAULT = generate("ROSH")

    fun generate(
        code: String,
        description: String = "$code description",
        id: Long = IdGenerator.getAndIncrement()
    ) = RegisterType(code, description, ReferenceDataGenerator.FLAG, id)
}
