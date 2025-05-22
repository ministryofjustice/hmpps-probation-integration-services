package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "upw_appointment")
@Immutable
class UpwAppointment(
    @Id
    @Column(name = "upw_appointment_id")
    val id: Long,

    val contactId: Long,

    val upwDetailsId: Long
)

interface UpwAppointmentRepository : JpaRepository<UpwAppointment, Long> {
    fun existsUpwAppointmentsByContactId(contactId: Long): Boolean
}

