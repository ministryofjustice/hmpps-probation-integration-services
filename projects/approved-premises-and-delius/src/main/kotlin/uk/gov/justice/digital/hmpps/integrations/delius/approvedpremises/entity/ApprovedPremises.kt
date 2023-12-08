package uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData

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
    val selectable: Boolean = true,
) {
    fun locationCode(): String = probationArea.code + code.code
}
