package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

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
    val category: DocReferenceData

)

@Entity
@Immutable
@Table(name="r_standard_reference_list")
class DocReferenceData(
    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long,
    @Column(name = "code_description")
    val description: String
)