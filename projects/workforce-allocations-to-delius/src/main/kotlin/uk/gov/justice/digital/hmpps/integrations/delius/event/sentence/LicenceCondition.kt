package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "lic_condition")
@SQLRestriction("soft_deleted = 0")
class LicenceCondition(
    @Id
    @Column(name = "lic_condition_id")
    val id: Long,

    @Column(name = "disposal_id")
    val disposalId: Long,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_main_cat_id")
    val mainCategory: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_sub_cat_id")
    val subCategory: ReferenceData?,

    @Column(name = "start_date")
    val startDate: LocalDate,

    @Column(name = "commencement_date")
    val commenceDate: LocalDate?,

    @Column(name = "termination_date")
    val terminationDate: LocalDate?,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val activeFlag: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)