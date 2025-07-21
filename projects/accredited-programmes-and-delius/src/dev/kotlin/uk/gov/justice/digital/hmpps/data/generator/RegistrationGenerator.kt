package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.toCrn
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.registration.RegisterType
import uk.gov.justice.digital.hmpps.entity.registration.Registration
import java.time.LocalDate

object RegistrationGenerator {
    fun generate(person: Person, type: RegisterType, category: ReferenceData) = Registration(
        id = id(),
        person = person.toCrn(),
        type = type,
        category = category,
        date = LocalDate.of(2000, 1, 1),
        nextReviewDate = LocalDate.of(2000, 6, 1),
    )
}
