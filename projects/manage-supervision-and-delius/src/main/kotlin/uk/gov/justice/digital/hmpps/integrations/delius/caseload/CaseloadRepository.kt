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
            select main.*
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
                        past_appointments as ( select max(appointments.contact_id)           as contact_id,
                                                      max(appointments.appointment_datetime) as appointment_datetime,
                                                      offender_id,
                                                      staff_id,
                                                      type_code,
                                                      type_description
                                               from appointments
                                               where contact_date between sysdate - 1825 and sysdate + 1
                                               group by offender_id, staff_id, type_code, type_description ),
                        future_appointments as ( select min(appointments.contact_id)           as contact_id,
                                                        min(appointments.appointment_datetime) as appointment_datetime,
                                                        offender_id,
                                                        staff_id,
                                                        type_code,
                                                        type_description
                                                 from appointments
                                                 where contact_date between sysdate - 1 and sysdate + 1825
                                                 group by offender_id, staff_id, type_code, type_description )
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
                          next_appointment.appointment_datetime       as next_appointment_date_time,
                          next_appointment.type_description           as next_appointment_type_description,
                          prev_appointment.contact_id                 as prev_appointment_id,
                          prev_appointment.appointment_datetime       as prev_appointment_date_time,
                          prev_appointment.type_description           as prev_appointment_type_description,
                          team.code                                   as team_code
                   from filtered_caseload
                   join team on team.team_id = filtered_caseload.trust_provider_team_id
                   join offender person on filtered_caseload.offender_id = person.offender_id
                   left join ( select future_appointments.*,
                                      row_number() over (partition by offender_id order by appointment_datetime) as row_num
                               from future_appointments
                               where appointment_datetime > sysdate ) next_appointment
                             on next_appointment.offender_id = filtered_caseload.offender_id and
                                next_appointment.row_num = 1
                   left join ( select past_appointments.*,
                                      row_number() over (partition by offender_id order by appointment_datetime desc) as row_num
                               from past_appointments
                               where appointment_datetime < sysdate ) prev_appointment
                             on prev_appointment.offender_id = filtered_caseload.offender_id and
                                prev_appointment.row_num = 1
                   left join ( select offender_id, total_sentences, disposal_id as latest_disposal_id
                               from ( select event.offender_id,
                                             disposal.disposal_id,
                                             count(event.event_id) over (partition by event.offender_id)                                         as total_sentences,
                                             row_number() over (partition by event.offender_id order by cast(event.event_number as number) desc) as row_num
                                      from event
                                      join disposal on disposal.event_id = event.event_id and disposal.active_flag = 1 and
                                                       disposal.soft_deleted = 0
                                      where event.soft_deleted = 0
                                        and event.active_flag = 1 ) sentences
                               where sentences.row_num = 1 ) sentence_stats
                             on sentence_stats.offender_id = filtered_caseload.offender_id
                   left join disposal on disposal.disposal_id = sentence_stats.latest_disposal_id
                   left join r_disposal_type on r_disposal_type.disposal_type_id = disposal.disposal_type_id
                   where (:nextContactCode is null or next_appointment.type_code = :nextContactCode)
                     and (:sentenceCode is null or r_disposal_type.disposal_type_code = :sentenceCode) ) main
            order by null
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
                      on next_appointment.offender_id = filtered_caseload.offender_id and
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