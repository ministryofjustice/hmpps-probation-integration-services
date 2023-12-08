package uk.gov.justice.digital.hmpps.integrations.delius.courtreport

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CourtReportRepository : JpaRepository<CourtReport, Long> {
    @Query(
        """
    SELECT json_object(
           'crn' value o.CRN,
           'pnc' value o.PNC_NUMBER,
           'name' value json_object(
           'forename' value o.FIRST_NAME,
           'surname' value o.SURNAME,
           'middleName' value o.SECOND_NAME),
           'dateOfBirth' value o.DATE_OF_BIRTH_DATE,
           'address' value json_object(
           'noFixedAbode' value CASE WHEN a.NO_FIXED_ABODE = 'Y' THEN 'true' ELSE 'false' END FORMAT JSON,
           'buildingName' value a.BUILDING_NAME,
           'addressNumber' value a.ADDRESS_NUMBER,
           'streetName' value a.STREET_NAME,
           'town' value a.TOWN_CITY,
           'district' value a.DISTRICT,
           'county' value a.COUNTY,
           'postcode' value a.POSTCODE ABSENT ON NULL),
           'court' value json_object(
           'name' value c.COURT_NAME,
           'localJusticeArea' value json_object(
           'name' value lja.DESCRIPTION)),
           'mainOffence' value json_object(
           'description' value moo.DESCRIPTION),
           'otherOffences' value (      
           SELECT json_arrayagg(json_object('description' VALUE aoo.DESCRIPTION))
           FROM ADDITIONAL_OFFENCE ao
           JOIN R_OFFENCE aoo ON aoo.OFFENCE_ID = ao.OFFENCE_ID
           WHERE ao.EVENT_ID = e.EVENT_ID
           AND ao.SOFT_DELETED = 0
           )
        )
FROM OFFENDER o
         JOIN COURT_REPORT cr ON cr.OFFENDER_ID = o.OFFENDER_ID
         JOIN DOCUMENT d ON d.primary_key_id = cr.court_report_id
         JOIN COURT_APPEARANCE ca ON ca.COURT_APPEARANCE_ID = cr.COURT_APPEARANCE_ID
         JOIN EVENT e ON ca.event_id = e.event_id
         JOIN COURT c ON c.COURT_ID = ca.COURT_ID
         JOIN PROBATION_AREA lja ON lja.PROBATION_AREA_ID = c.PROBATION_AREA_ID
         JOIN MAIN_OFFENCE mo ON mo.EVENT_ID = e.EVENT_ID
         LEFT OUTER JOIN R_OFFENCE moo ON moo.OFFENCE_ID = mo.OFFENCE_ID
         LEFT OUTER JOIN (OFFENDER_ADDRESS a 
         JOIN R_STANDARD_REFERENCE_LIST ast ON ast.STANDARD_REFERENCE_LIST_ID = a.ADDRESS_STATUS_ID AND ast.CODE_VALUE = 'M')
         ON a.OFFENDER_ID = o.OFFENDER_ID AND a.SOFT_DELETED = 0
WHERE
  lower(substr(d.external_reference, -36, 36)) = lower(:reportId)
  AND cr.SOFT_DELETED = 0
  AND ca.SOFT_DELETED = 0
  AND o.SOFT_DELETED = 0
  AND mo.SOFT_DELETED = 0
 """,
        nativeQuery = true,
    )
    fun getCourtReportContextJson(reportId: String): String?
}
