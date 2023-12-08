package uk.gov.justice.digital.hmpps.integrations.delius.provider

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

interface StaffRepository : JpaRepository<StaffRecord, Long> {
    @EntityGraph(attributePaths = ["grade.dataset", "user"])
    @Query("select s from StaffWithUser s where s.code = :code")
    fun findStaffWithUserByCode(code: String): StaffWithUser?

    @EntityGraph(attributePaths = ["grade.dataset"])
    fun findAllByCodeIn(codes: List<String>): List<Staff>

    @EntityGraph(attributePaths = ["grade.dataset", "user"])
    @Query("select s from StaffWithUser s where upper(s.user.username) = upper(:username)")
    fun findStaffWithUserByUsername(username: String): StaffWithUser?

    @EntityGraph(attributePaths = ["grade.dataset"])
    fun findByCode(code: String): Staff?

    @Query(
        """
        select count(team_id)
        from staff_team
        where staff_id = :staffId and team_id = :teamId
        """,
        nativeQuery = true,
    )
    fun countTeamMembership(
        staffId: Long,
        teamId: Long,
    ): Int

    @Query("select s from StaffWithUser s join s.teams t where t.code = :teamCode and (s.endDate is null or s.endDate > current_date)")
    fun findActiveStaffInTeam(teamCode: String): List<StaffWithUser>

    @Query(
        """
        select count(1) from offender_manager om 
        join event e on e.offender_id = om.offender_id
        join disposal d on d.event_id = e.event_id
        join custody c on d.disposal_id = c.disposal_id
        join key_date k on c.custody_id = k.custody_id
        join r_standard_reference_list r on k.key_date_type_id = r.standard_reference_list_id
        where om.allocation_staff_id = :staffId 
        and om.active_flag = 1
        and r.code_value = :keyDateCode
        and k.key_date <= :toDate
        and k.soft_deleted = 0 
        and om.soft_deleted = 0
        and e.soft_deleted = 0 and e.active_flag = 1
        and d.soft_deleted = 0 and d.active_flag = 1
        and c.soft_deleted = 0
    """,
        nativeQuery = true,
    )
    fun getKeyDateCountByCodeAndStaffId(
        staffId: Long,
        keyDateCode: String,
        toDate: LocalDate,
    ): Long

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
            nvl(kd.key_date, to_date('1970-01-01', 'YYYY-MM-DD'))) <= :toDate
            and om.allocation_staff_id = :staffId 
            and om.active_flag = 1
    """,
        nativeQuery = true,
    )
    fun getSentencesDueCountByStaffId(
        staffId: Long,
        toDate: LocalDate,
    ): Long

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
                and om.active_flag = 1
                and r.code_value = 'PAR'
                and i.date_required <= :toDate
                and i.date_completed is null
                and i.soft_deleted = 0 
                and om.soft_deleted = 0
                and e.soft_deleted = 0 and e.active_flag = 1
                and d.soft_deleted = 0 and d.active_flag = 1
                and c.soft_deleted = 0
    """,
        nativeQuery = true,
    )
    fun getParoleReportsDueCountByStaffId(
        staffId: Long,
        toDate: LocalDate,
    ): Long
}

fun StaffRepository.getWithUserByCode(code: String): StaffWithUser =
    findStaffWithUserByCode(code) ?: throw NotFoundException("Staff", "code", code)

fun StaffRepository.getByCode(code: String): Staff =
    findByCode(code) ?: throw NotFoundException("Staff", "code", code)

fun StaffRepository.verifyTeamMembership(
    staffId: Long,
    teamId: Long,
) = countTeamMembership(staffId, teamId) > 0
