package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface LicenceConditionRepository : JpaRepository<LicenceCondition, Long> {

    @Query(
        """
            SELECT lc FROM LicenceCondition lc 
            JOIN FETCH lc.mainCategory mc
            LEFT JOIN FETCH lc.subCategory
            WHERE lc.disposalId = :disposalId
            ORDER BY mc.description, lc.id ASC
        """
    )
    fun findAllByDisposalId(disposalId: Long): List<LicenceCondition>
}

fun LicenceConditionRepository.getByLicenceConditionId(id: Long) = findById(id)

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