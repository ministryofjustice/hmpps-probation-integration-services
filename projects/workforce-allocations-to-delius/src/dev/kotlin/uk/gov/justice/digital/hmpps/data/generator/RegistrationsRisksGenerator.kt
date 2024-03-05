package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.Registration
import java.time.LocalDate
import java.time.ZonedDateTime

class RegistrationsRisksGenerator {

    fun generate() : Registration =
        Registration(person = PersonGenerator.DEFAULT,
                     type = RegisterTypeGenerator.RED_ROSH,
                     date = LocalDate.now(),
                     id = IdGenerator.getAndIncrement(),
                     softDeleted = false,
                     deRegistered = false,
                     createdDateTime = ZonedDateTime.now(),
                     )

    object RegisterTypeGenerator {
        val RED_ROSH = generate("Rosh",
                            "Rosh description",
                                 "Red")

        fun generate(code: String,
                    description: String,
                    colour: String,
                    flag: ReferenceData = ReferenceDataGenerator.ROSH,
                    id: Long = IdGenerator.getAndIncrement(),) = RegisterType(code, flag, description, colour, id)
    }

    object ReferenceDataGenerator {
        val ROSH = generate("1", "RoSH" )

        fun generate(code: String,
            description: String,
            id: Long = IdGenerator.getAndIncrement(),
            risk: Dataset = DatasetGenerator.REGISTER_TYPE_FLAG)
        = ReferenceData(id, code, description, risk)
                                                                                )
    }

}