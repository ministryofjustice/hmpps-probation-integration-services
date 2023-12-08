package uk.gov.justice.digital.hmpps.controller.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "rqmnt")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class RequirementEntity(
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
    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,
)

@Immutable
@Entity
@Table(name = "r_rqmnt_type_main_category")
class RequirementMainCategory(
    @Id
    @Column(name = "rqmnt_type_main_category_id", nullable = false)
    val id: Long,
    val code: String,
    val description: String,
    @Column(name = "restrictive")
    @Convert(converter = YesNoConverter::class)
    val restrictive: Boolean,
)

interface RequirementRepository : JpaRepository<RequirementEntity, Long>
