package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.appointment.Contact

interface ContactRepository : JpaRepository<Contact, Long> {
    @Query(
        """
        select c from Contact c
        where c.personId = :personId
          and c.type.attendance = true
          and (trunc(c.date, day) > trunc(local_date, day) or
               trunc(c.date, day) = trunc(local_date, day) and to_char(c.startTime, 'HH24:MI') >= to_char(local_time, 'HH24:MI'))
        order by c.date asc, c.startTime asc
        """
    )
    @EntityGraph(attributePaths = ["type", "staff.user", "location"])
    fun findFutureAppointments(personId: Long, pageable: Pageable): Page<Contact>

    @Query(
        """
        select c from Contact c
        where c.personId = :personId
          and c.type.attendance = true
          and (trunc(c.date, day) < trunc(local_date, day) or
               trunc(c.date, day) = trunc(local_date, day) and to_char(c.startTime, 'HH24:MI') < to_char(local_time, 'HH24:MI'))
        order by c.date desc, c.startTime desc
        """
    )
    @EntityGraph(attributePaths = ["type", "staff.user", "location"])
    fun findPastAppointments(personId: Long, pageable: Pageable): Page<Contact>

    @Query(
        """
        select count(distinct c.date)
        from Contact c
        join NonStatutoryIntervention nsi on nsi.id = c.nsiId
        where nsi.requirementId = :requirementId
        and c.rarActivity = true
        and c.attended = true
        and c.complied = true
        """
    )
    fun countRarDaysAttended(requirementId: Long): Int
}
