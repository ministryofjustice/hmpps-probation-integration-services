package uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.custody.BaseEntity
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "contact_id_seq", sequenceName = "contact_id_seq", allocationSize = 1)
class Contact(
    @Id
    @Column(name = "contact_id", updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_seq")
    val id: Long = 0,
    @Column(name = "offender_id", updatable = false)
    val personId: Long,
    @Column(name = "event_id", updatable = false)
    val eventId: Long? = null,
    @ManyToOne
    @JoinColumn(name = "contact_type_id", updatable = false)
    val type: ContactType,
    @Lob
    val notes: String,
    @Column(name = "contact_date")
    val date: ZonedDateTime = ZonedDateTime.now(),
    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime = ZonedDateTime.now(),
    @Column(updatable = false)
    val staffId: Long,
    @Column(updatable = false)
    val staffEmployeeId: Long = staffId,
    @Column(updatable = false)
    val teamId: Long,
    @Column(name = "probation_area_id", updatable = false)
    val providerId: Long,
    @Column(name = "sensitive")
    @Convert(converter = YesNoConverter::class)
    val isSensitive: Boolean = type.isSensitive,
    @Column(updatable = false)
    val trustProviderTeamId: Long = teamId,
    @Column(updatable = false, columnDefinition = "NUMBER")
    val trustProviderFlag: Boolean = false,
    @Convert(converter = YesNoConverter::class)
    @Column(name = "alert_active")
    val alert: Boolean? = false,
) : BaseEntity()

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,
    val code: String,
    @Column(name = "sensitive_contact")
    @Convert(converter = YesNoConverter::class)
    val isSensitive: Boolean = false,
    @Convert(converter = YesNoConverter::class)
    @Column(name = "contact_alert_flag")
    val alert: Boolean? = false,
)
