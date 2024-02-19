package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData

@Entity
@Immutable
@Table(name = "approved_premises_referral")
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
