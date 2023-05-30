package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

object RegistrationGenerator {
    val MAPPA = generate(PersonGenerator.CASE_SUMMARY.id, "MAPP", "MAPPA 1", category = "M1", level = "M2")
    val HIGH_ROSH = generate(PersonGenerator.CASE_SUMMARY.id, "RHRH", "High RoSH", flag = RegisterType.ROSH_FLAG)

    fun generate(
        personId: Long,
        type: String,
        typeDescription: String,
        category: String? = null,
        level: String? = null,
        flag: String = "5"
    ) = Registration(
        id = IdGenerator.getAndIncrement(),
        personId = personId,
        notes = "Test notes",
        date = LocalDate.now(),
        type = RegisterType(
            id = IdGenerator.getAndIncrement(),
            code = type,
            description = typeDescription,
            flag = ReferenceData(
                id = IdGenerator.getAndIncrement(),
                code = flag,
                description = "Flag $flag"
            )
        ),
        category = category?.let {
            ReferenceData(
                id = IdGenerator.getAndIncrement(),
                code = category,
                description = "Category $category"
            )
        },
        level = level?.let {
            ReferenceData(
                id = IdGenerator.getAndIncrement(),
                code = level,
                description = "Level $level"
            )
        }
    )
}
