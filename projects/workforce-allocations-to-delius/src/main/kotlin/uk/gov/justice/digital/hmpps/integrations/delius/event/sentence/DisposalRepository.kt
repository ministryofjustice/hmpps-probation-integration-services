package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DisposalRepository : JpaRepository<Disposal, Long> {
    @Query(
        """
        select new uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.SentenceWithManager(
            d, mo, s
        ) from Disposal d
        join MainOffence mo on d.event.id = mo.event.id
        join OrderManager om on d.event.id = om.eventId
        join Staff s on om.staff.id = s.id
        where d.event.person.id = :personId
        and d.event.number <> :eventNumber
    """
    )
    fun findAllSentencesWithManagers(personId: Long, eventNumber: String): List<SentenceWithManager>
}
