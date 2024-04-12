package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.api.model.overview.Rar
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "rqmnt")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class Requirement(
    @Id
    @Column(name = "rqmnt_id", nullable = false)
    val id: Long,

    val length: Long?,

    @Column(name = "rqmnt_notes", columnDefinition = "clob")
    val notes: String?,

    @Column(name = "rqmnt_type_sub_category_id")
    val subCategoryId: Long?,

    val expectedStartDate: LocalDate?,

    val startDate: LocalDate,

    val commencementDate: LocalDate?,

    val expectedEndDate: LocalDate?,

    val terminationDate: LocalDate?,

    val rqmntTerminationReasonId: String?,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal? = null,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val mainCategory: RequirementMainCategory?,

    @Column(name = "active_flag", columnDefinition = "NUMBER", nullable = false)
    val active: Boolean = true,

    @Column(columnDefinition = "NUMBER")
    val softDeleted: Boolean = false
)

interface RarDays {
    val days: Int
    val type: String
}

interface RequirementDetails {
    val id: Long
    val expectedStartDate: LocalDate?
    val startDate: LocalDate
    val commencementDate: LocalDate?
    val expectedEndDate: LocalDate?
    val terminationDate: LocalDate?
    val terminationReason: String?
    val length: Long?
    val lengthUnitValue: String?
    val code: String
    val description: String
    val codeDescription: String
    val notes: String?
}

interface RequirementRepository : JpaRepository<Requirement, Long> {

    @Query(
        """
        select count(r.rqmnt_id) as days, 'SCHEDULED' as type from contact c
        join rqmnt r on r.rqmnt_id = c.rqmnt_id
        join r_rqmnt_type_main_category mc on r.rqmnt_type_main_category_id = mc.rqmnt_type_main_category_id
        where c.rar_activity = 'Y' and c.soft_deleted = 0
        and (c.attended is null)
        and (c.complied is null or c.complied = 'Y')
        and mc.code = 'F' and r.active_flag = 1 and r.soft_deleted = 0
        and r.disposal_id = :disposalId
        union
        select count(r.rqmnt_id) as days, 'COMPLETED' as type from contact c
        join rqmnt r on r.rqmnt_id = c.rqmnt_id
        join r_rqmnt_type_main_category mc on r.rqmnt_type_main_category_id = mc.rqmnt_type_main_category_id
        where c.rar_activity = 'Y' and c.soft_deleted = 0
        and (c.attended = 'Y')
        and (c.complied is null or c.complied = 'Y')
        and mc.code = 'F' and r.active_flag = 1 and r.soft_deleted = 0
        and r.disposal_id = :disposalId
        """, nativeQuery = true
    )
    fun getRarDays(disposalId: Long): List<RarDays>

    @Query(
        """
        select count(r.rqmnt_id) as days, 'SCHEDULED' as type from contact c
        join rqmnt r on r.rqmnt_id = c.rqmnt_id
        join r_rqmnt_type_main_category mc on r.rqmnt_type_main_category_id = mc.rqmnt_type_main_category_id
        where c.rar_activity = 'Y' and c.soft_deleted = 0
        and (c.attended is null)
        and (c.complied is null or c.complied = 'Y')
        and mc.code = 'F' and r.active_flag = 1 and r.soft_deleted = 0
        and r.disposal_id = :requirementId
        union
        select count(r.rqmnt_id) as days, 'COMPLETED' as type from contact c
        join rqmnt r on r.rqmnt_id = c.rqmnt_id
        join r_rqmnt_type_main_category mc on r.rqmnt_type_main_category_id = mc.rqmnt_type_main_category_id
        where c.rar_activity = 'Y' and c.soft_deleted = 0
        and (c.attended = 'Y')
        and (c.complied is null or c.complied = 'Y')
        and mc.code = 'F' and r.active_flag = 1 and r.soft_deleted = 0
        and r.rqmnt_id = :requirementId
        """, nativeQuery = true
    )
    fun getRarDaysByRequirementId(requirementId: Long): List<RarDays>

    @Query(
        """
            SELECT  r.rqmnt_id AS id,
                    r.expected_start_date, 
                    r.start_date, 
                    r.commencement_date, 
                    r.expected_end_date, 
                    r.termination_date,
                    rsrl3.code_description as terminationReason,
                    r."LENGTH", 
                    rsrl2.code_description as lengthUnitValue,
                    rrtmc.code, 
                    rrtmc.description, 
                    rsrl.code_description AS codeDescription, 
                    TO_CHAR(SUBSTR(r.rqmnt_notes, 1, 4000)) AS notes 
            FROM rqmnt r
            JOIN r_rqmnt_type_main_category rrtmc 
            ON rrtmc.rqmnt_type_main_category_id  = r.rqmnt_type_main_category_id 
            JOIN disposal d 
            ON d.disposal_id = r.disposal_id 
            JOIN event e 
            ON e.event_id = d.event_id
            LEFT JOIN r_standard_reference_list rsrl 
            ON rsrl.standard_reference_list_id = r.rqmnt_type_sub_category_id 
            LEFT JOIN r_standard_reference_list rsrl2 
            ON rsrl2.standard_reference_list_id = rrtmc.units_id  
            LEFT JOIN r_standard_reference_list rsrl3
            ON rsrl3.standard_reference_list_id = r.rqmnt_termination_reason_id 
            WHERE e.event_id = :id
            AND e.event_number = :eventNumber
            AND e.soft_deleted = 0 
            AND e.active_flag = 1
            ORDER BY rrtmc.description
        """, nativeQuery = true
    )
    fun getRequirements(id: Long, eventNumber: String): List<RequirementDetails>
}

fun RequirementRepository.getRar(disposalId: Long): Rar {
    val rarDays = getRarDays(disposalId)
    val scheduledDays = rarDays.find { it.type == "SCHEDULED" }?.days ?: 0
    val completedDays = rarDays.find { it.type == "COMPLETED" }?.days ?: 0
    return Rar(completed = completedDays, scheduled = scheduledDays)
}

@Immutable
@Entity
@Table(name = "r_rqmnt_type_main_category")
class RequirementMainCategory(
    @Id
    @Column(name = "rqmnt_type_main_category_id", nullable = false)
    val id: Long,
    val code: String,
    val description: String,
    val unitsId: Long?,
)

