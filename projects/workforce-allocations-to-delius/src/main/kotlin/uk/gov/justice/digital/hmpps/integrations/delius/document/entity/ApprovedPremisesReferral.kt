package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData

@Entity
@Immutable
class ApprovedPremisesReferral(
    @Id
    @Column(name = "approved_premises_referral_id")
    val id: Long,

    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @ManyToOne
    val event: DocEvent,

    @JoinColumn(name = "referral_category_id", insertable = false, updatable = false)
    @ManyToOne
    val category: ReferenceData

)
