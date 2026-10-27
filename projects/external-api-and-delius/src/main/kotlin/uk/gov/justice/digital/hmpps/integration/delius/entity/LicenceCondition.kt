package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime

@Entity
@Immutable
@Table(name = "lic_condition")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class LicenceCondition(
    @Id
    @Column(name = "lic_condition_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_main_cat_id")
    val mainCategory: LicenceConditionMainCategory,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_sub_cat_id")
    val subCategory: ReferenceData? = null,

    @ManyToOne
    @JoinColumns(
        JoinColumn(
            name = "lic_cond_type_main_cat_id",
            referencedColumnName = "lic_cond_type_main_cat_id",
            insertable = false,
            updatable = false,
        ),
        JoinColumn(
            name = "lic_cond_type_sub_cat_id",
            referencedColumnName = "lic_cond_type_sub_cat_id",
            insertable = false,
            updatable = false,
        )
    )
    @NotFound(action = NotFoundAction.IGNORE)
    val createAndVaryALicenceMapping: CreateAndVaryALicenceMapping? = null,

    @Column(name = "created_datetime")
    val createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "last_updated_datetime")
    val lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

interface LicenceConditionRepository : JpaRepository<LicenceCondition, Long> {
    @Query(
        """
        select lc from LicenceCondition lc 
        where lc.disposal.event.person.crn = :crn 
        and lc.disposal.active = true and lc.disposal.event.active = true
        """
    )
    fun findAllByCrn(crn: String): List<LicenceCondition>
}