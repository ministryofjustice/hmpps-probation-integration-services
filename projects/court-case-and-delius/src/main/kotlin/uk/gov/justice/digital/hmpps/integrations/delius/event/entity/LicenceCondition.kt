package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Disposal
import java.time.LocalDate

@Immutable
@Table(name = "lic_condition")
@Entity
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class LicenceCondition(
    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @Column
    val startDate: LocalDate,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_main_cat_id")
    val mainCategory: LicenceConditionMainCategory,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_sub_cat_id")
    val subCategory: ReferenceData?,

    @Lob
    @Column(name = "lic_condition_notes")
    val notes: String?,

    @Id
    @Column(name = "lic_condition_id")
    val id: Long,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "r_lic_cond_type_main_cat")
@Entity
class LicenceConditionMainCategory(
    @Id
    @Column(name = "lic_cond_type_main_cat_id")
    val id: Long,

    @Column
    val code: String,

    @Column
    val description: String
)

interface LicenceConditionRepository : JpaRepository<LicenceCondition, Long> {
    fun getAllByDisposal(disposal: Disposal): List<LicenceCondition>
}
