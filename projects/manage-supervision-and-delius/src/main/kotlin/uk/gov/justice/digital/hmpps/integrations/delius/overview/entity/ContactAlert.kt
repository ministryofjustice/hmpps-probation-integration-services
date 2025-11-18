package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Staff

@Immutable
@Entity
@Table(name = "contact_alert")
class ContactAlert(

    @ManyToOne
    @JoinColumn(name = "contact_id", nullable = false)
    val contact: Contact,

    @ManyToOne
    @JoinColumn(name = "staff_employee_id", nullable = false)
    val staff: Staff,

    @Id
    @Column(name = "contact_alert_id", nullable = false)
    val id: Long = 0,
)

interface ContactAlertRepository : JpaRepository<ContactAlert, Long>