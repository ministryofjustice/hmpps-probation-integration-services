package uk.gov.justice.digital.hmpps.controller.casedetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.common.entity.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0 and (finish_date is null or finish_date > current_date)")
@Table(name = "disability")
class DisabilityEntity(
    @Id
    @Column(name = "disability_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val case: CaseEntity,

    @ManyToOne
    @JoinColumn(name = "disability_type_id", updatable = false)
    val type: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "disability_condition_id", updatable = false)
    val condition: ReferenceData? = null,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String? = null,

    @Column(name = "start_date")
    val start: LocalDate? = null,

    @Column(name = "finish_date")
    val endDate: LocalDate? = null,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)
