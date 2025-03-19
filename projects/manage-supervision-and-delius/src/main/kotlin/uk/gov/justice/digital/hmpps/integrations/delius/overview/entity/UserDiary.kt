package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserDiaryRepository : JpaRepository<Contact, Long> {
    @Query(
        """
                SELECT  ROWNUM,
                        o1.first_name AS forename,
                        o1.second_name AS second_name,
                        o1.third_name AS third_name,
                        o1.surname AS surname,
                        o1.date_of_birth_date AS dob,
                        c1.contact_id AS id,
                        o1.crn AS crn, 
                        ol1.description AS location, 
                        c1.contact_date AS contact_date, 
                        c1.contact_start_time AS contact_start_time,
                        c1.contact_end_time AS contact_end_time,
                        total_sentences,
                        rct1.description AS contactDescription,
                        NVL(rdt1.description, latest_sentence_description)  AS sentenceDescription
                FROM contact c1 
                JOIN offender o1 ON o1.offender_id = c1.offender_id
                JOIN r_contact_type rct1 ON rct1.contact_type_id = c1.contact_type_id 
                JOIN staff s1 ON s1.staff_id = c1.staff_id 
                JOIN caseload cl1 ON s1.staff_id = cl1.staff_employee_id AND c1.offender_id = cl1.offender_id AND (cl1.role_code = 'OM') 
                LEFT JOIN event e1 ON e1.event_id = c1.event_id AND (e1.soft_deleted = 0) 
                LEFT JOIN disposal d1 ON e1.event_id = d1.event_id 
                LEFT JOIN r_disposal_type rdt1 ON rdt1.disposal_type_id = d1.disposal_type_id 
                LEFT JOIN office_location ol1 ON ol1.office_location_id = c1.office_location_id 
                LEFT JOIN ( 
                        SELECT sub1.* 
                        FROM
                          (SELECT e1.*,
                            rdt1.description AS latest_sentence_description,
                            COUNT(e1.event_id) over (PARTITION BY e1.offender_id) AS total_sentences,
                            ROW_NUMBER() over (PARTITION BY e1.offender_id ORDER BY CAST(e1.event_number AS NUMBER) DESC) AS row_num 
                            FROM event e1
                            JOIN disposal d1 ON d1.event_id = e1.event_id
                            JOIN r_disposal_type rdt1 ON rdt1.disposal_type_id = d1.disposal_type_id
                            WHERE e1.soft_deleted = 0 
                            AND e1.active_flag = 1
                            ) sub1
                        WHERE sub1.row_num = 1
                 ) ls1 ON ls1.offender_id = c1.offender_id 
                 WHERE (c1.soft_deleted = 0) 
                 AND s1.staff_id = :staffId
                 AND rct1.attendance_contact = 'Y' 
                 AND (to_char(c1.contact_date,'YYYY-MM-DD') = :dateNow 
                    AND to_char(c1.contact_start_time,'HH24:MI') > :timeNow)
                 ORDER BY c1.contact_date, c1.contact_start_time
        """,
        countQuery = """
                SELECT  COUNT(*)
                FROM contact c1 
                JOIN offender o1 ON o1.offender_id = c1.offender_id
                JOIN r_contact_type rct1 ON rct1.contact_type_id = c1.contact_type_id 
                JOIN staff s1 ON s1.staff_id = c1.staff_id 
                JOIN caseload cl1 ON s1.staff_id = cl1.staff_employee_id AND c1.offender_id = cl1.offender_id AND (cl1.role_code = 'OM') 
                WHERE (c1.soft_deleted = 0) 
                AND s1.staff_id = :staffId
                AND rct1.attendance_contact = 'Y' 
                AND (to_char(c1.contact_date,'YYYY-MM-DD') = :dateNow 
                AND to_char(c1.contact_start_time,'HH24:MI') > :timeNow)     
        """,
        nativeQuery = true
    )
    fun findAppointmentsForTodayByUser(
        staffId: Long,
        dateNow: String,
        timeNow: String,
        pageable: Pageable
    ): Page<Appointment>

}