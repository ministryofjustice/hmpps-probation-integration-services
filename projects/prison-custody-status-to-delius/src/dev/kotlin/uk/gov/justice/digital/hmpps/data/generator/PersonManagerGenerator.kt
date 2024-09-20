package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.staff.entity.Staff

object PersonManagerGenerator {
    fun generate(
        person: Person,
        staff: Staff = StaffGenerator.UNALLOCATED
    ): PersonManager = PersonManager(
        IdGenerator.getAndIncrement(),
        person,
        ReferenceDataGenerator.PERSON_MANAGER_ALLOCATION_REASON,
        staff,
        TeamGenerator.DEFAULT,
        ProbationAreaGenerator.DEFAULT
    )
}
