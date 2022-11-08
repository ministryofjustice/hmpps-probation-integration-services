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
class Referral(
    @Id
    @Column(name = "referral_id")
    val id: Long,

    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @ManyToOne
    val event: DocEvent
)

@Entity
@Immutable
class Assessment(
    @Id
    @Column(name = "assessment_id")
    val id: Long,

    @JoinColumn(name = "referral_id", insertable = false, updatable = false)
    @ManyToOne
    val referral: Referral? = null,

    @JoinColumn(name = "ASSESSMENT_TYPE_ID")
    @ManyToOne
    val type: AssessmentType
)

@Entity
@Immutable
@Table(name="r_assessment_type")
class AssessmentType(
    @Id
    @Column(name = "assessment_type_id")
    val id: Long,
    val description: String
)
