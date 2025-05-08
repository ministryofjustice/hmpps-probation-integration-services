with next as (select nvl(offender_id, :sql_last_value + :batch_size) sql_next_value
              from offender
              where soft_deleted = 0
                and offender_id >= :sql_last_value + :batch_size
                and exists (select 1 from event where event.offender_id = offender.offender_id and event.active_flag = 1 and event.soft_deleted = 0)
                and (select count(*) from contact where contact.offender_id = offender.offender_id and contact.soft_deleted = 0) > 1000
              order by offender_id fetch next 1 row only)
select "json",
       "contactId",
       (select sql_next_value from next) as "sql_next_value"
from (with page as (select contact.*
                    from contact
                    join (select offender_id
                          from contact
                          where soft_deleted = 0
                            and offender_id >= :sql_last_value
                            and offender_id < :sql_last_value + :batch_size
                          group by offender_id
                          having count(*) > 1000
                             and exists (select 1
                                         from event
                                         where event.offender_id = contact.offender_id
                                           and event.active_flag = 1
                                           and event.soft_deleted = 0)
                          order by offender_id fetch next :batch_size rows only) offender
                         on offender.offender_id = contact.offender_id
                    where :contact_id = 0
                    order by contact_id),
           single as (select * from contact where :contact_id > 0 and contact.contact_id = :contact_id)
      select json_object(
                     'crn' value offender.crn,
                     'offenderId' value offender.offender_id,
                     'id' value contact.contact_id,
                     'date' value to_char(contact.contact_date, 'yyyy-mm-dd'),
                     'startTime' value to_char(contact.contact_start_time, 'HH24:MI:SS'),
                     'endTime' value to_char(contact.contact_end_time, 'HH24:MI:SS'),
                     'description' value contact.description,
                     'notes' value contact.notes,
                     'attended' value decode(contact.attended, 'Y', 'attended', 'N', 'fta', null),
                     'complied' value decode(contact.complied, 'Y', 'complied', 'N', 'ftc', null),
                     'startDateTime' value decode(contact.contact_start_time, null, to_char(contact.contact_date, 'yyyy-mm-dd') || 'T00:00:00', to_char(contact.contact_date, 'yyyy-mm-dd') || 'T' || to_char(contact.contact_start_time, 'hh24:mi:ss')),
                     'endDateTime' value  decode(contact.contact_end_time, null, null, to_char(contact.contact_date, 'yyyy-mm-dd') || 'T' || to_char(contact.contact_end_time, 'hh24:mi:ss')),
                     'requiresOutcome' value case when exists (select cto.contact_outcome_type_id
                                                               from r_contact_type_outcome cto
                                                               join r_contact_outcome_type cot on cot.contact_outcome_type_id = cto.contact_outcome_type_id
                                                               where r_contact_outcome_type.code is null
                                                                 and cto.contact_type_id = r_contact_type.contact_type_id
                                                                 and cot.selectable = 'Y')
                                                      then 'Y' else 'N' end,
                     'nationalStandard' value r_contact_type.national_standards_contact,
                     'systemGenerated' value decode(r_contact_type.sgc_flag, 1, 'Y', 0, 'N', null),
                     'outcomeRequiredFlag' value r_contact_type.contact_outcome_flag,
                     'attendanceContact' value r_contact_type.attendance_contact,
                     'lastUpdatedDateTime' value contact.last_updated_datetime,
                     'typeCode' value r_contact_type.code,
                     'typeDescription' value r_contact_type.description,
                     'typeShortDescription' value r_contact_type.short_description,
                     'outcomeCode' value r_contact_outcome_type.code,
                     'outcomeDescription' value r_contact_outcome_type.description,
                     'softDeleted' value contact.soft_deleted,
                     'rowVersion' value contact.row_version
                     returning clob) as "json",
             contact.contact_id      as "contactId"
      from (select * from page union all select * from single) contact
      left outer join offender on offender.offender_id = contact.offender_id
      left outer join r_contact_type on r_contact_type.contact_type_id = contact.contact_type_id
      left outer join r_contact_outcome_type
                      on r_contact_outcome_type.contact_outcome_type_id = contact.contact_outcome_type_id
      where contact.soft_deleted = 0)
union all
select json_object('indexReady' value case when :sql_last_value >= (select max(contact_id) from contact) then 'true' else 'false' end format json,
                   'lastValue' value :sql_last_value,
                   'rowVersion' value :sql_last_value
                   returning clob)       as "json",
       -1                                as "contactId",
       (select sql_next_value from next) as "sql_next_value"
from dual
where :contact_id = 0