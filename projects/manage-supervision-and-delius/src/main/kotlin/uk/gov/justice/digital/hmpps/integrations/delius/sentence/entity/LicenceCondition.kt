package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "lic_condition")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class LicenceCondition(
    @Id
    @Column(name = "lic_condition_id", nullable = false)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_main_cat_id")
    val mainCategory: LicenceConditionMainCategory,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_sub_cat_id")
    val subCategory: ReferenceData?,

    @Column
    val disposalId: Long,

    @Column(name = "start_date", nullable = false)
    val imposedReleasedDate: LocalDate,

    @Column(name = "commencement_date")
    val actualStartDate: LocalDate?,

    @Column(name = "lic_condition_notes", columnDefinition = "clob")
    val notes: String?,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
) {
    fun getTruncatedNotes(): String? {
        notes?.let { return it.chunked(1500)[0] } ?: return null
    }

    fun hasNotesBeenTruncated(): Boolean? {
        return notes?.let {
            when {
                it.length > 1500 -> true
                else -> false
            }
        }
    }
}

interface LicenceConditionRepository : JpaRepository<LicenceCondition, Long> {
    fun findAllByDisposalId(disposalId: Long): List<LicenceCondition>
}

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