package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.api.model.overview.Rar

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
    val description: String?
    val codeDescription: String?
    val length: Long?
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
            SELECT r.rqmnt_id as id, r."LENGTH", rrtmc.description, rsrl.code_description AS codeDescription, TO_CHAR(SUBSTR(r.rqmnt_notes, 1, 4000)) AS notes 
            FROM rqmnt r
            JOIN r_rqmnt_type_main_category rrtmc 
            ON r.rqmnt_type_main_category_id = rrtmc.rqmnt_type_main_category_id 
            JOIN r_standard_reference_list rsrl 
            ON rsrl.standard_reference_list_id = r.rqmnt_type_sub_category_id 
            JOIN disposal d 
            ON r.disposal_id = d.disposal_id 
            JOIN event e 
            ON e.event_id = d.event_id
            JOIN offender o 
            ON o.offender_id = e.offender_id 
            AND o.crn = :crn
            AND e.event_number = :eventNumber
            AND e.soft_deleted = 0 
            AND e.active_flag = 1
        """, nativeQuery = true
    )
    fun getRequirements(crn: String, eventNumber: String): List<RequirementDetails>
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
    val description: String
)

