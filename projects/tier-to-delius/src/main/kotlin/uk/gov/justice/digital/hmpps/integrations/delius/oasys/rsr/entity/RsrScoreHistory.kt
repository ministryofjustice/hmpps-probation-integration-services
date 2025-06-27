package uk.gov.justice.digital.hmpps.integrations.delius.oasys.rsr.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.ZonedDateTime

@Entity
@Immutable
@Table(name = "dynamic_rsr_history")
class RsrScoreHistory(
    @Id
    @Column(name = "dynamic_rsr_history_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "dynamic_rsr_score", columnDefinition = "number(5,2)")
    val score: Double,

    @ManyToOne
    @JoinColumn(name = "reason_for_change_id")
    val reasonForChange: ReferenceData,

    @Column
    val dateRecorded: ZonedDateTime,
)