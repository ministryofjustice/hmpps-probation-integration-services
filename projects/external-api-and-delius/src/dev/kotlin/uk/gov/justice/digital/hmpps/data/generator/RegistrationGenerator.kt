package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.entity.Person
import uk.gov.justice.digital.hmpps.integration.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integration.delius.entity.RegisterType
import uk.gov.justice.digital.hmpps.integration.delius.entity.Registration
import uk.gov.justice.digital.hmpps.service.Category
import uk.gov.justice.digital.hmpps.service.Level
import java.time.LocalDate

object RegistrationGenerator {
    val MAPPA_TYPE = generateType(RegisterType.MAPPA_CODE)
    val CATEGORIES = Category.entries.map { generateReferenceData(it.name) }.associateBy { it.code }
    val LEVELS = Level.entries.map { generateReferenceData(it.name) }.associateBy { it.code }

    fun generateType(code: String, id: Long = IdGenerator.getAndIncrement()) = RegisterType(code, id)

    fun generate(
        type: RegisterType = MAPPA_TYPE,
        category: ReferenceData? = null,
        level: ReferenceData? = null,
        date: LocalDate = LocalDate.now(),
        reviewDate: LocalDate? = null,
        notes: String? = null,
        person: Person = DataGenerator.PERSON,
        deRegistered: Boolean = false,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Registration(person.id, type, category, level, date, reviewDate, notes, deRegistered, softDeleted, id)

    fun generateReferenceData(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(code, description, id)
}