package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Entity
@Table(name = "upw_appointment")
@SQLRestriction("soft_deleted = 0")
class UnpaidWorkAppointment(
    @Id
    @Column(name = "upw_appointment_id")
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "upw_details_id")
    val details: UnpaidWorkDetails,
    val minutesCredited: Int? = null,
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)