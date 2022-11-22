package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

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
    val category: StandardReference

)
