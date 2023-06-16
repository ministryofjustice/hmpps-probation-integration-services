package uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team

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

    @ManyToOne
    @JoinColumn(name = "address_id")
    val address: Address,

    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val probationArea: ProbationArea,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true
) {
    fun locationCode(): String = probationArea.code + code.code

    @Transient
    var team: Team? = null

    @Transient
    var staff: Staff? = null
}
