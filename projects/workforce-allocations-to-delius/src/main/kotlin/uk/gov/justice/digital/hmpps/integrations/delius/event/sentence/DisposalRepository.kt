package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DisposalRepository : JpaRepository<Disposal, Long> {
    @Query(
        """
        select new uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.SentenceWithManager(
            d, mo, s
        ) from Disposal d
        join fetch d.event e
        join fetch d.type
        join fetch d.entryLengthUnit.dataset
        join fetch MainOffence mo on e.id = mo.event.id
        join fetch mo.offence
        join OrderManager om on e.id = om.eventId
        join fetch Staff s on om.staff.id = s.id
        left join fetch s.grade.dataset
        where e.person.id = :personId
        and e.number <> :eventNumber
        and e.softDeleted = false and d.softDeleted = false
        and om.active = true
    """
    )
    fun findAllSentencesExcludingEventNumber(personId: Long, eventNumber: String): List<SentenceWithManager>
}
