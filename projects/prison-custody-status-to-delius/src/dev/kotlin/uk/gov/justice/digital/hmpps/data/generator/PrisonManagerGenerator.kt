package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.entity.PrisonManager
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.entity.ProbationArea
import java.time.ZonedDateTime

object PrisonManagerGenerator {
    fun generate(
        person: Person,
        startDate: ZonedDateTime = ZonedDateTime.now().minusDays(1),
        endDate: ZonedDateTime? = null,
        probationArea: ProbationArea = ProbationAreaGenerator.DEFAULT
    ) = PrisonManager(
        id = IdGenerator.getAndIncrement(),
        personId = person.id,
        date = startDate,
        endDate = endDate,
        allocationReason = ReferenceDataGenerator.PERSON_MANAGER_ALLOCATION_REASON,
        staff = StaffGenerator.UNALLOCATED,
        team = TeamGenerator.DEFAULT,
        probationArea = probationArea
    )
}
