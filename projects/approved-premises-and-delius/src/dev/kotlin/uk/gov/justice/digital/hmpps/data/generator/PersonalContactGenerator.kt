package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCase
import uk.gov.justice.digital.hmpps.integrations.delius.person.contact.CasePersonalContactEntity
import java.time.LocalDate

object PersonalContactGenerator {

    val CASE_COMPLEX_DOCTOR = generate(ProbationCaseGenerator.CASE_COMPLEX)

    fun generate(case: ProbationCase) = CasePersonalContactEntity(
        IdGenerator.getAndIncrement(),
        case,
        "Captains mate",
        ReferenceDataGenerator.DOCTOR_RELATIONSHIP,
        "Shiver",
        "Me",
        "Timbers",
        "0779999887",
        AddressGenerator.DEFAULT,
        LocalDate.now()
    )
}
