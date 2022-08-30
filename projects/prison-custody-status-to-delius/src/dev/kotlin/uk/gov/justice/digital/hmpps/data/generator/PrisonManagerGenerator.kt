package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.PrisonManager
import java.time.ZonedDateTime

object PrisonManagerGenerator {
    fun generate(
        person: Person
    ) = PrisonManager(
        id = IdGenerator.getAndIncrement(),
        personId = person.id,
        date = ZonedDateTime.now(),
        allocationReason = ReferenceDataGenerator.PERSON_MANAGER_ALLOCATION_REASON,
        staff = StaffGenerator.UNALLOCATED,
        team = TeamGenerator.DEFAULT,
        probationArea = ProbationAreaGenerator.DEFAULT,
    )
}
