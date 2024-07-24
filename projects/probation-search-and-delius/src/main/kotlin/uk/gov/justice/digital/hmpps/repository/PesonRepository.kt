package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.ContactJson
import uk.gov.justice.digital.hmpps.entity.Person

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrn(crn: String): Person?

    @Query(
        """
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
                 'lastUpdatedDateTime' value contact.last_updated_datetime,
                 'typeCode' value r_contact_type.code,
                 'typeDescription' value r_contact_type.description,
                 'typeShortDescription' value r_contact_type.short_description,
                 'outcomeCode' value r_contact_outcome_type.code,
                 'outcomeDescription' value r_contact_outcome_type.description,
                 'softDeleted' value contact.soft_deleted,
                 'rowVersion' value contact.row_version
                 returning clob) as "json",
            contact.contact_id   as "contactId"
            from contact
              left outer join offender on offender.offender_id = contact.offender_id
              left outer join r_contact_type on r_contact_type.contact_type_id = contact.contact_type_id
              left outer join r_contact_outcome_type on r_contact_outcome_type.contact_outcome_type_id = contact.contact_outcome_type_id
            where contact.soft_deleted = 0
            and contact.offender_id = :personId
        """,
        nativeQuery = true
    )
    fun getContacts(personId: Long): List<ContactJson>
}