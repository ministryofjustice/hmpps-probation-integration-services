package uk.gov.justice.digital.hmpps.integrations.delius.caseload

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.entity.Caseload
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.ContactTypeDetails

interface CaseloadRepository : JpaRepository<Caseload, Long> {
    @Query(
        """select main.*
from ( with filtered_caseload as ( select caseload.*
                                   from caseload
                                   join offender person on caseload.offender_id = person.offender_id
                                   where caseload.staff_employee_id = :staffId
                                     and caseload.role_code = 'OM'
                                     and caseload.trust_provider_flag = 0
                                     and (:nameOrCrn is null or lower(person.crn) like '%' || :nameOrCrn || '%' or
                                          lower(person.first_name || ' ' || person.surname) like
                                          '%' || :nameOrCrn || '%' or
                                          lower(person.surname || ' ' || person.first_name) like
                                          '%' || :nameOrCrn || '%' or
                                          lower(person.surname || ', ' || person.first_name) like
                                          '%' || :nameOrCrn || '%') ),
            next_appointment as ( select *
                                  from ( select c.offender_id,
                                                c.contact_id,
                                                (c.contact_date +
                                                 (c.contact_start_time - trunc(c.contact_start_time)))                                                                   as date_time,
                                                ct.code                                                                                                                  as type_code,
                                                ct.description                                                                                                           as type_description,
                                                row_number() over ( partition by c.offender_id order by c.contact_date asc, c.contact_start_time asc, c.contact_id asc ) as rn
                                         from contact c
                                         join r_contact_type ct on ct.contact_type_id = c.contact_type_id
                                         join filtered_caseload fc on fc.offender_id = c.offender_id
                                            where c.soft_deleted = 0
                                              and ct.attendance_contact = 'Y'
                                              and c.contact_date <= trunc(sysdate) + 1825
                                              and (
                                                    c.contact_date > trunc(sysdate)
                                                 or (
                                                      c.contact_date = trunc(sysdate)
                                                      and (c.contact_start_time - trunc(c.contact_start_time))
                                                          > (sysdate - trunc(sysdate))
                                                    )
                                                  )
                                             )
                                  where rn = 1 ),
            prev_appointment as ( select *
                                  from ( select c.offender_id,
                                                c.contact_id,
                                                (c.contact_date +
                                                 (c.contact_start_time - trunc(c.contact_start_time)))                                                                      as date_time,
                                                ct.code                                                                                                                     as type_code,
                                                ct.description                                                                                                              as type_description,
                                                row_number() over ( partition by c.offender_id order by c.contact_date desc, c.contact_start_time desc, c.contact_id desc ) as rn
                                         from contact c
                                         join r_contact_type ct on ct.contact_type_id = c.contact_type_id
                                         join filtered_caseload fc on fc.offender_id = c.offender_id
                                         where c.soft_deleted = 0
                                           and ct.attendance_contact = 'Y'
                                           and c.contact_date >= trunc(sysdate) - 1825
                                            and (
                                                  c.contact_date < trunc(sysdate)
                                               or (
                                                    c.contact_date = trunc(sysdate)
                                                    and (c.contact_start_time - trunc(c.contact_start_time))
                                                        < (sysdate - trunc(sysdate))
                                                  )
                                                )
                                             )
                                  where rn = 1 ),
            all_sentences as ( select e.offender_id, cast(e.event_number as int) as event_number, d.disposal_id
                               from filtered_caseload fc
                               join event e
                                    on e.offender_id = fc.offender_id and e.active_flag = 1 and e.soft_deleted = 0
                               join disposal d
                                    on d.event_id = e.event_id and d.active_flag = 1 and d.soft_deleted = 0 ),
            sentence_counts as ( select offender_id, count(*) as total_sentences, max(event_number) as max_event_number
                                 from all_sentences
                                 group by offender_id ),
            sentence_stats as ( select sc.offender_id, sc.total_sentences, sb.disposal_id as latest_disposal_id
                                from sentence_counts sc
                                join all_sentences sb
                                     on sb.offender_id = sc.offender_id and sb.event_number = sc.max_event_number )

       select person.offender_id                          as offender_id,
              person.crn                                  as crn,
              person.date_of_birth_date                   as date_of_birth,
              person.first_name                           as first_name,
              person.second_name                          as second_name,
              person.third_name                           as third_name,
              person.surname                              as surname,
              r_disposal_type.description                 as latest_sentence_type_description,
              coalesce(sentence_stats.total_sentences, 0) as total_sentences,
              next_appointment.contact_id                 as next_appointment_id,
              next_appointment.date_time                  as next_appointment_date_time,
              next_appointment.type_description           as next_appointment_type_description,
              prev_appointment.contact_id                 as prev_appointment_id,
              prev_appointment.date_time                  as prev_appointment_date_time,
              prev_appointment.type_description           as prev_appointment_type_description,
              team.code                                   as team_code
       from filtered_caseload
       join team on team.team_id = filtered_caseload.trust_provider_team_id
       join offender person on filtered_caseload.offender_id = person.offender_id
       left join next_appointment on filtered_caseload.offender_id = next_appointment.offender_id
       left join prev_appointment on filtered_caseload.offender_id = prev_appointment.offender_id
       left join sentence_stats on filtered_caseload.offender_id = sentence_stats.offender_id
       left join disposal on sentence_stats.latest_disposal_id = disposal.disposal_id
       left join r_disposal_type on disposal.disposal_type_id = r_disposal_type.disposal_type_id
       where (:nextContactCode is null or next_appointment.type_code = :nextContactCode)
         and (:sentenceCode is null or r_disposal_type.disposal_type_code = :sentenceCode) ) main
order by null
    """,
        countQuery = """
        select count(*)
        from caseload
        join offender person on caseload.offender_id = person.offender_id
        left join event e on e.offender_id = caseload.offender_id and e.active_flag = 1 and e.soft_deleted = 0
        left join disposal d on d.event_id = e.event_id and d.active_flag = 1 and d.soft_deleted = 0
        left join r_disposal_type on d.disposal_type_id = r_disposal_type.disposal_type_id
        left join contact c on c.offender_id = caseload.offender_id and c.soft_deleted = 0
        left join r_contact_type ct on ct.contact_type_id = c.contact_type_id and ct.attendance_contact = 'Y'
        where caseload.staff_employee_id = :staffId
          and caseload.role_code = 'OM'
          and caseload.trust_provider_flag = 0
          and (:nameOrCrn is null or lower(person.crn) like '%' || :nameOrCrn || '%' or
               lower(person.first_name || ' ' || person.surname) like '%' || :nameOrCrn || '%' or
               lower(person.surname || ' ' || person.first_name) like '%' || :nameOrCrn || '%' or
               lower(person.surname || ', ' || person.first_name) like '%' || :nameOrCrn || '%')
          and (:nextContactCode is null or ct.code = :nextContactCode)
          and (:sentenceCode is null or r_disposal_type.disposal_type_code = :sentenceCode)
    """,
        nativeQuery = true
    )
    fun searchByStaffId(
        pageable: Pageable,
        staffId: Long,
        nameOrCrn: String? = null,
        nextContactCode: String? = null,
        sentenceCode: String? = null,
    ): Page<CaseloadItem>

