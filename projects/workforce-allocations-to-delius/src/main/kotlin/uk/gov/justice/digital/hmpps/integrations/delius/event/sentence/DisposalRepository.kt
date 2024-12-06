package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.api.model.ActiveEvent
import uk.gov.justice.digital.hmpps.api.model.AllocationDemandSentence

interface DisposalRepository : JpaRepository<Disposal, Long> {
    @Query(
        """
        select new uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.SentenceWithManager(
            d, mo, s
        ) from Disposal d
        join fetch d.event e
        join fetch d.type
        join fetch d.entryLengthUnit elu
        join fetch elu.dataset
        join fetch MainOffence mo on e.id = mo.event.id
        join fetch mo.offence
        join OrderManager om on e.id = om.eventId
        join fetch Staff s on om.staff.id = s.id
        left join fetch s.grade sg
        left join fetch sg.dataset
        where e.person.id = :personId
        and e.number <> :eventNumber
        and e.softDeleted = false and d.softDeleted = false
        and om.active = true
    """
    )
    fun findAllSentencesExcludingEventNumber(personId: Long, eventNumber: String): List<SentenceWithManager>

    @Query(
        """
        select new uk.gov.justice.digital.hmpps.api.model.ActiveEvent(
            e.number, om.team.code, om.provider.code
        ) from Disposal d
        join d.event e
        join OrderManager om on e.id = om.eventId
        join Staff s on om.staff.id = s.id
        where e.person.id = :personId
        and e.softDeleted = false and d.softDeleted = false
        and e.active = true and d.active = true
        and om.active = true
        and s.code like '%U'
    """
    )
    fun findAllUnallocatedActiveEvents(personId: Long): List<ActiveEvent>

    @Query(
        """
        select new uk.gov.justice.digital.hmpps.api.model.AllocationDemandSentence(
            d.type.description, d.type.sentenceType, d.date, d.entryLength, d.entryLengthUnit.description
        ) from Disposal d
        join d.event e
        join d.type
        join d.entryLengthUnit.dataset
        where e.person.id = :personId
        and e.number = :eventNumber
        and e.softDeleted = false and d.softDeleted = false
    """
    )
    fun findSentenceForEventNumberAndPersonId(personId: Long, eventNumber: String): AllocationDemandSentence
}
