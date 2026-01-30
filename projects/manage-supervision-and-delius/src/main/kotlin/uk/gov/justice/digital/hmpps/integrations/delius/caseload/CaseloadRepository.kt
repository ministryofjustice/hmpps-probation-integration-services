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
        """select * from (with filtered_caseload as (
            select c.offender_id,
            c.trust_provider_team_id
                from caseload c
                join offender p on p.offender_id = c.offender_id
                where c.staff_employee_id   = :staffId
                and c.role_code           = 'OM'
                and c.trust_provider_flag = 0
                and (
            :nameOrCrn is null
            or lower(p.crn) like '%' || :nameOrCrn || '%'
            or lower(p.first_name || ' ' || p.surname) like '%' || :nameOrCrn || '%'
            or lower(p.surname || ' ' || p.first_name) like '%' || :nameOrCrn || '%'
            or lower(p.surname || ', ' || p.first_name) like '%' || :nameOrCrn || '%'
    )
    ),

    /* Sentence stats rewritten exactly as the 11g-safe version */
    sentence_base as (
    select e.offender_id,
    cast(e.event_number as int) as event_no,
    d.disposal_id
    from filtered_caseload fc
    join event e
    on e.offender_id   = fc.offender_id
    and e.active_flag   = 1
    and e.soft_deleted  = 0
    join disposal d
    on d.event_id      = e.event_id
    and d.active_flag   = 1
    and d.soft_deleted  = 0
    ),
    sentence_counts as (
    select offender_id,
    count(*) as total_sentences,
    max(event_no) as max_event_no
    from sentence_base
    group by offender_id
    ),
    sentence_stats as (
    select sc.offender_id,
    sc.total_sentences,
    sb.disposal_id as latest_disposal_id
    from sentence_counts sc
    join sentence_base sb
    on sb.offender_id = sc.offender_id
    and sb.event_no    = sc.max_event_no
    ),

    /* NEXT appointment */
    next_date as (
    select fc.offender_id,
    min(c.contact_date) as next_date
    from filtered_caseload fc
    join contact c on c.offender_id = fc.offender_id
    join r_contact_type ct
    on ct.contact_type_id = c.contact_type_id
    and ct.attendance_contact = 'Y'
    where c.soft_deleted = 0
    and 
    (c.contact_date > trunc(sysdate)
        or (
            c.contact_date = trunc(sysdate)
            and (c.contact_start_time - trunc(c.contact_start_time)) >
           (sysdate - trunc(sysdate))
            ))
    and c.contact_date <= trunc(sysdate) + 1825
    group by fc.offender_id
    ),  
next_time as (
    select nd.offender_id,
           nd.next_date,
           min(c.contact_start_time) as next_time
    from next_date nd
    join contact c
      on c.offender_id  = nd.offender_id
     and c.contact_date = nd.next_date
    join r_contact_type ct
      on ct.contact_type_id = c.contact_type_id
     and ct.attendance_contact = 'Y'
    where c.soft_deleted = 0
      and (
            c.contact_date > trunc(sysdate)
         or (
               c.contact_date = trunc(sysdate)
           and (c.contact_start_time - trunc(c.contact_start_time)) >
               (sysdate - trunc(sysdate))
             )
          )
    group by nd.offender_id, nd.next_date
),
    next_pick as (
    select nt.offender_id,
    min(c.contact_id) as contact_id
    from next_time nt
    join contact c
    on c.offender_id        = nt.offender_id
    and c.contact_date       = nt.next_date
    and c.contact_start_time = nt.next_time
    where c.soft_deleted = 0
    group by nt.offender_id
    ),
    next_app as (
    select c.offender_id,
    c.contact_id,
    c.contact_date,
    c.contact_start_time,
    ct.code        as type_code,
    ct.description as type_description
    from next_pick np
    join contact c on c.contact_id = np.contact_id
    join r_contact_type ct
    on ct.contact_type_id = c.contact_type_id
    )

    /* PREVIOUS appointment */
    ,
    prev_date as (
    select fc.offender_id,
    max(c.contact_date) as prev_date
    from filtered_caseload fc
    join contact c on c.offender_id = fc.offender_id
    join r_contact_type ct
    on ct.contact_type_id = c.contact_type_id
    and ct.attendance_contact = 'Y'
    where c.soft_deleted = 0
    and (
    c.contact_date < trunc(sysdate)
    or (c.contact_date = trunc(sysdate)
    and c.contact_start_time < sysdate)
    )
    and c.contact_date >= trunc(sysdate) - 1825
    group by fc.offender_id
    ),
    prev_time as (
    select pd.offender_id,
    pd.prev_date,
    max(c.contact_start_time) as prev_time
    from prev_date pd
    join contact c
    on c.offender_id  = pd.offender_id
    and c.contact_date = pd.prev_date
    join r_contact_type ct
    on ct.contact_type_id = c.contact_type_id
    and ct.attendance_contact = 'Y'
    where c.soft_deleted = 0
    group by pd.offender_id, pd.prev_date
    ),
    prev_pick as (
    select pt.offender_id,
    max(c.contact_id) as contact_id
    from prev_time pt
    join contact c
    on c.offender_id        = pt.offender_id
    and c.contact_date       = pt.prev_date
    and c.contact_start_time = pt.prev_time
    where c.soft_deleted = 0
    group by pt.offender_id
    ),
    prev_app as (
    select c.offender_id,
    c.contact_id,
    c.contact_date,
    c.contact_start_time,
    ct.code        as type_code,
    ct.description as type_description
    from prev_pick pp
    join contact c on c.contact_id = pp.contact_id
    join r_contact_type ct
    on ct.contact_type_id = c.contact_type_id
    )

    select
    p.offender_id,
    p.crn,
    p.date_of_birth_date            as date_of_birth,
    p.first_name,
    p.second_name,
    p.third_name,
    p.surname,

    rdt.description                 as latest_sentence_type_description,
    coalesce(ss.total_sentences,0)  as total_sentences,

    na.contact_id                   as next_appointment_id,
    (na.contact_date + (na.contact_start_time - trunc(na.contact_start_time)))
    as next_appointment_date_time,
    na.type_description             as next_appointment_type_description,

    pa.contact_id                   as prev_appointment_id,
    (pa.contact_date + (pa.contact_start_time - trunc(pa.contact_start_time)))
    as prev_appointment_date_time,
    pa.type_description             as prev_appointment_type_description,

    t.code                          as team_code

    from filtered_caseload fc
    join offender p on p.offender_id = fc.offender_id
    join team t     on t.team_id     = fc.trust_provider_team_id

    left join sentence_stats ss
    on ss.offender_id = fc.offender_id
    left join disposal d
    on d.disposal_id = ss.latest_disposal_id
    left join r_disposal_type rdt
    on rdt.disposal_type_id = d.disposal_type_id

    left join next_app na
    on na.offender_id = fc.offender_id
    left join prev_app pa
    on pa.offender_id = fc.offender_id

    where (:nextContactCode is null or upper(trim(na.type_code)) = upper(trim(:nextContactCode)))
    and (:sentenceCode   is null or rdt.disposal_type_code = :sentenceCode)
    ) main order by null
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