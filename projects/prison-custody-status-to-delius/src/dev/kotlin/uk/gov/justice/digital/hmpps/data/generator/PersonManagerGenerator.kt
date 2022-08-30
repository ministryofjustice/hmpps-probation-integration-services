package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff

object PersonManagerGenerator {
    fun generate(
        person: Person,
        staff: Staff = StaffGenerator.UNALLOCATED,
    ): PersonManager = PersonManager(
        IdGenerator.getAndIncrement(),
        person.id,
        ReferenceDataGenerator.PERSON_MANAGER_ALLOCATION_REASON,
        staff,
        TeamGenerator.DEFAULT,
        ProbationAreaGenerator.DEFAULT,
    )
}
