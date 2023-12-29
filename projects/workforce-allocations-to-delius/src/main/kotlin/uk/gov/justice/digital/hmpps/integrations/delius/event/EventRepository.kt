package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.api.model.Offence
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface EventRepository : JpaRepository<Event, Long> {

    @Query(
        """
        SELECT COUNT(ot) FROM OrderTransfer ot
        JOIN ReferenceData status ON ot.statusId = status.id 
        WHERE ot.eventId = :eventId 
        AND status.code = 'PN'
        AND ot.softDeleted = false
    """
    )
    fun countPendingTransfers(eventId: Long): Int

    @Modifying
    @Query(
        """
    merge into iaps_event using dual on (event_id = ?1) 
    when matched then update set iaps_flag=?2 
    when not matched then insert(event_id, iaps_flag) values(?1,?2)
    """,
        nativeQuery = true
    )
    fun updateIaps(eventId: Long, iapsFlagValue: Long = 1)

    fun findByPersonCrnAndNumberAndSoftDeletedFalse(crn: String, number: String): Event?

    @Query(
        """
            select mo.offence.description as mainCategory, true as mainOffence from MainOffence mo 
            where mo.event.id = :eventId
            union all
            select ao.offence.description as mainCategory, false as mainOffence from AdditionalOffence ao
            where ao.event.id = :eventId
        """
    )
    fun findAllOffencesByEventId(eventId: Long): List<Offence>

    @Query(
        """
        select count(r) 
        from Requirement r
        left join r.mainCategory mc
        left join r.subCategory sc
        left join r.additionalMainCategory amc
        where r.disposal.event.id = :eventId
        and (mc.code = 'RM38' 
            or (mc.code = '7' and (sc is null or sc.code <> 'RS66'))
            or (amc.code in ('RM38', '7')))
        and r.active = true and r.softDeleted = false
    """
    )
    fun countAccreditedProgrammeRequirements(eventId: Long): Int
}

fun EventRepository.getByPersonCrnAndNumber(crn: String, number: String) =
    findByPersonCrnAndNumberAndSoftDeletedFalse(crn, number)
        ?: throw NotFoundException("Event $number not found for crn $crn")
