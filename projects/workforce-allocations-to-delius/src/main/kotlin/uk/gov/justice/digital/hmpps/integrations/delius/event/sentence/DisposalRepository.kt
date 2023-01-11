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
        join fetch s.grade.dataset
        where d.event.person.id = :personId
        and d.event.number <> :eventNumber
    """
    )
    fun findAllSentencesExcludingEventNumber(personId: Long, eventNumber: String): List<SentenceWithManager>

    @Query(
        """
        select
            e.event_id as eventId,
            d.disposal_date as startDate,
            dt.description as description,
            d.entry_length || ' ' || u.code_description as length,
            greatest(nvl(d.notional_end_date, to_date('1970-01-01', 'YYYY-MM-DD')),
                     nvl(d.entered_notional_end_date, to_date('1970-01-01', 'YYYY-MM-DD')),
                     nvl(kd.key_date, to_date('1970-01-01', 'YYYY-MM-DD'))) as endDate,
            o.MAIN_CATEGORY_DESCRIPTION offenceMainCategory,
            o.SUB_CATEGORY_DESCRIPTION offenceSubCategory
        from offender_manager om
                 join event e on e.offender_id = om.offender_id and e.soft_deleted = 0 and e.active_flag = 1
                 join disposal d on d.event_id = e.event_id and d.soft_deleted = 0 and d.active_flag = 1
                 join r_disposal_type dt on dt.disposal_type_id = d.disposal_type_id
                 join main_offence mo on mo.event_id = e.event_id and mo.soft_deleted = 0
                 join r_offence o ON o.offence_id = mo.offence_id
                 left join r_standard_reference_list u on d.entry_length_units_id = u.standard_reference_list_id
                 left join custody c on d.disposal_id = c.disposal_id and c.soft_deleted = 0
                 left join key_date kd on c.custody_id = kd.custody_id and kd.soft_deleted = 0
                 left join r_standard_reference_list kdt on kd.key_date_type_id = kdt.standard_reference_list_id and
                                                            kdt.code_value = 'SED'
        where  e.offender_id = :personId AND e.event_number = :eventNumber
    """,
        nativeQuery = true
    )
    fun findSentenceSummary(personId: Long, eventNumber: String): SentenceSummary?
}
