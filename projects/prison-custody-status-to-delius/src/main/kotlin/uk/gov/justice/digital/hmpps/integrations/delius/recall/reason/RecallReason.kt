package uk.gov.justice.digital.hmpps.integrations.delius.recall.reason

import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
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
    @Type(type = "yes_no")
    val selectable: Boolean = true,
)
