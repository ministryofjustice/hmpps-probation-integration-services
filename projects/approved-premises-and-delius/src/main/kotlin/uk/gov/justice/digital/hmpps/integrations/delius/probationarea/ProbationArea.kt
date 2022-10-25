package uk.gov.justice.digital.hmpps.integrations.delius.probationarea

import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremises
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
@Immutable
class ProbationArea(
    @Id
    @Column(name = "probation_area_id")
    val id: Long = 0,

    @OneToMany(mappedBy = "probationArea")
    val approvedPremises: MutableList<ApprovedPremises> = mutableListOf(),
)
