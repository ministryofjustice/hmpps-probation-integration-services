package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.AdditionalIdentifier
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

object PersonGenerator {
    val DEFAULT = generate("X552020", "A1234YZ")
    val NULL_EVENT_PROCESSING = generate("X854525", "N0770LL")
    val MERGED_FROM = generate("M346785", "M1234GD", softDeleted = true)
    val MERGED_TO = generate("M346787", "M3465GD")
    val OGRS4 = generate("X731390", "X1234YZ")


    fun generate(
        crn: String,
        nomsId: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(
        id = id,
        crn = crn,
        nomsId = nomsId,
        forename = "Test",
        secondName = "Test",
        surname = "Test",
        dateOfBirth = LocalDate.now().minusYears(18),
        gender = ReferenceDataGenerator.GENDER_MALE,
        softDeleted = softDeleted,
    )

    fun generateAdditionalIdentifier(
        personId: Long,
        type: ReferenceData,
        identifier: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = AdditionalIdentifier(personId, type, identifier, false, ZonedDateTime.now(), id)
}
