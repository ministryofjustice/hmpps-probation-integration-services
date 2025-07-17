package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.sentence.Event

interface EventRepository : JpaRepository<Event, Long> {
    @EntityGraph(
        attributePaths = [
            "person.gender",
            "person.ethnicity",
            "person.manager.staff.user",
            "person.manager.team.localAdminUnit.probationDeliveryUnit",
            "disposal.type",
            "disposal.lengthUnits",
            "disposal.custody",
        ]
    )
    fun findByPersonCrnAndNumber(crn: String, eventNumber: String): Event?
}