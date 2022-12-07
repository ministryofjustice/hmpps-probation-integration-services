package uk.gov.justice.digital.hmpps.integrations.delius.recall.reason

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData

@Immutable
@Entity
@Table(name = "r_recall_reason")
class RecallReason(
    @Id
    @Column(name = "recall_reason_id")
    val id: Long,

    @Column(nullable = false)
    val code: String,

    @Column(nullable = false)
    val description: String,

    @ManyToOne
    @JoinColumn(name = "termination_reason_id")
    val licenceConditionTerminationReason: ReferenceData,

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,
)
