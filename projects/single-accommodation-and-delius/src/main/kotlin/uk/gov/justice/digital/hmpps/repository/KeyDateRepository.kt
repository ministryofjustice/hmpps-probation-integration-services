package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.event.KeyDate
import uk.gov.justice.digital.hmpps.entity.event.PersonIdAndDate
import java.time.LocalDate

interface KeyDateRepository : JpaRepository<KeyDate, Long> {
    @Query(
        """
        select kd.date from KeyDate kd
        join kd.custody c
        join c.disposal d
        join d.event e
        where e.personId = :personId
        and kd.type.code = 'EXP'
        order by kd.date desc
    	"""
    )
    fun findExpectedReleaseDates(personId: Long, pageRequest: PageRequest = PageRequest.of(0, 1)): LocalDate?

    @Query(
        """
        select e.personId as personId, max(kd.date) as releaseDate 
        from Event e
        join KeyDate kd on e.id = kd.custody.disposal.event.id
        where e.personId in :personIds
        and kd.type.code = 'EXP'
        group by e.personId
        """
    )
    fun findExpectedReleaseDatesByPersonIdIn(personIds: List<Long>): List<PersonIdAndDate>
}