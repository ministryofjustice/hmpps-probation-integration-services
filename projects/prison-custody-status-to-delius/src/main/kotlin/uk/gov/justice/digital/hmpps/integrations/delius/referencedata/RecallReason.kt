package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

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
    var id: Long,

    @Column(nullable = false)
    var code: String,

    @Column(nullable = false)
    @Type(type = "yes_no")
    var selectable: Boolean = true,
)
