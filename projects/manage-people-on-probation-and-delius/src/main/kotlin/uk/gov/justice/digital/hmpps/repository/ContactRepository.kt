package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.contact.Contact

interface ContactRepository : JpaRepository<Contact, Long> {
    @Query(
        """
        select c from Contact c
        join fetch Caseload caseload on caseload.person.id = c.person.id and caseload.staff.id = :staffId
        where c.staff.id = :staffId
          and c.outcome is null
          and c.type.attendance = true
          and (trunc(c.date, day) > trunc(local_date, day) or
               trunc(c.date, day) = trunc(local_date, day) and to_char(c.startTime, 'HH24:MI') > to_char(local_time, 'HH24:MI'))
        order by c.date asc, c.startTime asc
        """
    )
    @EntityGraph(attributePaths = ["person", "requirement.mainCategory", "type", "staff", "location"])
    fun findNextFiveAppointments(staffId: Long, pageable: Pageable = PageRequest.of(0, 5)): Page<Contact>

    @Query(
        """
        select c from Contact c
        join fetch Caseload caseload on caseload.person.id = c.person.id and caseload.staff.id = :staffId
        where c.staff.id = :staffId
          and c.outcome is null
          and c.type.outcomeRequired = true
          and c.type.attendance = true
          and (trunc(c.date, day) < trunc(local_date, day) or
               trunc(c.date, day) = trunc(local_date, day) and to_char(c.startTime, 'HH24:MI') < to_char(local_time, 'HH24:MI'))
        order by c.date desc, c.startTime desc
        """
    )
    @EntityGraph(attributePaths = ["person", "requirement.mainCategory", "type", "staff", "location"])
    fun findLastFiveAppointmentsRequiringAnOutcome(
        staffId: Long,
        pageable: Pageable = PageRequest.of(0, 5)
    ): Page<Contact>
}
