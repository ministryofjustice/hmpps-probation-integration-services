package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.REG_CAT1
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.ROSH
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData
import java.time.LocalDate
import java.time.LocalDateTime

object RegistrationGenerator {

    val TYPE_MAPPA = generateType("MAPP", null)
    val TYPE_OTH = generateType("OTH", null)
    val TYPE_DASO = generateType("DASO", null)
    val TYPE_ROSH_MEDIUM = generateType("RMRH", ROSH)

    fun generateType(code: String, referenceData: ReferenceData?, id: Long = IdGenerator.getAndIncrement()) = RegisterType(code, referenceData, id)

    fun generate(
        type: RegisterType,
        level: ReferenceData?,
        date: LocalDate,
        deRegistered: Boolean = false,
        softDeleted: Boolean = false,
        person: Person = PersonGenerator.DEFAULT,
        category: ReferenceData? = REG_CAT1,
        id: Long = IdGenerator.getAndIncrement()
    ) = Registration(
        person,
        type,
        level,
        date,
        LocalDate.now(),
        category,
        deRegistered,
        softDeleted,
        LocalDateTime.now(),
        id
    )
}
