package uk.gov.justice.digital.hmpps.integrations.delius.provider

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findByCode(code: String): Staff?

    @Query(
        """
        select case when count(t) > 0 then true else false end
        from Staff s 
        left join fetch Team t
        where s.id = :staffId
        and t.id = :teamId
        """
    )
    fun verifyTeamMembership(staffId: Long, teamId: Long): Boolean

    fun findAllByTeamsCode(teamCode: String): List<Staff>

    @Query(
        """
        select count(1) from offender_manager om 
        join event e on e.offender_id = om.offender_id
        join disposal d on d.event_id = e.event_id
        join custody c on d.disposal_id = c.disposal_id
        join key_date k on c.custody_id = k.custody_id
        join r_standard_reference_list r on k.key_date_type_id = r.standard_reference_list_id
        where om.allocation_staff_id = :staffId 
        and om.end_date is null
        and r.code_value = :keyDateCode
        and k.key_date >= :dateFrom
        and k.soft_deleted = 0 
        and om.soft_deleted = 0
        and e.soft_deleted = 0 and e.active_flag = 1
        and d.soft_deleted = 0 and d.active_flag = 1
        and c.soft_deleted = 0
    """,
        nativeQuery = true
    )
    fun getKeyDateCountByCodeAndStaffId(staffId: Long, keyDateCode: String, dateFrom: LocalDate): Long

    @Query(
        """
       select count(1)
        from offender_manager om 
        join event e on e.offender_id = om.offender_id and e.soft_deleted = 0 and e.active_flag = 1
        join disposal d on d.event_id = e.event_id and d.soft_deleted = 0 and d.active_flag = 1
        join r_disposal_type dt on dt.disposal_type_id = d.disposal_type_id
        left join custody c on d.disposal_id = c.disposal_id and c.soft_deleted = 0
        left join r_standard_reference_list cs on c.custodial_status_id = cs.standard_reference_list_id
        left join key_date kd on c.custody_id = kd.custody_id and kd.soft_deleted = 0
        left join r_standard_reference_list kdt on kd.key_date_type_id = kdt.standard_reference_list_id and
                                  kdt.code_value = 'SED'
        where greatest(nvl(d.notional_end_date, to_date('1970-01-01', 'YYYY-MM-DD')),
            nvl(d.entered_notional_end_date, to_date('1970-01-01', 'YYYY-MM-DD')),
            nvl(kd.key_date, to_date('1970-01-01', 'YYYY-MM-DD'))) >= :dateFrom
            and om.allocation_staff_id = :staffId 
    """,
        nativeQuery = true
    )
    fun getSentencesDueCountByStaffId(staffId: Long, dateFrom: LocalDate): Long

    @Query(
        """
       select 
        count(1) 
        from offender_manager om 
                join event e on e.offender_id = om.offender_id
                join disposal d on d.event_id = e.event_id
                join custody c on d.disposal_id = c.disposal_id
                join Institutional_report i on i.custody_id = c.custody_id
                join r_standard_reference_list r on i.institution_report_type_id = r.standard_reference_list_id
                where om.allocation_staff_id = :staffId
                and om.end_date is null
                and r.code_value = 'PAR'
                and i.date_required >= :dateFrom
                and i.date_completed is null
                and i.soft_deleted = 0 
                and om.soft_deleted = 0
                and e.soft_deleted = 0 and e.active_flag = 1
                and d.soft_deleted = 0 and d.active_flag = 1
                and c.soft_deleted = 0
    """,
        nativeQuery = true
    )
    fun getParoleReportsDueCountByStaffId(staffId: Long, dateFrom: LocalDate): Long
}
