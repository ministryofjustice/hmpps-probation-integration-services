package uk.gov.justice.digital.hmpps.service.event

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface EventRepository : JpaRepository<Event, Long> {

    @Query(
        """
        select count(c) from Event e
        join e.disposal d 
        join d.custody c
        where e.person.crn = :crn
        and e.disposal.custody.status.code in ('A','C','D','R')
        """
    )
    fun countCustodySentences(crn: String): Int
}

fun EventRepository.isInCustody(crn: String) = countCustodySentences(crn) > 0
