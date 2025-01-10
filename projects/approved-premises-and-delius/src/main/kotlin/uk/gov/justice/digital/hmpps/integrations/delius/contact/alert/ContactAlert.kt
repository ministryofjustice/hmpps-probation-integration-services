package uk.gov.justice.digital.hmpps.integrations.delius.contact.alert

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@EntityListeners(AuditingEntityListener::class)
class ContactAlert(
    @Id
    @SequenceGenerator(name = "contact_alert_id_generator", sequenceName = "contact_alert_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_alert_id_generator")
    @Column(name = "contact_alert_id", nullable = false)
    val id: Long = 0,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @Column
    val contactId: Long?,

    @Column(name = "contact_type_id", nullable = false)
    val typeId: Long,

    @Column(name = "offender_id", nullable = false)
    val personId: Long,

    @Column(name = "trust_provider_team_id")
    val teamId: Long,

    @Column(name = "staff_employee_id")
    val staffId: Long,

    @Column(name = "offender_manager_id")
    val personManagerId: Long,

    @Column(name = "trust_provider_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val trustProviderFlag: Boolean = false
)