    @Query(
        """
            with filtered_caseload as ( select caseload.*
                                        from caseload
                                        join offender person on caseload.offender_id = person.offender_id
                                        join team on team.team_id = caseload.trust_provider_team_id 
                                        where team.code = :teamCode
                                          and caseload.role_code = 'OM'
                                          and caseload.trust_provider_flag = 0)
            select person.crn         as crn,
                   person.first_name  as first_name,
                   person.second_name as second_name,
                   person.third_name  as third_name,
                   person.surname     as surname,
                   staff.forename     as staff_forename,
                   staff.surname      as staff_surname,
                   staff.officer_code as staff_code
            from filtered_caseload
            join team on team.code = :teamCode
            join staff on staff.staff_id = filtered_caseload.staff_employee_id
            join offender person on filtered_caseload.offender_id = person.offender_id
        """,
        nativeQuery = true
    )
    fun findByTeamCode(teamCode: String, pageable: Pageable): Page<TeamCaseloadItem>

    @Query(
        """
            select distinct code, description
            from ( select code, description, row_number() over (partition by offender_id order by date_time asc) as row_num
                   from ( select ct.code,
                                 ct.description,
                                 c.offender_id,
                                 trunc(c.contact_date) + (c.contact_start_time - trunc(c.contact_start_time)) as date_time
                          from caseload cl
                          join contact c on c.offender_id = cl.offender_id and c.staff_id = cl.staff_employee_id and
                                            c.contact_start_time is not null and c.soft_deleted = 0
                          join r_contact_type ct on ct.contact_type_id = c.contact_type_id and ct.attendance_contact = 'Y'
                          where cl.role_code = 'OM'
                            and cl.staff_employee_id = :id
                            and trunc(c.contact_date) + (c.contact_start_time - trunc(c.contact_start_time)) > sysdate ) )
            where row_num = 1
            order by description
        """, nativeQuery = true
    )
    fun findContactTypesForStaff(id: Long): List<ContactTypeDetails>

    @Query(
        """
            select distinct e.disposal.type from Caseload c
            join Event e on e.personId = c.person.id and e.active = true and e.softDeleted = false 
            where e.disposal is not null 
            and c.staff.id = :id
            and c.roleCode = 'OM'
            order by e.disposal.type.description asc
        """
    )
    fun findSentenceTypesForStaff(id: Long): List<DisposalType>
}