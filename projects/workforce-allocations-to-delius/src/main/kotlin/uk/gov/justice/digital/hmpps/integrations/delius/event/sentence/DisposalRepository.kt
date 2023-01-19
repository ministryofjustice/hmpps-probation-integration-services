package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DisposalRepository : JpaRepository<Disposal, Long> {
    @Query(
        """
        select new uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.SentenceWithManager(
            d, mo, s
        ) from Disposal d
        join fetch d.event
        join fetch d.type
        join fetch d.entryLengthUnit.dataset
        join fetch MainOffence mo on d.event.id = mo.event.id
        join fetch mo.offence
        join OrderManager om on d.event.id = om.eventId
        join fetch Staff s on om.staff.id = s.id
        left join fetch s.grade.dataset
        where d.event.person.id = :personId
        and d.event.number <> :eventNumber
    """
    )
    fun findAllSentencesExcludingEventNumber(personId: Long, eventNumber: String): List<SentenceWithManager>
}
