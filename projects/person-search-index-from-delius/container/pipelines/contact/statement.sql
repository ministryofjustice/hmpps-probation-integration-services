select json_object(
               'crn' value offender.crn,
               'offenderId' value offender.offender_id,
               'id' value contact.contact_id,
               'date' value to_char(contact.contact_date, 'yyyy-mm-dd'),
               'description' value contact.description,
               'notes' value contact.notes,
               'attended' value decode(contact.attended, 'Y', 'attended', 'N', 'fta', null),
               'complied' value decode(contact.complied, 'Y', 'complied', 'N', 'ftc', null),
               'lastUpdatedDateTime' value contact.last_updated_datetime,
               'typeCode' value r_contact_type.code,
               'typeDescription' value r_contact_type.description,
               'typeShortDescription' value r_contact_type.short_description,
               'outcomeCode' value r_contact_outcome_type.code,
               'outcomeDescription' value r_contact_outcome_type.description,
               'softDeleted' value contact.soft_deleted,
               'rowVersion' value contact.row_version,
               'scn' value case when contact.ora_rowscn is null then 0 else contact.ora_rowscn end
               returning clob) as "json",
       contact.contact_id      as "contactId"
from contact
         left outer join offender on offender.offender_id = contact.offender_id
         left outer join r_contact_type on r_contact_type.contact_type_id = contact.contact_type_id
         left outer join r_contact_outcome_type
                         on r_contact_outcome_type.contact_outcome_type_id = contact.contact_outcome_type_id
where (:contact_id = 0 and contact.contact_id >= :offset)
   or (:contact_id > 0 and contact.contact_id = :contact_id)
order by contact_id
    fetch next :size rows only