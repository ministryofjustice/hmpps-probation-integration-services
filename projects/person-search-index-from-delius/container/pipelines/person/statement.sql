WITH NEXT AS (SELECT NVL(MIN(OFFENDER_ID), :sql_last_value + :batch_size) sql_next_value
              FROM OFFENDER WHERE OFFENDER_ID >= :sql_last_value + :batch_size)
SELECT "json",
       "offenderId",
       (SELECT SQL_NEXT_VALUE FROM NEXT) AS "sql_next_value"
FROM (WITH PAGE AS (SELECT * FROM OFFENDER WHERE :offender_id = 0
                                             AND OFFENDER.OFFENDER_ID >= :sql_last_value
                                             AND OFFENDER.OFFENDER_ID < :sql_last_value + :batch_size
                                           ORDER BY OFFENDER_ID FETCH NEXT :batch_size ROWS ONLY),
           SINGLE AS (SELECT * FROM OFFENDER WHERE :offender_id > 0 AND OFFENDER.OFFENDER_ID = :offender_id),
           PRE_CON AS (SELECT doc.*, ROW_NUMBER() over (PARTITION BY OFFENDER_ID ORDER BY LAST_SAVED DESC) row_number
                          FROM DOCUMENT doc
                          WHERE (:offender_id = 0
                                  AND doc.OFFENDER_ID >= :sql_last_value
                                  AND doc.OFFENDER_ID < :sql_last_value + :batch_size)
                             OR (:offender_id > 0 AND doc.OFFENDER_ID = :offender_id)
                            AND doc.DOCUMENT_TYPE = 'PREVIOUS_CONVICTION'
                            AND doc.SOFT_DELETED = 0)
SELECT json_object(
               'otherIds' VALUE json_object(
               'crn' VALUE o.CRN,
               'nomsNumber' VALUE o.NOMS_NUMBER,
               'croNumber' VALUE o.CRO_NUMBER,
               'niNumber' VALUE o.NI_NUMBER,
               'pncNumber' VALUE o.PNC_NUMBER,
               'previousCrn' value (SELECT IDENTIFIER
                                    FROM ADDITIONAL_IDENTIFIER prev
                                    JOIN R_STANDARD_REFERENCE_LIST ait ON ait.CODE_VALUE = 'MFCRN' AND
                                                                          ait.STANDARD_REFERENCE_LIST_ID = prev.IDENTIFIER_NAME_ID
                                    WHERE prev.OFFENDER_ID = o.OFFENDER_ID
                                    ORDER BY prev.CREATED_DATETIME DESC FETCH NEXT 1 ROWS ONLY),
               'immigrationNumber' VALUE o.IMMIGRATION_NUMBER,
               'mostRecentPrisonerNumber' VALUE o.MOST_RECENT_PRISONER_NUMBER ABSENT ON NULL),
               'firstName' VALUE o.FIRST_NAME,
               'middleNames' VALUE json_array(o.SECOND_NAME, o.THIRD_NAME ABSENT ON NULL),
               'surname' VALUE o.SURNAME,
               'previousSurname' VALUE o.PREVIOUS_SURNAME,
               'preferredName' VALUE o.PREFERRED_NAME,
               'title' VALUE title.CODE_DESCRIPTION,
               'dateOfBirth' VALUE to_char(o.DATE_OF_BIRTH_DATE, 'yyyy-MM-dd'),
               'gender' VALUE gender.CODE_DESCRIPTION,
               'partitionArea' VALUE pa.AREA,
               'restrictionMessage' VALUE o.RESTRICTION_MESSAGE,
               'exclusionMessage' VALUE o.EXCLUSION_MESSAGE,
               'currentRestriction' VALUE CASE WHEN o.CURRENT_RESTRICTION = 1 THEN 'true' ELSE 'false' END FORMAT JSON,
               'currentExclusion' VALUE CASE WHEN o.CURRENT_EXCLUSION = 1 THEN 'true' ELSE 'false' END FORMAT JSON,
               'currentTier' VALUE tier.CODE_VALUE,
               'currentDisposal' VALUE TO_CHAR(o.CURRENT_DISPOSAL),
               'activeProbationManagedSentence' VALUE CASE WHEN o.CURRENT_DISPOSAL = 1 THEN 'true' ELSE 'false' END
               FORMAT JSON,
               'offenderProfile' VALUE json_object(
                       'ethnicity' VALUE eth.CODE_DESCRIPTION,
                       'nationality' VALUE nat.CODE_DESCRIPTION,
                       'secondNationality' VALUE secNat.CODE_DESCRIPTION,
                       'notes' VALUE o.NOTES,
                       'immigrationStatus' VALUE imStat.CODE_DESCRIPTION,
                       'offenderLanguages' VALUE json_object(
                               'primaryLanguage' VALUE lan.CODE_DESCRIPTION,
                               'languageConcerns' VALUE o.LANGUAGE_CONCERNS,
                               'requiresInterpreter' VALUE
                               CASE WHEN o.INTERPRETER_REQUIRED = 'Y' THEN 'true' ELSE 'false' END FORMAT JSON
                               ABSENT ON NULL),
                       'religion' VALUE rel.CODE_DESCRIPTION,
                       'sexualOrientation' VALUE so.CODE_DESCRIPTION,
                       'offenderDetails' VALUE o.OFFENDER_DETAILS,
                       'remandStatus' VALUE o.CURRENT_REMAND_STATUS,
                       'previousConviction' VALUE
                       json_object('convictionDate' VALUE to_char(pre_con.DATE_PRODUCED, 'yyyy-MM-dd'),
                                   'detail' VALUE json_object('documentName' VALUE pre_con.DOCUMENT_NAME)
                                   ABSENT ON NULL),
                       'riskColour' VALUE o.CURRENT_HIGHEST_RISK_COLOUR,
                       'genderIdentity' VALUE genDes.CODE_DESCRIPTION,
                       'selfDescribedGender' VALUE o.GENDER_IDENTITY_DESCRIPTION,
                       'disabilities' VALUE (SELECT json_arrayagg(
                                                            json_object('disabilityId' VALUE d.DISABILITY_ID,
                                                                        'disabilityType' VALUE json_object(
                                                                                'code' VALUE dt.CODE_VALUE,
                                                                                'description' VALUE dt.CODE_DESCRIPTION
                                                                            ),
                                                                        'condition' VALUE json_object(
                                                                                'code' VALUE dc.CODE_VALUE,
                                                                                'description' VALUE dc.CODE_DESCRIPTION
                                                                            ),
                                                                        'startDate' VALUE
                                                                        to_char(d.START_DATE, 'yyyy-MM-dd'),
                                                                        'endDate' VALUE
                                                                        to_char(d.FINISH_DATE, 'yyyy-MM-dd'),
                                                                        'notes' VALUE d.NOTES,
                                                                        'lastUpdatedDateTime' VALUE
                                                                        d.LAST_UPDATED_DATETIME ABSENT ON NULL
                                                                        RETURNING CLOB)
                                                            ABSENT ON NULL RETURNING CLOB)
                                             FROM DISABILITY d
                                                      JOIN R_STANDARD_REFERENCE_LIST dt
                                                           ON dt.STANDARD_REFERENCE_LIST_ID = d.DISABILITY_TYPE_ID
                                                      LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST dc
                                                                      ON dc.STANDARD_REFERENCE_LIST_ID = d.DISABILITY_CONDITION_ID
                                             WHERE d.OFFENDER_ID = o.OFFENDER_ID
                                               AND d.SOFT_DELETED = 0),
                       'provisions' VALUE (SELECT json_arrayagg(json_object(
                                                                        'provisionId'
                                                                        VALUE
                                                                        pr.PROVISION_ID,
                                                                        'provisionType' VALUE json_object(
                                                                                'code'
                                                                                VALUE
                                                                                pt.CODE_VALUE,
                                                                                'description'
                                                                                VALUE
                                                                                pt.CODE_DESCRIPTION
                                                                            ),
                                                                        'category' VALUE json_object(
                                                                                'code'
                                                                                VALUE
                                                                                pcat.CODE_VALUE,
                                                                                'description'
                                                                                VALUE
                                                                                pcat.CODE_DESCRIPTION
                                                                            ),
                                                                        'notes' VALUE
                                                                        pr.NOTES,
                                                                        'startDate' VALUE
                                                                        to_char(pr.START_DATE, 'yyyy-MM-dd'),
                                                                        'endDate' VALUE
                                                                        to_char(pr.FINISH_DATE, 'yyyy-MM-dd')
                                                                        ABSENT ON NULL
                                                                        RETURNING CLOB)
                                                                ABSENT ON NULL
                                                                RETURNING CLOB)
                                           FROM PROVISION pr
                                                    JOIN R_STANDARD_REFERENCE_LIST pt ON pt.STANDARD_REFERENCE_LIST_ID = pr.PROVISION_TYPE_ID
                                                    LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST pcat ON pcat.STANDARD_REFERENCE_LIST_ID = pr.PROVISION_CATEGORY_ID
                                           WHERE pr.OFFENDER_ID = o.OFFENDER_ID
                                             AND pr.SOFT_DELETED = 0)
                       ABSENT ON NULL RETURNING CLOB),
               'softDeleted' VALUE CASE WHEN o.SOFT_DELETED = 1 THEN 'true' ELSE 'false' END FORMAT JSON,
               'offenderManagers' VALUE json_array(json_object(
                                                           'active' VALUE
                                                           CASE WHEN om.ACTIVE_FLAG = 1 THEN 'true' ELSE 'false' END
                                                           FORMAT JSON,
                                                           'partitionArea' VALUE ompa.AREA,
                                                           'fromDate' VALUE om.ALLOCATION_DATE,
                                                           'toDate' VALUE om.END_DATE,
                                                           'softDeleted' VALUE
                                                           CASE WHEN om.SOFT_DELETED = 1 THEN 'true' ELSE 'false' END
                                                           FORMAT JSON,
                                                           'staff' VALUE json_object(
                                                                   'code' VALUE staff.OFFICER_CODE, 'forenames' VALUE
                                                                   staff.FORENAME ||
                                                                   DECODE(staff.FORENAME2, NULL, '', ' ' || staff.FORENAME2),
                                                                   'surname' VALUE staff.SURNAME,
                                                                   'unallocated' VALUE
                                                                   CASE WHEN staff.OFFICER_CODE like '%U'
                                                                       THEN 'true' ELSE 'false' END FORMAT JSON
                                                                   ABSENT ON NULL),
                                                           'team' VALUE json_object(
                                                                   'code' VALUE team.CODE,
                                                                   'description' VALUE team.DESCRIPTION,
                                                                   'telephone' VALUE team.TELEPHONE,
                                                                   'emailAddress' VALUE team.EMAIL_ADDRESS,
                                                                   'teamType' VALUE json_object(
                                                                           'code' VALUE ldu.CODE,
                                                                           'description' VALUE ldu.DESCRIPTION
                                                                           ABSENT ON NULL),
                                                                   'district' VALUE json_object(
                                                                           'code' VALUE district.CODE,
                                                                           'description' VALUE district.DESCRIPTION
                                                                           ABSENT ON NULL),
                                                                   'borough' VALUE json_object(
                                                                           'code' VALUE boro.CODE,
                                                                           'description' VALUE boro.DESCRIPTION
                                                                           ABSENT ON NULL)
                                                                   ABSENT ON NULL RETURNING CLOB),
                                                           'probationArea' VALUE json_object(
                                                                   'id' VALUE pa.PROBATION_AREA_ID,
                                                                   'code' VALUE pa.CODE,
                                                                   'description' VALUE pa.DESCRIPTION,
                                                                   'nps' VALUE
                                                                   CASE WHEN pa.PRIVATE = 1 THEN 'true' ELSE 'false' END
                                                                   FORMAT JSON),
                                                           'allocationReason' VALUE
                                                           json_object('code' VALUE ar.CODE_VALUE, 'description' VALUE
                                                                       ar.CODE_DESCRIPTION ABSENT ON NULL)
                                                           ABSENT ON NULL RETURNING CLOB) RETURNING CLOB),
               'contactDetails' VALUE json_object(
                       'phoneNumbers' VALUE json_array(
                        CASE
                            WHEN o.TELEPHONE_NUMBER IS NULL THEN NULL
                            ELSE json_object('number' VALUE o.TELEPHONE_NUMBER, 'type' VALUE 'TELEPHONE' ABSENT ON
                                             NULL) END,
                        CASE
                            WHEN o.MOBILE_NUMBER IS NULL THEN NULL
                            ELSE json_object('number' VALUE o.MOBILE_NUMBER, 'type' VALUE 'MOBILE' ABSENT ON NULL) END
                        ABSENT ON NULL RETURNING CLOB),
                       'emailAddresses' VALUE json_array(o.E_MAIL_ADDRESS ABSENT ON NULL RETURNING CLOB),
                       'allowSms' VALUE CASE WHEN o.ALLOW_SMS = 'Y' THEN 'true' ELSE 'false' END FORMAT JSON,
                       'addresses' VALUE (SELECT json_arrayagg(json_object(
                                                                       'id' VALUE oa.offender_address_id,
                                                                       'from' VALUE
                                                                       to_char(oa.START_DATE, 'yyyy-MM-dd'),
                                                                       'to' VALUE to_char(oa.END_DATE, 'yyyy-MM-dd'),
                                                                       'noFixedAbode' VALUE
                                                                       CASE
                                                                           WHEN oa.NO_FIXED_ABODE = 'Y' THEN 'true'
                                                                           ELSE 'false' END FORMAT JSON,
                                                                       'notes' VALUE oa.NOTES,
                                                                       'addressNumber' VALUE oa.ADDRESS_NUMBER,
                                                                       'buildingName' VALUE oa.BUILDING_NAME,
                                                                       'streetName' VALUE oa.STREET_NAME,
                                                                       'district' VALUE oa.DISTRICT,
                                                                       'town' VALUE oa.TOWN_CITY,
                                                                       'county' VALUE oa.COUNTY,
                                                                       'postcode' VALUE oa.POSTCODE,
                                                                       'telephoneNumber' VALUE oa.TELEPHONE_NUMBER,
                                                                       'type' VALUE json_object(
                                                                               'code' VALUE type.CODE_VALUE,
                                                                               'description' VALUE type.CODE_DESCRIPTION
                                                                               ABSENT ON NULL),
                                                                       'status' VALUE json_object(
                                                                               'code' VALUE status.CODE_VALUE,
                                                                               'description' VALUE
                                                                               status.CODE_DESCRIPTION
                                                                               ABSENT ON NULL),
                                                                       'typeVerified' VALUE CASE
                                                                                                WHEN oa.TYPE_VERIFIED = 'Y'
                                                                                                    THEN 'true'
                                                                                                ELSE 'false' END FORMAT
                                                                       JSON,
                                                                       'createdDateTime' VALUE oa.CREATED_DATETIME,
                                                                       'lastUpdatedDateTime' VALUE
                                                                       oa.LAST_UPDATED_DATETIME
                                                                       ABSENT ON NULL RETURNING CLOB) RETURNING CLOB)
                                          FROM OFFENDER_ADDRESS oa
                                                   LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST type
                                                                   ON oa.ADDRESS_TYPE_ID = type.STANDARD_REFERENCE_LIST_ID
                                                   LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST status
                                                                   ON oa.ADDRESS_STATUS_ID = status.STANDARD_REFERENCE_LIST_ID
                                          WHERE oa.OFFENDER_ID = o.OFFENDER_ID
                                            AND oa.SOFT_DELETED = 0)
                       ABSENT ON NULL RETURNING CLOB),
               'offenderAliases' VALUE (SELECT json_arrayagg(json_object(
                                                                     'id' VALUE al.ALIAS_ID,
                                                                     'dateOfBirth' VALUE
                                                                     to_char(al.DATE_OF_BIRTH_DATE, 'yyyy-MM-dd'),
                                                                     'firstName' VALUE al.FIRST_NAME,
                                                                     'middleNames' VALUE
                                                                     json_array(al.SECOND_NAME, al.THIRD_NAME ABSENT ON NULL),
                                                                     'surname' VALUE al.SURNAME,
                                                                     'gender' VALUE al_gender.CODE_DESCRIPTION
                                                                     ABSENT ON NULL RETURNING CLOB) RETURNING CLOB)
                                        FROM ALIAS al
                                                 JOIN R_STANDARD_REFERENCE_LIST al_gender
                                                      ON al.GENDER_ID = al_gender.STANDARD_REFERENCE_LIST_ID
                                        WHERE al.OFFENDER_ID = o.OFFENDER_ID
                                          AND al.SOFT_DELETED = 0),
               'probationStatus' VALUE json_object(
                       'status' VALUE
                       CASE
                           WHEN o.CURRENT_DISPOSAL = 1 THEN 'CURRENT'
                           WHEN EXISTS(SELECT 1
                                       FROM DISPOSAL d
                                       WHERE d.OFFENDER_ID = o.OFFENDER_ID) THEN 'PREVIOUSLY_KNOWN'
                           ELSE 'NOT_SENTENCED' END,
                       'previouslyKnownTerminationDate' VALUE
                       (SELECT MAX(d.TERMINATION_DATE) FROM DISPOSAL d WHERE d.OFFENDER_ID = o.OFFENDER_ID),
                       'inBreach' VALUE CASE
                                            WHEN o.CURRENT_DISPOSAL = 1 AND EXISTS(SELECT 1
                                                                                   FROM EVENT e
                                                                                   WHERE e.OFFENDER_ID = o.OFFENDER_ID
                                                                                     AND e.ACTIVE_FLAG = 1
                                                                                     AND e.SOFT_DELETED = 0
                                                                                     AND e.IN_BREACH = 1) THEN 'true'
                                            ELSE 'false' END FORMAT JSON,
                       'preSentenceActivity' VALUE CASE
                                                       WHEN EXISTS(SELECT 1
                                                                   FROM EVENT e
                                                                   WHERE e.OFFENDER_ID = o.OFFENDER_ID
                                                                     AND NOT EXISTS(SELECT 1 FROM DISPOSAL d WHERE d.EVENT_ID = e.EVENT_ID))
                                                           THEN 'true'
                                                       ELSE 'false' END FORMAT JSON,
                       'awaitingPsr' VALUE CASE
                                               WHEN EXISTS(SELECT 1
                                                           FROM COURT_APPEARANCE ca
                                                                    JOIN R_STANDARD_REFERENCE_LIST outcome
                                                                         ON outcome.STANDARD_REFERENCE_LIST_ID = ca.OUTCOME_ID
                                                                    JOIN EVENT e ON e.EVENT_ID = ca.EVENT_ID
                                                                    LEFT OUTER JOIN DISPOSAL d ON d.EVENT_ID = e.EVENT_ID
                                                           WHERE e.OFFENDER_ID = o.OFFENDER_ID
                                                             AND d.DISPOSAL_ID IS NULL
                                                             AND outcome.CODE_VALUE = '101'
                                                   ) THEN 'true'
                                               ELSE 'false' END FORMAT JSON
                       ABSENT ON NULL RETURNING CLOB),
               'mappa' VALUE (SELECT json_object(
                                             'level' VALUE CASE
                                                               WHEN lvl.CODE_VALUE = 'M1' THEN 1
                                                               WHEN lvl.CODE_VALUE = 'M2' THEN 2
                                                               WHEN lvl.CODE_VALUE = 'M3' THEN 3
                                                               ELSE 0 END,
                                             'levelDescription' VALUE coalesce(lvl.CODE_DESCRIPTION, 'Missing Level'),
                                             'category' VALUE CASE
                                                                  WHEN cat.CODE_VALUE = 'M1' THEN 1
                                                                  WHEN cat.CODE_VALUE = 'M2' THEN 2
                                                                  WHEN cat.CODE_VALUE = 'M3' THEN 3
                                                                  WHEN cat.CODE_VALUE = 'M4' THEN 4
                                                                  ELSE 0 END,
                                             'categoryDescription' VALUE
                                             COALESCE(cat.CODE_DESCRIPTION, 'Missing category'),
                                             'startDate' VALUE to_char(r.REGISTRATION_DATE, 'yyyy-MM-dd'),
                                             'reviewDate' VALUE to_char(r.NEXT_REVIEW_DATE, 'yyyy-MM-dd'),
                                             'notes' VALUE r.REGISTRATION_NOTES,
                                             'team' VALUE
                                             json_object('code' VALUE t.CODE, 'description' VALUE t.DESCRIPTION),
                                             'officer' VALUE
                                             json_object('code' VALUE s.OFFICER_CODE, 'surname' VALUE s.SURNAME,
                                                         'forenames' VALUE s.FORENAME ||
                                                                           DECODE(s.FORENAME2, NULL, '', ' ' || s.FORENAME2)),
                                             'probationArea' VALUE
                                             json_object('code' VALUE pa.CODE, 'description' VALUE pa.DESCRIPTION)
                                             ABSENT ON NULL RETURNING CLOB)
                              FROM REGISTRATION r
                                       JOIN R_REGISTER_TYPE rt ON rt.REGISTER_TYPE_ID = r.REGISTER_TYPE_ID
                                       LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST lvl
                                                       ON lvl.STANDARD_REFERENCE_LIST_ID = r.REGISTER_LEVEL_ID
                                       LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST cat
                                                       ON cat.STANDARD_REFERENCE_LIST_ID = r.REGISTER_CATEGORY_ID
                                       LEFT OUTER JOIN TEAM t ON t.TEAM_ID = r.REGISTERING_TEAM_ID
                                       LEFT OUTER JOIN STAFF s ON s.STAFF_ID = r.REGISTERING_STAFF_ID
                                       LEFT OUTER JOIN PROBATION_AREA pa ON pa.PROBATION_AREA_ID = t.PROBATION_AREA_ID
                              WHERE r.OFFENDER_ID = o.OFFENDER_ID
                                AND r.SOFT_DELETED = 0
                                AND r.DEREGISTERED = 0
                                AND rt.CODE = 'MAPP'
                              ORDER BY r.CREATED_DATETIME DESC FETCH NEXT 1 ROWS ONLY)
               ABSENT ON NULL RETURNING CLOB) "json",
       o.OFFENDER_ID AS                       "offenderId"
FROM (SELECT * FROM PAGE UNION ALL SELECT * FROM SINGLE) o
         JOIN PARTITION_AREA pa ON pa.PARTITION_AREA_ID = o.PARTITION_AREA_ID
         JOIN OFFENDER_MANAGER om ON om.OFFENDER_ID = o.OFFENDER_ID AND om.ACTIVE_FLAG = 1
         LEFT OUTER JOIN PROBATION_AREA pa ON pa.PROBATION_AREA_ID = om.PROBATION_AREA_ID
         LEFT OUTER JOIN STAFF staff ON staff.STAFF_ID = om.ALLOCATION_STAFF_ID
         LEFT OUTER JOIN PARTITION_AREA ompa ON ompa.PARTITION_AREA_ID = om.PARTITION_AREA_ID
         LEFT OUTER JOIN TEAM team ON team.TEAM_ID = om.TEAM_ID
         LEFT OUTER JOIN LOCAL_DELIVERY_UNIT ldu ON ldu.LOCAL_DELIVERY_UNIT_ID = team.LOCAL_DELIVERY_UNIT_ID
         LEFT OUTER JOIN DISTRICT district ON district.DISTRICT_ID = team.DISTRICT_ID
         LEFT OUTER JOIN BOROUGH boro ON boro.BOROUGH_ID = district.BOROUGH_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST title ON title.STANDARD_REFERENCE_LIST_ID = o.TITLE_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST gender ON gender.STANDARD_REFERENCE_LIST_ID = o.GENDER_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST ar ON ar.STANDARD_REFERENCE_LIST_ID = om.ALLOCATION_REASON_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST eth ON eth.STANDARD_REFERENCE_LIST_ID = o.ETHNICITY_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST nat ON nat.STANDARD_REFERENCE_LIST_ID = o.NATIONALITY_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST secNat ON secNat.STANDARD_REFERENCE_LIST_ID = o.SECOND_NATIONALITY_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST imStat ON imStat.STANDARD_REFERENCE_LIST_ID = o.IMMIGRATION_STATUS_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST rel ON rel.STANDARD_REFERENCE_LIST_ID = o.RELIGION_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST so ON so.STANDARD_REFERENCE_LIST_ID = o.SEXUAL_ORIENTATION_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST lan ON lan.STANDARD_REFERENCE_LIST_ID = o.LANGUAGE_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST genDes ON genDes.STANDARD_REFERENCE_LIST_ID = o.GENDER_IDENTITY_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST tier ON tier.STANDARD_REFERENCE_LIST_ID = o.CURRENT_TIER
         LEFT OUTER JOIN pre_con ON pre_con.OFFENDER_ID = o.OFFENDER_ID AND row_number = 1


WHERE o.SOFT_DELETED = 0
  AND om.SOFT_DELETED = 0
ORDER BY o.OFFENDER_ID)

UNION ALL

SELECT JSON_OBJECT('indexReady'
                   VALUE CASE
                             WHEN :sql_last_value >= (SELECT max(offender_id)
                                                      FROM OFFENDER o
                                                      WHERE SOFT_DELETED = 0
                                                        AND EXISTS(SELECT 1
                                                                   FROM OFFENDER_MANAGER om
                                                                   WHERE o.OFFENDER_ID = om.OFFENDER_ID
                                                                     AND om.ACTIVE_FLAG = 1
                                                                     AND om.SOFT_DELETED = 0)) then 'true' ELSE 'false' END FORMAT JSON,
                   'nextValue' VALUE (SELECT SQL_NEXT_VALUE FROM NEXT)
                   RETURNING CLOB)       AS "json",
       -1                                AS "offenderId",
       (SELECT SQL_NEXT_VALUE FROM NEXT) AS "sql_next_value"
FROM DUAL
WHERE :offender_id = 0