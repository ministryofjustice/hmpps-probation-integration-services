package uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises

import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Immutable
@Table(name = "r_approved_premises")
class ApprovedPremises(
    @Id
    @Column(name = "approved_premises_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "national_hostel_code")
    val code: ReferenceData,
)
