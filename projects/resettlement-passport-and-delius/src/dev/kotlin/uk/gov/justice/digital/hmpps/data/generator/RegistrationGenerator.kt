package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Category
import uk.gov.justice.digital.hmpps.entity.Level
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.RegisterType
import uk.gov.justice.digital.hmpps.entity.Registration
import java.time.LocalDate

object RegistrationGenerator {
    val MAPPA_TYPE = generateType("MAPP")
    val CATEGORIES = Category.entries.map { ReferenceDataGenerator.generate(it.name) }.associateBy { it.code }
    val LEVELS = Level.entries.map { ReferenceDataGenerator.generate(it.name) }.associateBy { it.code }

    fun generateType(code: String, id: Long = IdGenerator.getAndIncrement()) = RegisterType(id, code)

    fun generate(
        type: RegisterType = MAPPA_TYPE,
        category: ReferenceData? = null,
        level: ReferenceData? = null,
        date: LocalDate = LocalDate.now(),
        reviewDate: LocalDate? = null,
        person: Person = PersonGenerator.DEFAULT,
        deRegistered: Boolean = false,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Registration(person, type, category, level, date, reviewDate, deRegistered, softDeleted, id)
}
