package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Immutable
@Entity
@Table(name = "rqmnt")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class Requirement(
    @Id
    @Column(name = "rqmnt_id", nullable = false)
    val id: Long,

    val length: Long?,

    @Column(name = "rqmnt_notes")
    val notes: String?,

    @Column(name = "rqmnt_type_sub_category_id")
    val subCategoryId: String?,

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
            SELECT r."LENGTH", rrtmc.DESCRIPTION, rsrl.CODE_DESCRIPTION, r.RQMNT_NOTES 
            FROM rqmnt r
            JOIN R_RQMNT_TYPE_MAIN_CATEGORY rrtmc 
            ON r.RQMNT_TYPE_MAIN_CATEGORY_ID = RRTMC.RQMNT_TYPE_MAIN_CATEGORY_ID 
            JOIN R_STANDARD_REFERENCE_LIST rsrl 
            ON RSRL.STANDARD_REFERENCE_LIST_ID = r.RQMNT_TYPE_SUB_CATEGORY_ID 
            JOIN DISPOSAL d 
            ON r.DISPOSAL_ID = d.DISPOSAL_ID 
            JOIN EVENT e 
            ON e.EVENT_ID = d.EVENT_ID 
            JOIN OFFENDER o 
            ON o.OFFENDER_ID = e.OFFENDER_ID 
            AND o.CRN = :crn
            AND e.EVENT_NUMBER = :eventNumber
        """, nativeQuery = true
    )
    fun getRequirements(crn: String, eventNumber: String) : List<RequirementDetails>
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

