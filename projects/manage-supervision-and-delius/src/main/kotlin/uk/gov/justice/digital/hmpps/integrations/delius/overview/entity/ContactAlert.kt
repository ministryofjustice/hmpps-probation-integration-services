package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Staff
import uk.gov.justice.digital.hmpps.jpa.GeneratedId

@Immutable
@Entity
@Table(name = "contact_alert")
@SequenceGenerator(name = "contact_alert_id_generator", sequenceName = "contact_alert_id_seq", allocationSize = 1)
class ContactAlert(

    @ManyToOne
    @JoinColumn(name = "contact_id", nullable = false)
    val contact: Contact,

    @Column(name = "contact_type_id", nullable = false)
    val typeId: Long,

    @Column(name = "offender_id", nullable = false)
    val personId: Long,

    @Column(name = "trust_provider_team_id")
    val teamId: Long,

    @Column(name = "offender_manager_id")
    val personManagerId: Long,

    @ManyToOne
    @JoinColumn(name = "staff_employee_id", nullable = false)
    val staff: Staff,

    @Id
    @GeneratedId(generator = "contact_alert_id_generator")
    @Column(name = "contact_alert_id", nullable = false)
    val id: Long = 0,
)

interface ContactAlertRepository : JpaRepository<ContactAlert, Long> {
    fun deleteByContactIdIn(contactId: Collection<Long>)
    fun findByContactId(contactId: Long): MutableList<ContactAlert>
}