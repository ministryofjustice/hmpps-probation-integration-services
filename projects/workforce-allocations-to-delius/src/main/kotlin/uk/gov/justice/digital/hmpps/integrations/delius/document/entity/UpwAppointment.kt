package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class UpwAppointment {
    @Id
    @Column(name = "upw_appointment_id", insertable = false, updatable = false)
    val id: Long = 0

    @JoinColumn(name = "upw_details_id", insertable = false, updatable = false)
    @ManyToOne
    val upwDetails: UpwDetails? = null
}

@Entity
@Immutable
class UpwDetails {
    @Id
    @Column(name = "upw_details_id", updatable = false)
    val id: Long = 0

    @JoinColumn(name = "DISPOSAL_ID")
    @ManyToOne
    val disposal: DocDisposal? = null
}
