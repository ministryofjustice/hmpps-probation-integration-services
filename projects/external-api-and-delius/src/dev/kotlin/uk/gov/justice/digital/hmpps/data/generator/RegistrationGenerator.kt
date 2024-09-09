package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.entity.*
import uk.gov.justice.digital.hmpps.model.Category
import uk.gov.justice.digital.hmpps.model.Level
import java.time.LocalDate

object RegistrationGenerator {
    val CHILD_CONCERNS_TYPE = generateType(RegisterType.CHILD_CONCERNS_CODE)
    val CHILD_PROTECTION_TYPE = generateType(RegisterType.CHILD_PROTECTION_CODE)
    val SERIOUS_FURTHER_OFFENCE_TYPE = generateType(RegisterType.SERIOUS_FURTHER_OFFENCE_CODE)
    val MAPPA_TYPE = generateType(RegisterType.MAPPA_CODE)
    val DATASET_TYPE_GENDER = Dataset(IdGenerator.getAndIncrement(), "GENDER")
    val DATASET_TYPE_OTHER = Dataset(IdGenerator.getAndIncrement(), "OTHER")
    val REFDATA_MALE = generateReferenceData("M", description = "MALE", dataset = DATASET_TYPE_GENDER)
    val REFDATA_FEMALE = generateReferenceData("F", description = "FEMALE", dataset = DATASET_TYPE_GENDER)
    val CATEGORIES = Category.entries.map { generateReferenceData(it.name) }.associateBy { it.code }
    val LEVELS = Level.entries.map { generateReferenceData(it.name) }.associateBy { it.code }

    fun generateType(code: String, id: Long = IdGenerator.getAndIncrement()) =
        RegisterType(code, "Description for $code", id)

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
    ) = RegistrationEntity(person.id, type, category, level, date, reviewDate, notes, deRegistered, softDeleted, id)

    fun generateReferenceData(
        code: String,
        description: String = "Description of $code",
        dataset: Dataset = DATASET_TYPE_OTHER,
        id: Long = IdGenerator.getAndIncrement(),

        ) = ReferenceData(code, description, dataset, id)
}