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

