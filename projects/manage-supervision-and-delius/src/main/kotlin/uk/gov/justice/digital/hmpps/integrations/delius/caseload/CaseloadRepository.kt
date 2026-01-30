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
        """           
/*+ INLINE */
with fc as (
    select /*+ CARDINALITY(fc 6) */ c.*
    from caseload c
    where c.staff_employee_id   = :staffId
      and c.role_code           = 'OM'
      and c.trust_provider_flag = 0
),
     filtered_offenders as (
         select fc.*
         from fc
         join offender p on p.offender_id = fc.offender_id
         where (:nameOrCrn is null
             or lower(p.crn) like '%' || :nameOrCrn || '%'
             or lower(p.first_name || ' ' || p.surname)  like '%' || :nameOrCrn || '%'
             or lower(p.surname    || ' ' || p.first_name) like '%' || :nameOrCrn || '%'
             or lower(p.surname    || ', ' || p.first_name) like '%' || :nameOrCrn || '%')
     ),
     apps as (
         select c.offender_id,
                c.contact_id,
                c.contact_date,
                c.contact_start_time,
                trunc(c.contact_date) + (c.contact_start_time - trunc(c.contact_start_time)) as appointment_datetime,
                ct.code        as type_code,
                ct.description as type_description
         from contact c
         join r_contact_type ct
              on ct.contact_type_id = c.contact_type_id
                  and ct.attendance_contact = 'Y'
         join filtered_offenders fo on fo.offender_id = c.offender_id
         where c.soft_deleted = 0
           and c.contact_date between trunc(sysdate) - 1825 and trunc(sysdate) + 1825
     ),
     agg_next as (
         select offender_id,
                min(contact_id)        keep (dense_rank first order by appointment_datetime)        as next_appointment_id,
                min(appointment_datetime) keep (dense_rank first order by appointment_datetime)     as next_appointment_date_time,
                min(type_description)   keep (dense_rank first order by appointment_datetime)       as next_appointment_type_description
         from apps
         where appointment_datetime > sysdate
         group by offender_id
     ),
     agg_prev as (
         select offender_id,
                min(contact_id)        keep (dense_rank first order by appointment_datetime desc)   as prev_appointment_id,
                min(appointment_datetime) keep (dense_rank first order by appointment_datetime desc) as prev_appointment_date_time,
                min(type_description)   keep (dense_rank first order by appointment_datetime desc)   as prev_appointment_type_description
         from apps
         where appointment_datetime < sysdate
         group by offender_id
     ),
     sentence_stats as (
         select offender_id, total_sentences, disposal_id as latest_disposal_id
         from (
             select e.offender_id,
                    d.disposal_id,
                    count(e.event_id) over (partition by e.offender_id)                             as total_sentences,
                    row_number() over (partition by e.offender_id order by to_number(e.event_number) desc) as rn
             from event e
             join disposal d on d.event_id = e.event_id
                 and d.active_flag   = 1
                 and d.soft_deleted  = 0
             where e.active_flag  = 1
               and e.soft_deleted = 0
         )
         where rn = 1
     )
select p.offender_id,
       p.crn,
       p.date_of_birth_date                   as date_of_birth,
       p.first_name,
       p.second_name,
       p.third_name,
       p.surname,
       rdt.description                        as latest_sentence_type_description,
       nvl(ss.total_sentences, 0)             as total_sentences,
       an.next_appointment_id,
       an.next_appointment_date_time,
       an.next_appointment_type_description,
       ap.prev_appointment_id,
       ap.prev_appointment_date_time,
       ap.prev_appointment_type_description,
       t.code                                  as team_code
from filtered_offenders fo
join offender p        on p.offender_id = fo.offender_id
join team t            on t.team_id     = fo.trust_provider_team_id
left join agg_next an  on an.offender_id = fo.offender_id
left join agg_prev ap  on ap.offender_id = fo.offender_id
left join sentence_stats ss on ss.offender_id = fo.offender_id
left join disposal d        on d.disposal_id  = ss.latest_disposal_id
left join r_disposal_type rdt on rdt.disposal_type_id = d.disposal_type_id
where (:nextContactCode is null or an.next_appointment_id is null or exists (
    select 1 from apps a
    where a.offender_id = fo.offender_id
      and a.contact_id  = an.next_appointment_id
      and a.type_code   = :nextContactCode
))
  and (:sentenceCode is null or rdt.disposal_type_code = :sentenceCode)
order by null;
        """,
        countQuery = """
            with filtered_caseload as ( select caseload.*
                                        from caseload
                                        join offender person on caseload.offender_id = person.offender_id
                                        where caseload.staff_employee_id = :staffId
                                          and caseload.role_code = 'OM'
                                          and caseload.trust_provider_flag = 0
                                          and (:nameOrCrn is null or lower(person.crn) like '%' || :nameOrCrn || '%' or
                                               lower(person.first_name || ' ' || person.surname) like '%' || :nameOrCrn || '%' or
                                               lower(person.surname || ' ' || person.first_name) like '%' || :nameOrCrn || '%' or
                                               lower(person.surname || ', ' || person.first_name) like '%' || :nameOrCrn || '%') ),
                 appointments as ( select contact.contact_id                                               as contact_id,
                                          contact.contact_date                                             as contact_date,
                                          contact.offender_id                                              as offender_id,
                                          contact.staff_id                                                 as staff_id,
                                          r_contact_type.code                                              as type_code,
                                          r_contact_type.description                                       as type_description,
                                          trunc(contact.contact_date) +
                                          (contact.contact_start_time - trunc(contact.contact_start_time)) as appointment_datetime
                                   from contact
                                   join r_contact_type on contact.contact_type_id = r_contact_type.contact_type_id and
                                                          r_contact_type.attendance_contact = 'Y'
                                   join filtered_caseload on filtered_caseload.offender_id = contact.offender_id and
                                                             filtered_caseload.staff_employee_id = contact.staff_id
                                   where contact_date is not null
                                     and staff_id = :staffId
                                     and contact_start_time is not null
                                     and contact_date between sysdate - 1825 and sysdate + 1825
                                     and soft_deleted = 0 ),
                 future_appointments as ( select min(appointments.contact_id)           as contact_id,
                                                 min(appointments.appointment_datetime) as appointment_datetime,
                                                 offender_id,
                                                 staff_id,
                                                 type_code,
                                                 type_description
                                          from appointments
                                          where contact_date between sysdate - 1 and sysdate + 1825
                                          group by offender_id, staff_id, type_code, type_description )
            select count(*)
            from filtered_caseload
            left join ( select future_appointments.*,
                               row_number() over (partition by offender_id order by appointment_datetime) as row_num
                        from future_appointments
                        where appointment_datetime > sysdate ) next_appointment
                      on next_appointment.offender_id = filtered_caseload.offender_id and next_appointment.row_num = 1
            join ( select offender_id, disposal_id as latest_disposal_id
                   from ( select event.offender_id,
                                 disposal.disposal_id,
                                 row_number() over (partition by event.offender_id order by event.event_number desc) as row_num
                          from event
                          join disposal
                               on disposal.event_id = event.event_id and disposal.active_flag = 1 and disposal.soft_deleted = 0
                          where event.soft_deleted = 0
                            and event.active_flag = 1 ) sentences
                   where sentences.row_num = 1 ) sentence_stats on sentence_stats.offender_id = filtered_caseload.offender_id
            left join disposal on disposal.disposal_id = sentence_stats.latest_disposal_id
            left join r_disposal_type on r_disposal_type.disposal_type_id = disposal.disposal_type_id
            where (:nextContactCode is null or next_appointment.type_code = :nextContactCode)
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