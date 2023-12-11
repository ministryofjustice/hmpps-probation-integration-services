package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Nsi
import uk.gov.justice.digital.hmpps.entity.NsiManager
import uk.gov.justice.digital.hmpps.entity.NsiStatus
import uk.gov.justice.digital.hmpps.entity.NsiType
import uk.gov.justice.digital.hmpps.entity.Person
import java.time.LocalDate

object NSIGenerator {
    val DEFAULT = generate(PersonGenerator.DEFAULT)

    fun generate(person: Person, id: Long = IdGenerator.getAndIncrement()) =
        Nsi(
            id,
            person,
            NSITypeGenerator.DTR,
            ReferenceDataGenerator.DTR_SUB_TYPE,
            NSIStatusGenerator.INITIATED,
            LocalDate.now(),
            null,
            "Duty to refer notes"
        )
}

object NSITypeGenerator {
    val DTR = generate("DTR")

    fun generate(code: String, id: Long = IdGenerator.getAndIncrement()) =
        NsiType(id, code)
}

object NSIStatusGenerator {
    val INITIATED = generate("INIT", "Initiated")

    fun generate(code: String, description: String, id: Long = IdGenerator.getAndIncrement()) =
        NsiStatus(id, code, description)
}

object NSIManagerGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) =
        NsiManager(
            id,
            NSIGenerator.DEFAULT,
            ProviderGenerator.DEFAULT_STAFF,
            ProviderGenerator.DEFAULT_TEAM,
            ProviderGenerator.DEFAULT_AREA
        )
}
