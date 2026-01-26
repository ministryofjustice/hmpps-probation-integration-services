package uk.gov.justice.digital.hmpps.data.entity

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "contact_alert")
class ContactAlert(
    @Id
    @Column(name = "contact_alert_id")
    val id: Long = 0,

    @Column(name = "contact_id")
    val contactId: Long?,

    @Column(name = "contact_type_id")
    val contactTypeId: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "offender_manager_id")
    val personManagerId: Long,

    @Column(name = "trust_provider_team_id")
    val teamId: Long,

    @Column(name = "staff_employee_id")
    val staffId: Long,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0
)

interface ContactAlertRepository : JpaRepository<ContactAlert, Long> {
    fun findByContactId(contactId: Long): ContactAlert?
}