package uk.gov.justice.digital.hmpps.controller.casedetails.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.integrations.common.entity.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
@Where(clause = "soft_deleted = 0 and (finish_date is null or finish_date > current_date)")
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

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String,

    @Column(name = "start_date")
    val start: LocalDate? = null,

    @Column(name = "finish_date")
    val endDate: LocalDate? = null,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false
)
