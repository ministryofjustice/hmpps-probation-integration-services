package uk.gov.justice.digital.hmpps.integrations.delius.recall.reason

import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

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
    @Type(type = "yes_no")
    val selectable: Boolean = true,
)
