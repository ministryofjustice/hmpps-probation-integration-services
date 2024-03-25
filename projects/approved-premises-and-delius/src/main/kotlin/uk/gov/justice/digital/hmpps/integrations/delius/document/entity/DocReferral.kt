package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "referral")
class DocReferral(
    @Id
    @Column(name = "referral_id")
    val id: Long,

    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @ManyToOne
    val event: DocEvent,

    @JoinColumn(name = "REFERRAL_TYPE_ID")
    @ManyToOne
    val type: ReferralType
)

@Entity
@Immutable
@Table(name = "assessment")
class DocAssessment(
    @Id
    @Column(name = "assessment_id")
    val id: Long,

    @JoinColumn(name = "referral_id", insertable = false, updatable = false)
    @ManyToOne
    val docReferral: DocReferral? = null,

    @JoinColumn(name = "ASSESSMENT_TYPE_ID")
    @ManyToOne
    val type: AssessmentType
)

@Entity
@Immutable
@Table(name = "r_assessment_type")
class AssessmentType(
    @Id
    @Column(name = "assessment_type_id")
    val id: Long,
    val description: String
)

@Entity
@Immutable
@Table(name = "r_referral_type")
class ReferralType(
    @Id
    @Column(name = "referral_type_id")
    val id: Long,
    val description: String
)
