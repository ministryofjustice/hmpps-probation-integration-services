package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@SequenceGenerator(name = "contact_alert_id_generator", sequenceName = "contact_alert_id_seq", allocationSize = 1)
@Table(name = "contact_alert")
class Alert(

    @Column
    val contactId: Long?,

    @Column(name = "contact_type_id")
    val typeId: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "trust_provider_team_id")
    val teamId: Long,

    @Column(name = "staff_employee_id")
    val staffId: Long,

    @Column(name = "offender_manager_id")
    val personManagerId: Long,

    @Column(name = "trust_provider_flag", columnDefinition = "number")
    val trustProviderFlag: Boolean = false,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_alert_id_generator")
    @Column(name = "contact_alert_id")
    val id: Long = 0,
)

interface AlertRepository : JpaRepository<Alert, Long>